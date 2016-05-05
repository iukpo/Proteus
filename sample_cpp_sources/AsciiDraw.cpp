#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include <string.h>
#include <proteus_app.h>

/*Image to be decrypted by asciidraw*/
unsigned char *img;

/*Size of image to be decrypted by asciidraw*/
long int imgFilesize;

/*Reads image into *img from file.*/
void initImage(char *imgFilename);

/*Decrypt and display image*/
void decryptAndDisplay();

/*Request password from user. Depending on correctness of answer, one of two images will be displayed.*/
void requestAndVerifyPassword();

void initImage(char *imgFilename)
{
	char theKey='*';
	FILE *freader;
	unsigned char *buffer;
	freader = fopen(imgFilename, "r");
	fseek(freader, 0L, SEEK_END);
	imgFilesize = ftell(freader);
	img=(unsigned char*)calloc(1,imgFilesize);
	fseek(freader, SEEK_SET, 0);
	fread(img, imgFilesize, 1, freader);
	fclose(freader);
}

void decryptAndDisplay()
{
	int i=0;
	char theKey='*';

	for (i=0; i<imgFilesize; i++)
	{
		//Below is an example anti-disassembly mark. In this example, we are trying to prevent disassembly of the code that decrypts the image.
		/*<antidisasm><platform>gcc-x86</platform><inputs><input><inputname>img[i]</inputname><inputdatatype>char</inputdatatype></input><input><inputname>theKey</inputname><inputdatatype>char</inputdatatype></input></inputs><statementtype>assignment</statementtype><howtocompile>Makefile</howtocompile></antidisasm>*/
		img[i]=img[i]+theKey;
	}
	printf("%s\n", img);
}

void requestAndVerifyPassword()
{
	int input;

	//Below is an example anti-debugging mark. This mark identifies the name and value of the real password we are trying to protect from prying eyes.
	/*<antidebug><platform>gcc-x86</platform><statementtype>declaration</statementtype><inputs><input><inputname>realPassword</inputname><inputdatatype>int</inputdatatype><inputval>17388600</inputval></input></inputs><howtocompile>Makefile</howtocompile></antidebug>*/
	int realPassword=17388600;
	printf("Hello! This program will draw one of two ASCII pictures: one for the correct numeric password entered, one for the incorrect password. \n\nPlease enter a numeric password.\n");
	scanf("%d", &input);

	//Below is an example anti-debugging mark. This mark identifies the inputs into the comparison, as well as the comparison itself.
	/*<antidebug><platform>gcc-x86</platform><statementtype>ifeq</statementtype><inputs><input><inputname>input</inputname><inputdatatype>int</inputdatatype><inputtype>var</inputtype></input><input><inputname>realPassword</inputname><inputdatatype>int</inputdatatype><inputtype>hidethis</inputtype><inputval>17388600</inputval></input></inputs><howtocompile>Makefile</howtocompile></antidebug>*/
	if (input==realPassword)
	{
		initImage("4");
	}
	else
	{
		initImage("3");
	}
	decryptAndDisplay();
}

int main(int argc, char **argv)
{

	printf("Starting HELib...\n");

	//Initialize HELib before using any of its functions.
	setupHELib();

	//Initialize keys.
	string pubkeyname(argv[1]);
			
	string privkeyname(argv[2]);

	printf("Reading keys...\n");

	readKeys((char *)pubkeyname.c_str(), (char *)privkeyname.c_str());

	//Continue with the rest of execution.
	requestAndVerifyPassword();

	return 0;
}
