import java.util.*;
import java.util.concurrent.*;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TTransportFactory;

import java.io.FileWriter;
import java.io.IOException;

public class TestClient {
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
    private static final int MaxRoundValue = 4096;

    public static String randPwdGen(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randValueIndex = (int) (Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(randValueIndex));
        }
        return builder.toString();
    }

    public static List<String> randPasswords(int numPwds, int length) {
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < numPwds; i++) {
            result.add(randPwdGen(length));
        }
        return result;
    }

    public static void main(String [] args) {
		if (args.length != 3) {
			System.err.println("Usage: java Client FE_host FE_port password");
			System.exit(-1);
		}

		try {
			TSocket sock = new TSocket(args[0], Integer.parseInt(args[1]));
			TTransport transport = new TFramedTransport(sock);
			TProtocol protocol = new TBinaryProtocol(transport);
			BcryptService.Client client = new BcryptService.Client(protocol);
			transport.open();

			try {
                FileWriter myWriter = new FileWriter("testClient.csv");
                FileWriter myWriter2 = new FileWriter("testClient2.csv");
                FileWriter myWriter3 = new FileWriter("testClient3.csv");
                myWriter.write("LogRounds, Hash-Tput, Hash-Latency" + System.lineSeparator());
                myWriter2.write("LogRounds, Check-Tput-Positive, Check-Latency-Positive" + System.lineSeparator());                
                myWriter3.write("LogRounds, Check-Tput-Negative, Check-Latency-Negative" + System.lineSeparator());
                int numRounds = 256;
                long startTime;
                long endTime;
                for (short s = 8; s <= 12; s++) {
                    int n = MaxRoundValue/numRounds;
                    List<String> password = randPasswords(n, 1024);
                    numRounds *= 2;
                    startTime = System.currentTimeMillis();
                    List<String> hash = client.hashPassword(password, (short)10);
                    endTime = System.currentTimeMillis();
                    myWriter.write(s + "," + n * 1000f/(endTime-startTime) + "," + (endTime-startTime)/n + System.lineSeparator());
                    startTime = System.currentTimeMillis();
                    List<Boolean> check = client.checkPassword(password, hash);
                    System.out.println("Positive check: " + client.checkPassword(password, hash));
                    endTime = System.currentTimeMillis();
                    myWriter2.write(s + "," + n * 1000f/(endTime-startTime) + "," + (endTime-startTime)/n + System.lineSeparator());
                    for (int i = 0; i < hash.size(); i++) {
			    	    hash.set(i, "$2a$14$reBHJvwbb0UWqJHLyPTVF.6Ld5sFRirZx/bXMeMmeurJledKYdZmG");
			        }
                    startTime = System.currentTimeMillis();
                    check = client.checkPassword(password, hash);
                    endTime = System.currentTimeMillis();
                    System.out.println("Negative check: " + client.checkPassword(password, hash));
                    myWriter3.write(s + "," + n * 1000f/(endTime-startTime) + "," + (endTime-startTime)/n + System.lineSeparator());
                }            
                myWriter.close();
                myWriter2.close();
                myWriter3.close();
                System.out.println("Test complete!");
            } catch(IOException ioe) {
                System.out.println("Couldn't write to testClient.csv due to IO Exception");
            }
			transport.close();
		} catch (TException x) {
			x.printStackTrace();
		} 
    }
}

// public class TestClient {
//     public static void main(String [] args) {
//         try {
//             String plaintextPassword = "ABCDEFG";

//             TSocket sock = new TSocket(args[0], Integer.parseInt(args[1]));
//             TTransport transport = new TFramedTransport(sock);
//             TProtocol protocol = new TBinaryProtocol(transport);
//             BcryptService.Client client = new BcryptService.Client(protocol);
//             transport.open();

//             List<String> password = genPasswordList();
//             List<String> hash = client.hashPassword(password, (short)10);
//             System.out.println("Check: " + client.checkPassword(password, hash));

//             transport.close();
//         } catch (TException x) {
//             x.printStackTrace();
//         }
//     }

//     public static List<String> genPasswordList() {
//         List<String> l = new ArrayList<String>(1024);
//         String somebigpassword = "faldskfjalkdsjfalkfdjasfoeiurqoeueoirqueroqiewurewqodfnmdsalkfjdsalkfjaslkfajflasdjfadslfkajdflkjfdalkadfjlkdfjfadsflkjafaldskfjalkdsjfalkfdjasfoeiurqoeueoirqueroqiewurewqodfnmdsalkfjdsalkfjaslkfajflasdjfadslfkajdflkjfdalkadfjlkdfjfadsflkjafaldskfjalkdsjfalkfdjasfoeiurqoeueoirqueroqiewurewqodfnmdsalkfjdsalkfjaslkfajflasdjfadslfkajdflkjfdalkadfjlkdfjfadsflkjafaldskfjalkdsjfalkfdjasfoeiurqoeueoirqueroqiewurewqodfnmdsalkfjdsalkfjaslkfajflasdjfadslfkajdflkjfdalkadfjlkdfjfadsflkjafaldskfjalkdsjfalkfdjasfoeiurqoeueoirqueroqiewurewqodfnmdsalkfjdsalkfjaslkfajflasdjfadslfkajdflkjfdalkadfjlkdfjfadsflkjafaldskfjalkdsjfalkfdjasfoeiurqoeueoirqueroqiewurewqodfnmdsalkfjdsalkfjaslkfajflasdjfadslfkajdflkjfdalkadfjlkdfjfadsflkjafaldskfjalkdsjfalkfdjasfoeiurqoeueoirqueroqiewurewqodfnmdsalkfjdsalkfjaslkfajflasdjfadslfkajdflkjfdalkadfjlkdfjfadsflkjafaldskfjalkdsjfalkfdjasfoeiurqoeueoirqueroqiewurewqodfnmdsalkfjdsalkfjaslkfajflasdjfadslfkajdflkjfdalkadfjlkdfjfadsflkjafaldskfjalkdsjfalkfdjasfoeiurqoeueoirqueroqiewurvcvmvcmdoiZZ";
//         for (int i = 0; i < 100; i++) {
//             l.add(somebigpassword + i);
//         }
//         return l;
//     }
// }