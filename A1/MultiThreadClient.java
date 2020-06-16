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
        for (int i = 0; i < 20; i++) {            
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
                    FileWriter myWriter = new FileWriter("MultiThreadClient-hash.csv", true);                
                    FileWriter myWriter2 = new FileWriter("MultiThreadClient-check.csv", true);
                    int numPwds = 128;
                    List<String> passwords = randPasswords(numPwds, 1024);
                    long startTime;
                    long endTime;      
                    myWriter.write("Thread, LogRounds, Hash-Tput, Hash-Latency" + System.lineSeparator());
                    myWriter2.write("Thread, LogRounds, Check-Tput, Check-Latency" + System.lineSeparator());
                    for(short logRounds = 4; logRounds <= 16; logRounds++) {
                        startTime = System.currentTimeMillis();
                        List<String> hash = client.hashPassword(passwords, logRounds);
                        endTime = System.currentTimeMillis();                   
                        myWriter.write(Thread.currentThread().getName() + "," + logRounds + "," + numPwds*1000f/(endTime-startTime) + "," + ((endTime-startTime)/numPwds) + System.lineSeparator());
                        startTime = System.currentTimeMillis();
                        List<Boolean> checks = client.checkPassword(passwords, hash);
                        endTime = System.currentTimeMillis();
                        myWriter2.write(Thread.currentThread().getName() + "," + logRounds + "," + numPwds*1000f/(endTime-startTime) + "," + ((endTime-startTime)/numPwds) + System.lineSeparator());
                    }
                    myWriter.close();
                    myWriter2.close();
                } catch(IOException ioe) {
                    System.out.println("Couldn't write to MultiThreadClient-hash.csv and/or MultiThreadClient-check.csv");
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