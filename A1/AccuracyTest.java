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
import java.lang.IllegalArgumentException;

public class AccuracyTest{

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
        AccuracyClient aClient = new AccuracyClient (args[0], args[1]);
        for (int i = 0; i < 5; i++) {            
            execService.submit(aClient);
        }
    }
}

class AccuracyClient implements Runnable{
    private String hostName;
    private String port;
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
    
    public AccuracyClient(String hostName, String port) {
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
                    FileWriter myWriter = new FileWriter("AccuracyTest.csv", true);
                    int numPwds = 10;
                    List<String> passwords = randPasswords(numPwds, 1024);     
                    myWriter.write("Thread, LogRounds, Hash-Tput, Hash-Latency" + System.lineSeparator());
                    for(short logRounds = 8; logRounds <= 12; logRounds++) {
                        List<String> hash = client.hashPassword(passwords, logRounds);
                        for(int i = 0; i < hash.size(); i++) {
                            if (i % 2 == 0) {
                                hash.set(i, "HELLO");
                            }
                        }
                        List<Boolean> checks = client.checkPassword(passwords, hash);
                        myWriter.write(Thread.currentThread().getName() + "," + logRounds + System.lineSeparator());                       
                        myWriter.write("Odd/Even -ve/+ve check: " + checks + System.lineSeparator());
                        // Empty test
                        try{                            
                            hash = client.hashPassword(passwords, (short)10);
                            passwords.clear();                        
                            checks = client.checkPassword(passwords, hash);
                            hash.clear();
                            myWriter.write("No Illegal Argument, all successful!\n");
                        } catch (IllegalArgument ill) {
                            myWriter.write("Illegal Argument thrown!\n");
                        }                   
                        myWriter.close();
                    }
                } catch(IOException ioe) {
                    System.out.println("Couldn't write to AccuracyTest.csv");
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