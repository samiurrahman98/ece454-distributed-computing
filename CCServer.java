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

		final int ASCIIENDOFTEXT = 3;
		final int ASCIIENDOFTRANSMISSION = 4;
		final int ASCIILINEFEED = 10;
		final int ASCIICARRIAGERETURN = 13;
		final int ASCIISPACE = 32;
		final int LOWERLIMIT = 48;
		final int UPPERLIMIT = 57;

		int port = Integer.parseInt(args[0]);

		ServerSocket ssock = new ServerSocket(port);
		System.out.println("listening on port " + port);
		while(true) {
			try {
				// block until connection arrives
				Socket csock = ssock.accept();

				DataInputStream din = new DataInputStream(csock.getInputStream());
				int respDataLen = din.readInt();
				byte[] bytes = new byte[respDataLen];
				din.readFully(bytes);

				// Initialize Graph
				MGraph mg = new MGraph();

				int i = 0;
				Boolean streamComplete = false;
				while (i < bytes.length) {
					
					while (bytes[i] > UPPERLIMIT || bytes[i] < LOWERLIMIT) {
						i++;
						if (i >= bytes.length) {
							streamComplete = true;
							break;
						}
					}

					if (streamComplete)
						break;

					int firstNode = 0;
					while (bytes[i] != ASCIISPACE) {
						char c = (char) bytes[i];
						firstNode = firstNode * 10 + Character.getNumericValue(c);
						i++;
					}
					i++;

					while (bytes[i] > UPPERLIMIT || bytes[i] < LOWERLIMIT)
						i++;

					int secondNode = 0;
					while (bytes[i] <= UPPERLIMIT && bytes[i] >= LOWERLIMIT) {
						char c = (char) bytes[i];
						secondNode = secondNode * 10 + Character.getNumericValue(c);
						i++;
					}
					i++;

					mg.addEdge(firstNode, secondNode);
				}

				mg.findTriangles();

				// Write graph result to the client
				DataOutputStream dout = new DataOutputStream(csock.getOutputStream());
				bytes = mg.toString().getBytes("UTF-8");
				dout.writeInt(bytes.length);
				dout.write(bytes);
				dout.flush();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    }
}