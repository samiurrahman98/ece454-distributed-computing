import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import java.io.FileWriter;
import java.io.IOException;

public class MultiThreadClient{

    private static ExecutorService execService = Executors.newCachedThreadPool(new ThreadFactory() {
        private AtomicInteger threadCounter = new AtomicInteger(0);

        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(false);
            t.setPriority(Thread.NORM_PRIORITY);
            t.setName("client-thread-" + threadCounter.incrementAndGet());
            return t;
        }
    });

    public static void main(String [] args) {
        BasicClient bClient = new BasicClient (args[0], args[1]);
        for (int i = 0; i < 7; i++) {            
            execService.submit(bClient);
        }
    }
}

class BasicClient implements Runnable{
    private String hostName;
    private String port;
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
    
    public BasicClient(String hostName, String port) {
        this.hostName = hostName;
        this.port = port;
    }

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

    public void run() {
        // for (int i = 0; i < 30; i++) {
            try {
                TSocket sock = new TSocket(hostName, Integer.parseInt(port));
                TTransport transport = new TFramedTransport(sock);
                TProtocol protocol = new TBinaryProtocol(transport);
                BcryptService.Client client = new BcryptService.Client(protocol);
                transport.open();

                try {
                    List<String> passwords = randPasswords(128, 1024);
                    long startTime = System.currentTimeMillis();
                    List<String> hash = client.hashPassword(passwords, (short)10);
                    long endTime = System.currentTimeMillis();
                    // DELETE CONTENTS OF CSV FILE BEFORE RUNNING THIS CODE AGAIN
                    FileWriter myWriter = new FileWriter("MultiThreadClient4.csv", true);
                    myWriter.write("Number of BE nodes: 4\n");
                    myWriter.write("Number of 1024 char pwds, Total time\n");
                    myWriter.write("Hash size: " + passwords.size() + " took: " + (endTime - startTime) + System.lineSeparator());

                    startTime = System.currentTimeMillis();
                    List<Boolean> results = client.checkPassword(passwords, hash);
                    endTime = System.currentTimeMillis();
                    Boolean valid = true;
                    for (Boolean val: results) {
                        valid = valid && val;
                    }
                    myWriter.write("Check size: " + passwords.size() + " " +  valid + " took:" + (endTime - startTime) + System.lineSeparator());
                    myWriter.close();

                } catch(IOException ioe) {
                    System.out.println("Couldn't write to MultiThreadClient.csv");
                }


                transport.close();
            } catch (TException x) {
                System.out.println(x.getMessage());
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ex) {
                // do nothing
            }
        // }
    }
}