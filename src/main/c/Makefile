TARGET = libzeromq_req.so

OS := $(shell uname)
CC=gcc

CLIB = -L. \
       -lc -lm -lzmq

CINCLUDE = -I. \
		 -I/usr/local/include \
		 -I/opt/jdk/include/ \
		 -I/opt/jdk/include/linux/

DEBUG = -g -O2

CFLAGS = -w -g -pedantic -Wextra -Wno-unused-parameter \
	     -Wall -Wextra -Wno-unused-function -fPIC -std=gnu99 \
	     $(DEBUG) \
	     $(CINCLUDE)

LFLAGS = -Wall $(DEBUG) $(CLIB)

all : $(TARGET)

$(TARGET) : zeromq_req.o
	$(CC) -shared -fPIC $(LFLAGS) -o $(TARGET) $<

zeromq_req.o : zeromq_req.c
	$(CC) $(CFLAGS) -c $< -o $@

clean :
	rm -f *.o
	rm -f ${TARGET}