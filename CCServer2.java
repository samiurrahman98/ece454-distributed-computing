import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.net.*;
import com.google.common.graph.EndpointPair;

class CCServer2 {
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
				// block until connection arrives
				Socket csock = ssock.accept();

				// System.out.println("Accepted client: " + csock);

				DataInputStream din = new DataInputStream(csock.getInputStream());
				int respDataLen = din.readInt();
				System.out.println("received response header, data payload has length " + respDataLen);
				byte[] bytes = new byte[respDataLen];
				din.readFully(bytes);

				MGraph2 mg = new MGraph2();
				
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

					mg.addEdge(firstNode, secondNode);
				}
				
				for(EndpointPair<Integer> edge: mg.mGraph.edges()) {
					mg.findTriangles(edge);
				}
				// mg.findTriangles();

				// Write graph result to the client
				DataOutputStream dout = new DataOutputStream(csock.getOutputStream());
				bytes = mg.toString().getBytes("UTF-8");
				dout.writeInt(bytes.length);
				dout.write(bytes);
				dout.flush();
				System.out.println("sent result header and " + bytes.length + " bytes of payload data to Client");		

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    }
}