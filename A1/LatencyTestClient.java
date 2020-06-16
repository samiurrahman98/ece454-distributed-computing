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

public class LatencyTestClient {
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
                FileWriter myWriter = new FileWriter("latencyTestClient-hash.csv");                
                FileWriter myWriter2 = new FileWriter("latencyTestClient-check.csv");
                int numPwds = 128;
                short logRounds = 10;
                List<String> passwords = randPasswords(numPwds, 1024);
                long startTime;
                long endTime;      
                myWriter.write("LogRounds, Hash-Tput, Hash-Latency" + System.lineSeparator());
                myWriter2.write("LogRounds, Check-Tput, Check-Latency" + System.lineSeparator());
                for(String pwd: passwords) {
                    List<String> pwdList = new ArrayList<String>();
                    pwdList.add(pwd);
                    startTime = System.currentTimeMillis();
                    List<String> hash = client.hashPassword(pwdList, logRounds);
                    endTime = System.currentTimeMillis();                   
                    myWriter.write(logRounds + "," + 1000f/(endTime-startTime) + "," + (endTime-startTime) + System.lineSeparator());
                    startTime = System.currentTimeMillis();
                    List<Boolean> checks = client.checkPassword(pwdList, hash);
                    endTime = System.currentTimeMillis();
                    myWriter2.write(logRounds + "," + 1000f/(endTime-startTime) + "," + (endTime-startTime) + System.lineSeparator());
                }
                myWriter.close();
                myWriter2.close();
            } catch(IOException ioe) {
                System.out.println("Couldn't write to latencyTestClient-hash.csv and/or latencyTestClient-check.csv");
            }

            transport.close();
        } catch (TException x) {
            x.printStackTrace();
        }
    }
}