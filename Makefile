all: compile

compile:
	javac MultithreadedChatServer.java
	javac ChatClient.java

clean:
	rm *.class
