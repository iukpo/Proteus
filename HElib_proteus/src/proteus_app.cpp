#include "EncryptedArray.h"
#include <NTL/lzz_pXFactoring.h>
#include <iostream>
#include <stack>
#include <string>
#include <stdlib.h>
#include "proteus_app.h"
using namespace std;
stack<Ctxt> theStack;
FHEcontext* context;
FHESecKey* secretKey;
FHEPubKey* publicKey;
ZZX G;

string pubkey="";
string privkey="";

/*If decrypted vector is equivalent to this, the difference between ciphertexts is zero, and the numbers are the same. This is hacky, but works. TODO: Replace with more robust means.*/
string emptyResult("[[] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] [] []]");

string secretIntFileName;

string loadInteger(string filename)
{
	ifstream file;
	file.open(filename.c_str());
	string secretIntegerStr;
	secretIntegerStr.assign( (std::istreambuf_iterator<char>(file) ), (std::istreambuf_iterator<char>() ) );
	return secretIntegerStr;
}

string returnIntEncodedAsHECiphertext(int val)
{
	Ctxt c(*publicKey);
	EncryptedArray ea(*context, G);
	NewPlaintextArray p(ea); 
	string rslt="";
	encode(ea, p, val);
        ea.encrypt(c, *publicKey, p);
	ostringstream oss_encryptedDataOp;
	oss_encryptedDataOp<<c;
	rslt=oss_encryptedDataOp.str().c_str();
	return rslt;
}

bool doComparison(string opEncStrOne, string optwo)
{
	EncryptedArray ea(*context, G);
	NewPlaintextArray p(ea); 
	std::ostringstream stream;

	/*Initialize ciphertext objects with same public key.*/
	Ctxt *op1=new Ctxt(*publicKey);
	Ctxt *op2=new Ctxt(*publicKey);
	istringstream strEncOne(opEncStrOne);
	
	/*Read "secret integer" from file.*/
	string encoded=loadInteger(optwo);
	istringstream strEncTwo(encoded);

	/*Write enciphered integers into ciphertext objects.*/
	strEncOne >> *op1;
	strEncTwo >> *op2;

	/*Subtract ciphertexts.*/
	(*op1) -= (*op2);

	/*Decrypt result.*/
    ea.decrypt(*op1, *secretKey, p);
	p.print(stream);

	/*If the result is an empty vector set, the numbers were the same (the difference was zero)*/
	if (stream.str().compare(emptyResult)==0)
	{
		return true;
	}
	else
	{
		return false;
	}
}

void readKeys(char *pubkeyfilename, char *privkeyfilename)
{
	ifstream file;
	file.open(pubkeyfilename);
	string pubKeyContent;
	string privKeyContent;
	pubKeyContent.assign( (std::istreambuf_iterator<char>(file) ), (std::istreambuf_iterator<char>() ) );
	istringstream iss_pubKey(pubKeyContent);
	iss_pubKey>>*publicKey;
	file.close();

	file.open(privkeyfilename);
	privKeyContent.assign( (std::istreambuf_iterator<char>(file) ), (std::istreambuf_iterator<char>() ) );
	istringstream iss_privKey(privKeyContent);
	iss_privKey>>*secretKey;
	file.close();
}


void setupHELib() {
    long p=101;
    long r=1;
    long L=8;
    long c=2;
    long k=80;
    long s=0;
    long d=0;
    long m = FindM(k, L, c, p, d, s, 0);
    
    context = new FHEcontext(m,p,r);
    buildModChain(*context, L, c);
    G = context->alMod.getFactorsOverZZ()[0];

     secretKey = new FHESecKey(*context);
     publicKey = secretKey;
}
