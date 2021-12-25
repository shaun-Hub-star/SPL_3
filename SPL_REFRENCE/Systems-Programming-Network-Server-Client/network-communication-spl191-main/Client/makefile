CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LFLAGS  = -L/usr/lib -lboost_system -lpthread


all: BGSclient
	g++ -o bin/BGSclient bin/connectionHandler.o bin/BGSclient.o $(LFLAGS)

	
BGSclient: bin/connectionHandler.o bin/BGSclient.o 
	
	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

	
bin/BGSclient.o: src/BGSclient.cpp
	g++ $(CFLAGS) -o bin/BGSclient.o src/BGSclient.cpp
	
	
	
	
.PHONY: clean
clean:
	rm -f bin/*
