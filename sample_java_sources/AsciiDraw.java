import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class AsciiDraw {

	private static byte[] output;

	/*Reads image into *img from file.*/
	public static void initImage(String imgFilename)
	{
		try 
		{
		    File file = new File(imgFilename);
		    output=new byte[(int) file.length()];
		    FileInputStream fileInputStream = new FileInputStream(file);
		    fileInputStream.read(output);
		    fileInputStream.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/*Decrypt and display image*/
	public static void decryptAndDisplay()
	{
		int i=0;
		char theKey='*';

		for (i = 0; i < output.length; i++) 
		{
			//Below is an example anti-disassembly mark. In this example, we are trying to prevent disassembly of the code that decrypts the image.
			/*<antidisasm><platform>java</platform><input><inputname>output[i]</inputname><inputdatatype>byte</inputdatatype></input><input><inputname>theKey</inputname><inputdatatype>byte</inputdatatype></input><statementtype>assignment</statementtype><howtocompile>Makefile</howtocompile></antidisasm>*/
		       	output[i]=(byte)(output[i]+theKey);
		}

		String s = new String(output);

		System.out.println(s);
	}

    public static void main(String[] args) 
    {
	int input;
	int realPassword=17388600;
	System.out.println("Hello! This program will draw one of two ASCII pictures: one for the correct numeric password entered, one for the incorrect password. \n\nPlease enter a numeric password.\n");
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter Password:");
        try{
            int i = Integer.parseInt(br.readLine());
	    if (i!=realPassword)
	    {
		initImage("3");
		decryptAndDisplay();
	    }
	    else
	    {
		initImage("4");
		decryptAndDisplay();
	    }
        }catch(NumberFormatException nfe){
            System.err.println("Invalid password entry.");
        }
	catch(IOException ioe){
            System.err.println("Unable to read input.");
        }
    }
    
}
