//TODO: Control flow obfuscation comes from Java Control Flow Obfuscation by Douglas Low.

//Excluding package for now until can resolve with makefile (having packagename leads to "class not found" error. Maybe because source not in path specified by package name?)
//package com.columbia.edu.comse6156.ProteusObfuscatorVersionTwo;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.io.ByteArrayInputStream;
import org.apache.commons.io.FileUtils;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.apache.commons.lang3.StringUtils;
import java.util.HashMap;

public class Proteus {

	private static String pubkeyname;

	private static String privkeyname;

	private static File destDir;

	private static final String RANDOM_FILENAME_LEXICON="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	private static final int RANDOM_FILENAME_LENGTH=8;

	private static final int UNKNOWN_ERROR=1;

	private static final int INCORRECT_ARGUMENT_LENGTH=2;

	private static final int XML_PARSE_ERROR=3;

	private static final int HELIB_PROGRAM_BUILD_ERROR=4;

	private static final int RAND_MIN_INWARD_JMP=5;

	private static final int RAND_MAX_INWARD_JMP=15;

	private static final int RAND_MAX=100;

	private static final int RAND_MIN=1;

	private static final int NUM_BYTES_FOR_XOR_EAX_EAX=2;

	private static ArrayList<String> fileStrings = new ArrayList<String>();

	private static HashMap<String, String> encryptedVariableNames = new HashMap<String, String>();

	/*Reserved for future use.*/
	private static String simpleCFalseOpaquePredicateInitializeStmt="int x=A;\nint y=B;\n";

	/*Templates for false opaque predicates (predicates whose if-statement always evaluates to false at runtime) in C*/
	private static String[] simpleCFalseOpaquePredicates = {"if ( ((7*y)-1) == (int)(pow(x, 2)) )","if ( ( ((int)pow(((int)x), 3) - x)  % 3)  !=0  )"};

	/*Templates for true opaque predicates (predicates whose if-statement always evaluates to true at runtime) in C*/
	private static String[] simpleCTrueOpaquePredicates = {"if ((int)( pow(x, 2) + x ) % 2 == 0 )","if (( (int)x % 2 == 0 ) || ((int)( pow(x, 2) - 1 ) % 8 == 0 ))"};

	/*Templates for false opaque predicates (predicates whose if-statement always evaluates to false at runtime) in Java*/
	private static String[] simpleJavaFalseOpaquePredicates = {"if ( (7*y)-1 == Math.pow(x, 2) )","if (( Math.pow(x, 3) - x ) % 3 != 0 )"};
	
	/*Templates for true opaque predicates (predicates whose if-statement always evaluates to false at runtime) in Java*/
	private static String[] simpleJavaTrueOpaquePredicates = {"if (( Math.pow(x, 2) + x ) % 2 == 0 )","if (( x % 2 == 0 ) || (( Math.pow(x, 2) - 1 ) % 8 == 0 ))"};

	/*Template for inward jump in GCC*/
	private static String xEightySixGCCInwardJump = "asm __volatile__ (\".byte 0x66\");\nasm __volatile__ (\".byte 0xB8\");\nasm __volatile__ (\".byte 0xEB\");\nasm __volatile__ (\".byte 0xXX\");\nasm __volatile__ (\".byte 0x31\");\nasm __volatile__ (\".byte 0xC0\");\nasm __volatile__ (\".byte 0x74\");\nasm __volatile__ (\".byte 0x-(XX+2)\");\nasm __volatile__ (\".byte 0xE8\");";

	private static String proteusAssistantPath="../proteusassistant";

	/*Random seed.*/
	private static Random rand = new java.util.Random();

	private static boolean getRandomBoolean() 
	{
       		return rand.nextBoolean();
   	}

	private static int getRandomInteger(int min, int max)
	{
		// nextInt is normally exclusive of the top value, so add 1 to make it inclusive
		return rand.nextInt((max - min) + 1) + min;
	}

	private static String getStringOfRandomLength(String characters, int length)
	{
		char[] text = new char[length];
		for (int i = 0; i < length; i++)
		{
			text[i] = characters.charAt(rand.nextInt(characters.length()));
		}
		return new String(text);
	}
	
