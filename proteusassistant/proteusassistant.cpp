//TODO: Limitation-SK is available on FS in this implementation. Needs to be hidden to prevent an attacker.

//TODO: Limitation-encoded value is very large, like keys! Need to write out.

//TODO: Limitation-program returning unencrypted result for comparison. To be more secure, should return ciphertext for processing.


#include "proteus_app.h"
#include <cstring>
#include "EncryptedArray.h"
#include <NTL/lzz_pXFactoring.h>
#include <iostream>
#include <stack>
#include <string>
#include <stdlib.h>
using namespace std;
FHEcontext* jni_context;
FHESecKey* jni_secretKey;
FHEPubKey* jni_publicKey;
ZZX jni_G;
string secretJNIIntFileName;

//Dump secret integer as ciphertext file. (JNI only)
void dumpSecretInteger(int val, string filename);

//Create key files (JNI)
void createKeys(char *pubkeyfilename, char *privkeyfilename);

//Setup required variables.
void setupGeneratorHELib();

//Return integer as ciphertext (App)
string returnIntEncodedAsHECiphertext(int val);

//Dump secret integer as ciphertext file.
void dumpSecretInteger(int val, string filename)
{
	ofstream out;
	string enc=returnIntEncodedAsHECiphertext(val);
	out.open(filename.c_str());
	out<<enc;
	out.close();
	secretJNIIntFileName.assign(filename);
}

/*string returnIntEncodedAsHECiphertext(int val)
{
	Ctxt c(*jni_publicKey);
	EncryptedArray ea(*jni_context, jni_G);
	NewPlaintextArray p(ea); 
	string rslt="";
	encode(ea, p, val);
        ea.encrypt(c, *jni_publicKey, p);
	ostringstream oss_encryptedDataOp;
	oss_encryptedDataOp<<c;
	rslt=oss_encryptedDataOp.str().c_str();
	return rslt;
}*/

void createKeys(char *pubkeyfilename, char *privkeyfilename)
{
	ifstream file;
	ofstream out;
	long p=101;
	long r=1;
	long L=8;
	long c=2;
	long k=80;
	long s=0;
	long d=0;
	long w=64;
	long m = FindM(k, L, c, p, d, s, 0);
	jni_context = new FHEcontext(m,p,r);
	buildModChain(*jni_context, L, c);
	jni_G = jni_context->alMod.getFactorsOverZZ()[0];
	jni_secretKey = new FHESecKey(*jni_context);
	jni_publicKey = jni_secretKey;
	jni_secretKey->GenSecKey(w); 
	addSome1DMatrices(*jni_secretKey);
	out.open(pubkeyfilename);
	out<<*jni_publicKey;
	out.close();
	out.open(privkeyfilename);
	out<<*jni_secretKey;
	out.close();
}

void setupGeneratorHELib() {
    long p=101;
    long r=1;
    long L=8;
    long c=2;
    long k=80;
    long s=0;
    long d=0;
    long m = FindM(k, L, c, p, d, s, 0);
    
    jni_context = new FHEcontext(m,p,r);
    buildModChain(*jni_context, L, c);
    jni_G = jni_context->alMod.getFactorsOverZZ()[0];

     jni_secretKey = new FHESecKey(*jni_context);
     jni_publicKey = jni_secretKey;
}

int main(int argc, char** argv) {
	if ( (argc!=4) && (argc!=6) )
	{
		printf("Usage: intcomparetest compare (num_to_compare) (encrypted int filename) (public key) (private key)\n(if creating new keys, intcomparetest generate (public key name) (private key name))\nIf creating an enciphered integer, intcomparetest encipherint (number) (encrypted int filename) (public key) (private key)\n");
		exit(1);
	}

	setupHELib();

	//Proteus only. Generate keys.
	if (argc==4)
	{
		if (strcmp(argv[1],"generate")==0)
		{
			setupGeneratorHELib();

			string pubkeyname(argv[2]);
			
			string privkeyname(argv[3]);

			printf("Performing setup...\n");

			printf("Creating keys...\n");
			
			createKeys((char *)pubkeyname.c_str(), (char *)privkeyname.c_str());

			printf("New keys created.\n");
		}
	}

	if (argc==6)
	{

		if (strcmp(argv[1],"encipherint")==0)
		{
			string intfilename(argv[3]);

			string pubkeyname(argv[4]);
			
			string privkeyname(argv[5]);

			printf("Performing setup...\n");

			setupGeneratorHELib();

			readKeys((char *)pubkeyname.c_str(), (char *)privkeyname.c_str());

			printf("Creating enciphered integer...\n");
			
			dumpSecretInteger(atoi(argv[2]), intfilename);

			printf("Enciphered integer created.\n");
		}

		if (strcmp(argv[1],"compare")==0)
		{
			string intfilename(argv[3]);

			string pubkeyname(argv[4]);
			
			string privkeyname(argv[5]);

			printf("Performing setup...\n");

			setupHELib();

			//App only
			readKeys((char *)pubkeyname.c_str(), (char *)privkeyname.c_str());
	
			//App only
			if (doComparison(returnIntEncodedAsHECiphertext(atoi(argv[2])),intfilename))
			{
				cout<<"Results match."<<endl;
			}
			else
			{
				cout<<"Results do not match."<<endl;
			}
		}
		
	}	
}
