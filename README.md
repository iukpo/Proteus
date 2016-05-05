# Proteus

Proteus is an anti-reverse engineering system that provides protection from disassembly and debugging for software written in Java and C/C++.

## How does it work?

First, the software author must provide "marks", comments in the code indicating where Proteus should apply a particular protection. The mark should indicate what kind of protection is desired. The mark needs to be placed directly above the line in the code that needs protection. 

Once the marks have been placed, Proteus can be run. The target source code is copied to a working folder. Proteus inserts randomly chosen opaque predicates and either dynamically created inward jumps (C/C++ only) or altered function calls (Java only) into target source code at anti-disassembly marks. These predicates provide control flow obfuscation.

Proteus anti-debugging code is designed to protect sensitive data from being visible in memory and takes the form of a two step process involving homomorphic encryption. First, the variable to be protected is marked. This anti-debug mark tells Proteus the name of the variable containing the sensitive data and the value of that variable. Proteus removes references to this variable and replaces them with references to an encrypted file containing the data. The sensitive data is then stored in the encrypted file. Then, the operation involving this variable is marked, and replaced with an equivalent function that encrypts input and uses homomorphic encryption to compare the encrypted input with the encypted file content; Because neither value is decrypted for the operation, an attacker cannot use a debugger to ascertain the sensitive data.

Once all the anti-reverse engineering code has been inserted into source, Proteus then builds the binary from the software by running the source code Makefile.

Proteus is designed to be run more than once on a target source; each time Proteus is run, it generates a binary with a different protection implementation. Such variety makes software cracking more difficult; Instead of being able to crack a software and release a patch or key generator that can be applied to all instances of a software, an attacker would have to crack each individual binary.

## Dependencies

Proteus relies on an informal "fork" of [HELib](https://github.com/shaih/HElib), an implementation of homomorphic encryption. This fork is called HELib_Proteus and is contained within this repo. This fork provides the implementation for the functions that replace the function calls that involve sensitive data. The functions are contained within a static library (FHE.a) built from the fork, and the fork provides headers for these functions.

C/C++ programs that use anti-debugging protection must link against the static library mentioned above and the GNU Multiple Precision Arithmetic Library.

## Installation

First, FHE.a must be built. To do this, simply go into the HElib_proteus/src folder and run Make. Then, navigate to the ProteusSrc folder and run make to generate the Java classes necessary for Proteus itself.

## How to use

java Proteus {number of times to run} {full path to target source code}

During the course of execution, Proteus will build and run an assitant program, intcomparetest, to create public and private keys for homomorphic encryption, as well as encrypt the sensitive data.

All generated binaries and modified source will be copied to the ProteusSrc folder.

## Present limitations/issues

Proteus can only build programs with Makefiles.

Anti-debugging protection is not available for Java programs.

Anti-debugging requires GCC as compiler.

Inward jumps are written in x86 assembler.

Only equality comparisons involving integers are supported for anti-debugging protection.

Programs utilizing anti-debugging are considerably larger and slower than their counterparts lacking such protection.

Only byte and integer types are supported for Java anti-disassembly code.

Only char, byte, and integer types are supported for C/C++ anti-debugging/anti-disassembly protection.

The public and private keys are not stored securely at the moment; While an attacker won't be able to use a debugger to obtain sensitive data, an attacker could try to guess which files are what keys, then move on to try the keys on the encrypted data.

Anti-disassembly can only be applied to assignment statements (Ex.: z=x+y)

## Credits

[HELib](https://github.com/shaih/HElib) is written by Shai Halevi.

Opaque predicates come from Douglas Low's master's thesis, Java Control Flow Obfuscation.

HECalc, a reverse Polish notation calculator that uses HELib, was an inspiration on figuring out how to use HELib. It can be obtained [here](https://code.google.com/archive/p/thep/downloads).

## License

Proteus is distributed under the terms of the [GNU General Public License] (GPL).
