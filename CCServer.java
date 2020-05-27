import java.io.*;
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

				// block until connection arrives
				Socket csock = ssock.accept();

				System.out.println("Accepted client: " + csock);

				DataInputStream din = new DataInputStream(csock.getInputStream());
				int respDataLen = din.readInt();
				System.out.println("received response header, data payload has length " + respDataLen);
				byte[] bytes = new byte[respDataLen];
				din.readFully(bytes);

				// Initialize Graph
				Graph g = new Graph();
				System.out.println("Initialized graph.");
				
				int i = 0;
				while (i < bytes.length) {
					final int ASCIISPACE = 32;
					final int ASCIILINEFEED = 10;

					int firstNode = 0;
					while (bytes[i] != ASCIISPACE) {
						char c = (char) bytes[i];
						firstNode = firstNode * 10 + Character.getNumericValue(c);
						i++;
					}
					i++;
					System.out.println("First Node: " + firstNode);

					int secondNode = 0;
					while (bytes[i] != ASCIILINEFEED) {
						char c = (char) bytes[i];
						secondNode = secondNode * 10 + Character.getNumericValue(c);
						i++;
					}
					i++;
					System.out.println("Second Node: " + secondNode);

					g.construct(firstNode, secondNode);
					System.out.println("Output: " + g.toString());
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    }
}
