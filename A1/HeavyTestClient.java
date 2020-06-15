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

public class HeavyTestClient {
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
        try {
            TSocket sock = new TSocket(args[0], Integer.parseInt(args[1]));
            TTransport transport = new TFramedTransport(sock);
            TProtocol protocol = new TBinaryProtocol(transport);
            BcryptService.Client client = new BcryptService.Client(protocol);
            transport.open();

            try {
                FileWriter myWriter = new FileWriter("heavyTestClient.csv");
                myWriter.write("Hash+Check password results for 128, 1024 character passwords\n");
                int numPwds = 128;
                List<String> password = randPasswords(numPwds, 1024);
                long startTime = System.currentTimeMillis();
                for(short logRounds = 8; logRounds <=12; logRounds++){                    
                    List<String> hash = client.hashPassword(password, logRounds);
                    long endTime = System.currentTimeMillis();
                    myWriter.write("LogRounds, Hash-Tput, Hash-Latency" + System.lineSeparator());
                    myWriter.write(logRounds + "," + numPwds * 1000f/(endTime-startTime) + "," + (endTime-startTime)/numPwds + System.lineSeparator());
                    startTime = System.currentTimeMillis();               
                    List<Boolean> checks = client.checkPassword(password, hash);
                    endTime = System.currentTimeMillis();
                    myWriter.write("LogRounds, Check-Tput, Check-Latency" + System.lineSeparator());
                    myWriter.write(logRounds + "," + numPwds * 1000f/(endTime-startTime) + "," + (endTime-startTime)/numPwds + System.lineSeparator());
                }
                myWriter.close();
            } catch(IOException ioe) {
                System.out.println("Couldn't write to heavyTestClient.csv");
            }

            transport.close();
        } catch (TException x) {
            x.printStackTrace();
        }
    }
}