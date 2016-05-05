CC = g++
CFLAGS = -g -O2 -Wfatal-errors -Wshadow -Wall -I/usr/local/include
GMP = -lgmp
HEdir = ../../HElib_proteus/src
HElib = $(HEdir)/fhe.a

LDLIBS = -L/usr/local/lib $(HElib) -lntl $(GMP) -lm
INCDIR = -I$(HEdir)

asciidraw: AsciiDraw.cpp
	$(CC) $(CFLAGS) -o $@ $(INCDIR) $< $(LDLIBS)

clean:
	rm -f asciidraw
	rm -f *.o
