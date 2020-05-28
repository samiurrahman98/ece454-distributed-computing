import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.net.*;
import DFS;

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

				DataInputStredfs din = new DataInputStredfs(csock.getInputStredfs());
				int respDataLen = din.readInt();
				System.out.println("received response header, data payload has length " + respDataLen);
				byte[] bytes = new byte[respDataLen];
				din.readFully(bytes);

				// Initialize Graph
				DFS dfs = new DFS();
				System.out.println("Initialized adjacency matrix.");
				
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

					int secondNode = 0;
					while (bytes[i] != ASCIILINEFEED) {
						char c = (char) bytes[i];
						secondNode = secondNode * 10 + Character.getNumericValue(c);
						i++;
					}
					i++;

					dfs.addEdge(firstNode, secondNode);
				}

				// dfs.findTriangles();

				System.out.println("Output: ");
				dfs.toString();
				// System.out.println(dfs.toString());

				// Write graph result to the client
				// DataOutputStredfs dout = new DataOutputStredfs(csock.getOutputStredfs());
				// bytes = cg.toString().getBytes("UTF-8");
				// dout.writeInt(bytes.length);
				// dout.write(bytes);
				// dout.flush();
				// System.out.println("sent result header and " + bytes.length + " bytes of payload data to Client");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    }
}
