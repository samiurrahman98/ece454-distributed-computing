import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.net.*;

class CCServer {
    public static void main(String args[]) throws Exception {
	if (args.length != 1) {
	    System.out.println("usage: java CCServer port");
	    System.exit(-1);
	}
	int port = Integer.parseInt(args[0]);

	ServerSocket ssock = new ServerSocket(port);
	System.out.println("listening on port " + port);
	while(true) {
	    try {
		/*
		  YOUR CODE GOES HERE
		  - accept connection from server socket
		  - read requests from connection repeatedly
		  - for each request, compute an output and send a response
		  - each message has a 4-byte header followed by a payload
		  - the header is the length of the payload
		    (signed, two's complement, big-endian)
		  - the payload is a string
		    (UTF-8, big-endian)
		*/
		Socket clientSock = ssock.accept();
		System.out.println("Connected to client");
		DataInputStream din = new DataInputStream(clientSock.getInputStream());
		int reqDataLen = din.readInt();
		System.out.println("received response header, data payload has length " + reqDataLen);
		byte[] bytes = new byte[reqDataLen];
		din.readFully(bytes);
		
		System.out.println("received " + bytes.length + " bytes of payload from client.");
		System.out.println(new String(bytes, StandardCharsets.UTF_8));
		
		// while (dataIn.available() > 0) {
		// 	String k = dataIn.readUTF();
		// 	System.out.println(k+" ");
		// }

	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }
}