	private static boolean isAFileToExamineForMethodReplacement(String source)
	{
		if (source.toLowerCase().endsWith(".java"))
		{
			return true;
		}
		else if ( (source.toLowerCase().endsWith(".c")) )
		{
			return true;
		}
		else if (source.toLowerCase().endsWith(".cpp"))
		{
			return true;
		}
		//For future release
		/*else if (source.toLowerCase().endsWith(".h"))
		{
			return true;
		}*/
		else
		{
			return false;
		}
	}

	private static boolean isAJavaSource(String source)
	{
		if (source.toLowerCase().endsWith(".java"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private static boolean isABuildFile(String source)
	{
		
		if (source.toLowerCase().endsWith("makefile"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private static boolean isAntidisasmProtectionMark(String line)
	{
		/*Remove comments*/
		line=StringUtils.remove(line, "/*");
		line=StringUtils.remove(line, "*/");
		line=StringUtils.remove(line, "//");

		/*Then, examine the XML to determine if this is an anti-disassembly mark.*/
		if ((line.trim().startsWith("<antidisasm>")))
		{
			try
			{

				/*Look for compiler and cpu type.*/
				StringBuilder xmlStringBuilder = new StringBuilder();
				xmlStringBuilder.append(line);

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();

				ByteArrayInputStream input =  new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
				Document doc = builder.parse(input);

				//optional, but recommended
				//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
				doc.getDocumentElement().normalize();

				Element root = doc.getDocumentElement();

				String type=doc.getDocumentElement().getNodeName();

				System.out.println("Type of protection to implement: "+type);

				return type.toLowerCase().equals("antidisasm");
			
			}

			catch (Exception e)
			{
				System.out.println("Error in parsing XML statement");
				e.printStackTrace();
				return false;
			}
		}
		else
		{
			return false;
		}
	}

	private static boolean isAntidebugProtectionMark(String line)
	{
		/*Remove comments*/
		line=StringUtils.remove(line, "/*");
		line=StringUtils.remove(line, "*/");
		line=StringUtils.remove(line, "//");

		/*Then, examine the XML to determine if this is an anti-debug mark.*/
		if ((line.trim().startsWith("<antidebug>")))
		{
			try
			{

				//Parse apart all the tags. Look for compiler and cpu type.
				StringBuilder xmlStringBuilder = new StringBuilder();
				xmlStringBuilder.append(line);

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();

				ByteArrayInputStream input =  new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
				Document doc = builder.parse(input);

				//optional, but recommended
				//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
				doc.getDocumentElement().normalize();

				Element root = doc.getDocumentElement();

				String type=doc.getDocumentElement().getNodeName();

				System.out.println("Type of protection to implement is: "+type);

				return type.toLowerCase().equals("antidebug");
			
			}

			catch (Exception e)
			{
				System.out.println("Error in parsing XML statement");
				e.printStackTrace();
				return false;
			}
		}
		else
		{
			return false;
		}
	}

	/*Generate anti-debug code*/
	private static String generateAntidebug(String mark, String lineBelow)
	{
		String rslt="";

		boolean choseTruePredicate;

		int predicateIndex=0;

		int predicateMin=0;

		int predicateMax=0;

		/*Remove the comments.*/
		mark=StringUtils.remove(mark, "/*");
		mark=StringUtils.remove(mark, "*/");
		mark=StringUtils.remove(mark, "//");

		try
		{
			//Parse apart all the tags. Look for compiler and cpu type.
			StringBuilder xmlStringBuilder = new StringBuilder();
			xmlStringBuilder.append(mark);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			ByteArrayInputStream input =  new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
			Document doc = builder.parse(input);

			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			Element root = doc.getDocumentElement();

			String platform=root.getElementsByTagName("platform").item(0).getTextContent();

			String statementtype=root.getElementsByTagName("statementtype").item(0).getTextContent();

			/*If this anti-debug statement is a declaration (identification of variable and value to protect), create its encrypted integer equivalent.*/
			if (statementtype.toLowerCase().equals("declaration"))
			{
				String originalVariableName=root.getElementsByTagName("inputname").item(0).getTextContent();
				String valueToEncrypt=root.getElementsByTagName("inputval").item(0).getTextContent();
				String datatype=root.getElementsByTagName("inputdatatype").item(0).getTextContent();
				if (datatype.toLowerCase().equals("int"))
				{
					//Create the encrypted form in the path.
					generateEncryptedInteger(originalVariableName, valueToEncrypt, pubkeyname, privkeyname);
					lineBelow="";//"/*"+lineBelow+"*/"; //This reveals the original line as a comment!
					rslt=rslt+lineBelow+"\n";
				}
			}

			/*Else, if this anti-debug statement is a comparison (identification of variable and value to protect), create its encrypted integer equivalent.*/
			else if (statementtype.toLowerCase().equals("ifeq"))
			{
				//Construct opaque predicate. First, choose whether to use a true or false predicate. Then, choose from the array of chosen predicates.
				NodeList nList = doc.getElementsByTagName("input");

				ArrayList<Operand> operands=new ArrayList<Operand>();

				choseTruePredicate=getRandomBoolean();

				int temp = 0;

				for (temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					String inputname=eElement.getElementsByTagName("inputname").item(0).getTextContent();
					String inputdatatype=eElement.getElementsByTagName("inputdatatype").item(0).getTextContent();
					Operand op=new Operand(inputdatatype, inputname);
					if (encryptedVariableNames.get(inputname)!=null)
					{
						op.setToSecret(true);
					}
					operands.add(op);
				}
			}

			/*Build new comparison statement*/
			rslt="if (doComparison(returnIntEncodedAsHECiphertext(VARIABLE),ENCVALFILE))";
			for (temp = 0; temp < operands.size(); temp++) {

				//System.out.println("Value for "+operands.get(temp).getName()+" in hash: "+encryptedVariableNames.get(operands.get(temp).getName()));
				if ( (operands.get(temp).isThisSecret()) )
				{
					rslt=StringUtils.replace(rslt, "ENCVALFILE", "\""+encryptedVariableNames.get(operands.get(temp).getName())+"\"");
				}
				else
				{
					rslt=StringUtils.replace(rslt, "VARIABLE", operands.get(temp).getName());
				}
			}

			lineBelow="";
			rslt=rslt+lineBelow+"\n";
			}
		}
		catch (Exception e)
		{
			System.out.println("Error in parsing XML statement");
			e.printStackTrace();
		}
		finally
		{
			return rslt;
		}
	}

	
	private static String generateAntidisassembly(String mark, String lineBelow)
	{
		
		String rslt="";

		boolean choseTruePredicate;

		int predicateIndex=0;

		int predicateMin=0;

		int predicateMax=0;

		//Remove the comments.
		mark=StringUtils.remove(mark, "/*");
		mark=StringUtils.remove(mark, "*/");
		mark=StringUtils.remove(mark, "//");

		try
		{
			//Parse apart all the tags. Look for compiler and cpu type.
			StringBuilder xmlStringBuilder = new StringBuilder();
			xmlStringBuilder.append(mark);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			ByteArrayInputStream input =  new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
			Document doc = builder.parse(input);

			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			Element root = doc.getDocumentElement();

			//System.out.println("Platform : " + root.getElementsByTagName("platform").item(0).getTextContent());

			String platform=root.getElementsByTagName("platform").item(0).getTextContent();

			String statementtype=root.getElementsByTagName("statementtype").item(0).getTextContent();


			//Construct opaque predicate. First, choose whether to use a true or false predicate. Then, choose from the array of chosen predicates.

			NodeList nList = doc.getElementsByTagName("input");

			ArrayList<Operand> operands=new ArrayList<Operand>();

			choseTruePredicate=getRandomBoolean();

			predicateIndex=0;

			predicateMin=0;

			predicateMax=0;

			if (choseTruePredicate)
			{
				if ((platform.toLowerCase().equals("gcc-x86")))
				{
					predicateMax=simpleCTrueOpaquePredicates.length-1;
					predicateIndex=getRandomInteger(0, predicateMax);
					rslt=simpleCTrueOpaquePredicates[predicateIndex];
				}
				else if ((platform.toLowerCase().equals("java")))
				{
					predicateMax=simpleJavaTrueOpaquePredicates.length-1;
					predicateIndex=getRandomInteger(0, predicateMax);
					rslt=simpleJavaTrueOpaquePredicates[predicateIndex];
				}
			}

			else
			{
				if ((platform.toLowerCase().equals("gcc-x86")))
				{
					predicateMax=simpleCFalseOpaquePredicates.length-1;
					predicateIndex=getRandomInteger(0, predicateMax);
					rslt=simpleCFalseOpaquePredicates[predicateIndex];
				}
				else if ((platform.toLowerCase().equals("java")))
				{
					predicateMax=simpleJavaFalseOpaquePredicates.length-1;
					predicateIndex=getRandomInteger(0, predicateMax);
					rslt=simpleJavaFalseOpaquePredicates[predicateIndex];
				}
			}

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
			
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					//System.out.println("Input name : " + eElement.getElementsByTagName("inputname").item(0).getTextContent());
					//System.out.println("Input data type : " + eElement.getElementsByTagName("inputdatatype").item(0).getTextContent());
					String inputname=eElement.getElementsByTagName("inputname").item(0).getTextContent();
					String inputdatatype=eElement.getElementsByTagName("inputdatatype").item(0).getTextContent();
					Operand op=new Operand(inputdatatype, inputname);
					operands.add(op);
				}
			}

			if ( (operands.get(0).getType().toLowerCase().equals("int")) || (operands.get(0).getType().toLowerCase().equals("byte")) )
			{
				rslt=StringUtils.replace(rslt, "x", "("+operands.get(0).getName()+")");
				//System.out.println("rslt="+rslt);
			}
			else
			{
				//TODO Limitation: only going to consider for C/C++ b/c of quick and dirty casting. For Java, will have to take into consideration not just primitives, but objects being compared. For Java, if user really wanted it, would have to add a function for us that handles the try-catch we cannot insert.
				//rslt=StringUtils.replace(rslt, "x", "(Integer.parseInt("+operands[0].name+"))");
				if ((platform.toLowerCase().equals("gcc-x86")))
				{
					rslt=StringUtils.replace(rslt, "x", "((int)("+operands.get(0).getName()+"))");
				}
				
			}
			if ( (operands.get(1).getType().toLowerCase().equals("int")) || (operands.get(1).getType().toLowerCase().equals("byte")) )
			{
				rslt=StringUtils.replace(rslt, "y", "("+operands.get(1).getName()+")");
				//System.out.println("rslt="+rslt);
			}
			else
			{
				//TODO Limitation: only going to consider for C/C++ b/c of quick and dirty casting. For Java, will have to take into consideration not just primitives, but objects being compared. For Java, if user really wanted it, would have to add a function for us that handles the try-catch we cannot insert.
				//rslt=StringUtils.replace(rslt, "y", "(Integer.parseInt("+operands[1].name+"))");
				if ((platform.toLowerCase().equals("gcc-x86")))
				{
					rslt=StringUtils.replace(rslt, "y", "((int)("+operands.get(1).getName()+"))");
				}
			}
			rslt=rslt+"\n{\n";

			//This is the statement for the other line of code that is never executed. It should just be a modification of the line to protect.
			String falseStatement="";

			if ((platform.toLowerCase().equals("java")))
			{
				falseStatement=lineBelow;

				//Check if statement below is an assignment statement. If so, replace the inputs to the right of the equals sign.
				if (statementtype.toLowerCase().equals("assignment"))
				{
					String[] expressions = falseStatement.split("=");
					falseStatement=expressions[1];
					falseStatement=StringUtils.replace(falseStatement, operands.get(0).getName(), operands.get(0).getName()+"+"+String.valueOf(getRandomInteger(RAND_MIN, RAND_MAX)));
					falseStatement=StringUtils.replace(falseStatement, operands.get(1).getName(), operands.get(1).getName()+"+"+String.valueOf(getRandomInteger(RAND_MIN, RAND_MAX)));
					falseStatement=expressions[0]+"="+falseStatement;

					//System.out.println("Expression is "+falseStatement);
				}
				
				//Else, just assign random values to the statement.
				else
				{
					falseStatement=StringUtils.replace(falseStatement, operands.get(0).getName(), operands.get(0).getName()+"+"+String.valueOf(getRandomInteger(RAND_MIN, RAND_MAX)));
					falseStatement=StringUtils.replace(falseStatement, operands.get(1).getName(), operands.get(1).getName()+"+"+String.valueOf(getRandomInteger(RAND_MIN, RAND_MAX)));
				}
			}
			else if ((platform.toLowerCase().equals("gcc-x86")))
			{
				//Build the inward jump stmt.
				int inwardJumpLength = getRandomInteger(RAND_MIN_INWARD_JMP, RAND_MAX_INWARD_JMP);

				String inwardJmpHex=String.format("%02X", inwardJumpLength);

				String inwardJmpBackHex=String.format("%02X", -(inwardJumpLength+NUM_BYTES_FOR_XOR_EAX_EAX));

				//System.out.println("inwardJmpHex="+inwardJmpHex+", inwardJmpBackHex="+inwardJmpBackHex);

				falseStatement=xEightySixGCCInwardJump;

				falseStatement=StringUtils.replace(falseStatement, "0xXX", "0x"+inwardJmpHex);

				falseStatement=StringUtils.replace(falseStatement, "0x-(XX+2)", "0x"+inwardJmpBackHex);
			}
			
			if (choseTruePredicate)
			{
				rslt=rslt+lineBelow+"\n";
				rslt=rslt+"}\nelse{\n"+falseStatement;
			}
			else
			{
				rslt=rslt+falseStatement+"\n";
				rslt=rslt+"}\nelse{\n"+lineBelow;
			}
			rslt=rslt+"\n}\n";

			return rslt;
		}

		catch (Exception e)
		{
			System.out.println("Error in parsing XML statement");
			e.printStackTrace();
			return "";
		}
	}
		
	private static void applyProtectionToSource(final File folder)
	{
		String source="";
		for (final File fileEntry : folder.listFiles()) 
		{
			if (fileEntry.isDirectory()) {
				//replaceMethodsWithObfuscatedNames(fileEntry);
			} 
			else {
				source=fileEntry.getAbsolutePath();
				if ( isAFileToExamineForMethodReplacement(source) )
				{
					try
			    		{
						//Create a bak copy of each source by copying the source to a .bak file.
						InputStream inStream = null;
						OutputStream outStream = null;
						String line;

						File srcFile =new File(source);
						File bkupFile =new File(source+".bak");

						inStream = new FileInputStream(srcFile);
						outStream = new FileOutputStream(bkupFile);

						byte[] buffer = new byte[1024];

						int length;
						//copy the file content in bytes 
						while ((length = inStream.read(buffer)) > 0){
							outStream.write(buffer, 0, length);
						}

						inStream.close();
						outStream.close();

						System.out.println("Backup of file "+source+" was successful.");

						//Replace all instances of method names with matching obfuscated method name.
	
						// Reading objects
						FileInputStream fis = new FileInputStream(srcFile);
						DataInputStream inp = new DataInputStream(fis);
						BufferedReader br = new BufferedReader(new InputStreamReader(inp));

						//Read in all the strings of the source
						while ((line = br.readLine()) != null) {
							   fileStrings.add(line);
						}

						br.close();
						inp.close();
						fis.close();

						
						// Write protected source.
						File protectedSrc = new File(srcFile.getAbsolutePath());
						FileWriter fw = new FileWriter(protectedSrc);
						BufferedWriter out = new BufferedWriter(fw);

						//Then, go through each line read in, writing to output file. If mark found, write that in place of the mark, then continue.

						//First pass is for anti-disassembly, and must be done separately from anti-debug, as that one requires potentially modifying prior lines.
						//for (String fileString : fileStrings) 
						//Need an index because may need to pass string after current.
						for (int i = 0; i < fileStrings.size()-1; i++) 
						{
						    if (isAntidisasmProtectionMark(fileStrings.get(i)))
						    {
							out.write(generateAntidisassembly(fileStrings.get(i),fileStrings.get(i+1)));

							//Erase the line below.
							fileStrings.set(i+1,"");
						    }
						    if (isAntidebugProtectionMark(fileStrings.get(i)))
						    {
							out.write(generateAntidebug(fileStrings.get(i),fileStrings.get(i+1)));

							//Erase the line below.
							fileStrings.set(i+1,"");
						    }
						    else
						    {
							out.write(fileStrings.get(i));
						    }
						    out.write("\n");
						}

						out.write(fileStrings.get(fileStrings.size()-1));

						out.close();
						fw.close();
			    		}
			    		catch (Exception e)
			    		{
			    			
			    		}
				}
			}
			fileStrings.clear();
		}
	}

	private static String locateMakefilePath(final File folder)
	{
		String buildPath=" ";
		for (final File fileEntry : folder.listFiles()) 
		{
			if (!fileEntry.isDirectory()) {
				String source=fileEntry.getAbsolutePath();
				if ( isABuildFile(source) )
				{
					buildPath=source.substring(0,source.lastIndexOf(File.separator));
					break;
				}
			}
		}
		return buildPath;
	}

	private static int generateHELibProgram()
	{
		String buildCmd = "";

		String[] envp = { };

		int execRslt=-1;

		//Go to the path and run make if a makefile has been found.
		buildCmd="cd "+proteusAssistantPath+"; make";

		try
		{
			//Launch process with another instance of bash (or cmd, for Windows). This ensures that the build process will complete.
			Process proc=Runtime.getRuntime().exec(new String[]{ "bash","-c",  buildCmd });

			String procOutput="";
			BufferedReader in = new BufferedReader(
			       new InputStreamReader(proc.getInputStream()) );
		       		while ((procOutput = in.readLine()) != null) {
			 	System.out.println(procOutput);
		       }
		       in.close();

			proc.waitFor();

			execRslt=proc.exitValue();
		}
		catch(Exception e)
		{
		    System.out.println("generateHELibProgram() Error: "+e.getMessage());
		    e.printStackTrace();
		}
		finally
		{
			return execRslt;
		}
	}

	 private static int generatePublicAndPrivateKeys(String pubkeyname, String privkeyname)
	 {
		String generateCmd = "";

		String[] envp = { };

		int execRslt=-1;

		//Go to the path and run make if a makefile has been found.
		System.out.println("Public key: "+pubkeyname);
		System.out.println("Private key: "+privkeyname);
		generateCmd="cd "+proteusAssistantPath+"; ./proteusassistant generate "+pubkeyname+" "+privkeyname;

		try
		{
			//Launch process with another instance of bash (or cmd, for Windows). This ensures that the build process will complete.
			Process proc=Runtime.getRuntime().exec(new String[]{ "bash","-c",  generateCmd });

			String procOutput="";
			BufferedReader in = new BufferedReader(
			       new InputStreamReader(proc.getInputStream()) );
		       		while ((procOutput = in.readLine()) != null) {
			 	System.out.println(procOutput);
		       }
		       in.close();

			proc.waitFor();

			execRslt=proc.exitValue();
		}
		catch(Exception e)
		{
		    System.out.println("generatePublicAndPrivateKeys() Error: "+e.getMessage());
		    e.printStackTrace();
		}
		finally
		{
			return execRslt;
		}
	}

	 private static int generateEncryptedInteger(String intName, String intVal, String pubkeyname, String privkeyname)
	 {
		String generateCmd = "";

		String[] envp = { };

		int execRslt=-1;

		try
		{
			String encryptedIntFilename=getStringOfRandomLength(RANDOM_FILENAME_LEXICON, RANDOM_FILENAME_LENGTH);
			String encryptedIntFilenameWithPath=destDir.getAbsolutePath()+"/"+encryptedIntFilename;

			generateCmd="cd "+proteusAssistantPath+"; ./proteusassistant encipherint "+intVal+" "+encryptedIntFilenameWithPath+" "+pubkeyname+" "+privkeyname;

			System.out.println(generateCmd);

			//Launch process with another instance of bash (or cmd, for Windows). This ensures that the build process will complete.
			Process proc=Runtime.getRuntime().exec(new String[]{ "bash","-c",  generateCmd });

			String procOutput="";
			BufferedReader in = new BufferedReader(
			       new InputStreamReader(proc.getInputStream()) );
		       		while ((procOutput = in.readLine()) != null) {
			 	System.out.println(procOutput);
		       }
		       in.close();

			proc.waitFor();

			execRslt=proc.exitValue();

			if (execRslt==0)
			{
				//Add name to map of encrypted names;
				//System.out.println("Storing ("+intName+", "+encryptedIntFilename+")");
				encryptedVariableNames.put(intName, encryptedIntFilename);
			}
		}
		catch(Exception e)
		{
		    System.out.println("generatePublicAndPrivateKeys() Error: "+e.getMessage());
		    e.printStackTrace();
		}
		finally
		{
			return execRslt;
		}
	}
	
	 public static void main (String[] args) 
	 {		
		try
		{

			//Make a copy of the source folder. We will work in the folder copy.
			if (args.length!=2)
			{
				System.out.println("How to run: java Proteus {number of binaries with different instantiation of desired protection to be created} {path to folder containing source}");
				System.exit(INCORRECT_ARGUMENT_LENGTH);
			}

			//Create HELib program that will generate keys if it doesn't exist. Do this by going into that directory and running make.
			if (generateHELibProgram()==0)
			{
				System.out.println("HELib program successfully built.");
			}
			else
			{
				System.out.println("HELib program not successfully built. This program is required for anti-debugging protection.");
				System.exit(HELIB_PROGRAM_BUILD_ERROR);
			}

			int numberOfTimesToRun=Integer.parseInt(args[0]);
			String srcPath=args[1];

			for (int i=0; i<numberOfTimesToRun; i++)
			{
				SimpleDateFormat sDate = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
				Date dateNow = new Date();
				String date_to_string = sDate.format(dateNow);
				long millis = System.currentTimeMillis() % 1000;
				File srcDir = new File(srcPath);
				destDir = new File("proteus_working_directory_"+date_to_string+"_"+Long.toString(millis));
				FileUtils.copyDirectory(srcDir, destDir);

				//Generate public and private keys for anti-debug
				pubkeyname=destDir.getAbsolutePath()+"/"+getStringOfRandomLength(RANDOM_FILENAME_LEXICON, RANDOM_FILENAME_LENGTH);
				privkeyname=destDir.getAbsolutePath()+"/"+getStringOfRandomLength(RANDOM_FILENAME_LEXICON, RANDOM_FILENAME_LENGTH);
				generatePublicAndPrivateKeys(pubkeyname, privkeyname);

				//Apply protection code.
				applyProtectionToSource(destDir);

				//Build executable.
				String buildPath=locateMakefilePath(destDir);

				String buildCmd = "";

				String[] envp = { };

				System.out.println("Building protected source..");

				//Go to the path and run make if a makefile has been found.
				if ( !buildPath.equals(" ") )
				{
					buildCmd="cd "+buildPath+"; make";
				}

				//System.out.println("Path is "+buildPath);

				//Launch process with another instance of bash (or cmd, for Windows). This ensures that the build process will complete.
				Process proc=Runtime.getRuntime().exec(new String[]{ "bash","-c",  buildCmd });

				String procOutput="";
				BufferedReader in = new BufferedReader(
				       new InputStreamReader(proc.getInputStream()) );
			       		while ((procOutput = in.readLine()) != null) {
				 	System.out.println(procOutput);
			       }
			       in.close();

				proc.waitFor();

				int execRslt=proc.exitValue();
				
				System.out.println("Build exited with code "+Integer.toString(execRslt));

				encryptedVariableNames.clear();

				/*if (execRslt!=0)
				{
					//Spit out error message
				}*/
			}
			
		}
		catch(Exception e)
		{
		    System.out.println("Error: "+e.getMessage());
		    e.printStackTrace();
		}
	 }

}
