import java.util.*;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TTransportFactory;

import java.lang.IllegalArgumentException;

public class IllegalArgumentTest {    
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
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

            List<String> passwords = new ArrayList<String>();
            List<String> hash = new ArrayList<String>();
            List<Boolean> checks = new ArrayList<Boolean>();
            short logRounds = 10;
            // empty values in passwords list
            try {
                hash = client.hashPassword(passwords, logRounds);
                System.out.println("no exception1a");
            } catch (IllegalArgument ill) {
                System.out.println("Empty values in passwords to hashPassword - IllegalArgumentException was thrown as expected!");
            }
            try {
                checks = client.checkPassword(passwords, hash);
                System.out.println("no exception1b");
            } catch (IllegalArgument ill) {
                System.out.println("Empty values in passwords to checkPassword - IllegalArgumentException was thrown as expected!");
            }
            try {
                checks = client.checkPassword(passwords, hash);
                System.out.println("no exception1c");
            } catch (IllegalArgument ill) {
                System.out.println("Empty values in hash to checkPassword - IllegalArgumentException was thrown as expected!");
            }
            passwords = randPasswords(10, 200);
            hash = client.hashPassword(passwords, logRounds);
            // unequal passwords or hash length
            passwords.add("Hello");
            try {
                checks = client.checkPassword(passwords, hash);
                System.out.println("no exception2a");
            } catch (IllegalArgument ill) {
                
                System.out.println("Passwords.length > Hash.length - IllegalArgumentException was thrown as expected!");
            }
            hash.add("HelloSir");
            hash.add("Goodbye");
            try {
                checks = client.checkPassword(passwords, hash);
                System.out.println("no exception2b");
            } catch (IllegalArgument ill) {                
                System.out.println("Hash.length > Passwords.length - IllegalArgumentException was thrown as expected!");
            }
            // logRounds argument out of range
            try{
                hash = client.hashPassword(passwords, (short)2);
                   System.out.println("no exception3");
            } catch (IllegalArgument ill) {
                System.out.println("logRounds argument out of range - IllegalArgumentException was thrown as expected!");
            }            
            transport.close();
        } catch (TException x) {
            System.out.println("TException is being thrown...");
            x.printStackTrace();
        }
    }
}