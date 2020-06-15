import java.util.*;
import java.time.Instant;
import java.time.Duration;
import java.util.concurrent.*;
import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.thrift.transport.TSocket;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;

import org.mindrot.jbcrypt.BCrypt;
public class BcryptServiceHandler implements BcryptService.Iface {

    private boolean isBENode;
    private final ExecutorService service = Executors.newFixedThreadPool(2);

    public BcryptServiceHandler(boolean isBENode){
        this.isBENode = isBENode;
    }

    public List<String> hashPassword(List<String> passwords, short logRounds) throws IllegalArgument, org.apache.thrift.TException
    {
        if (passwords.size() == 0) throw new IllegalArgument("Cannot have empty password list");
        if (logRounds < 4 || logRounds > 16) throw new IllegalArgument("logRounds parameter must be between 4 and 16");

        TTransport transport = null;
        String[] res = new String[passwords.size()];

        if (isBENode) {
            System.out.println("BE Node: attempting to hash password");
            Tracker.receivedBatch();
            try {
                int size = passwords.size();
                int numThreads = size < 4 ? size : 4;
                int chunkSize = size / numThreads;
                CountDownLatch latch = new CountDownLatch(numThreads);
                if (size > 1) {
                    for (int i = 0; i < numThreads; i++) {
                        int start = i * chunkSize;
                        int end = i == numThreads - 1 ? size : (i + 1) * chunkSize;
                        service.execute(new MultiThreadHash(passwords, logRounds, res, start, end, latch));
                    }
                    latch.await();
                } else
					hashPassword(passwords, logRounds, res, 0, passwords.size());

                Tracker.receivedBatch();
                return Arrays.asList(res);
            } catch (Exception e) {
                throw new IllegalArgument(e.getMessage());
            }
        } else {
            System.out.println("FE Node: attempting to offload hash password operation to the BE Node.");
            NodeProperties nodeProperties = NodeManager.getAvailableNodeProperties();
            System.out.println("node properties: " + nodeProperties);
            while (nodeProperties != null) {
                System.out.println("Node Properties is not null!");
                BcryptService.Client client = nodeProperties.getClient();
                transport = nodeProperties.getTransport();
                try {
                    if (!transport.isOpen()) transport.open();
                    nodeProperties.addLoad(passwords.size(), logRounds);
                    List<String> BEResult = client.hashPassword(passwords, logRounds);
                    nodeProperties.reduceLoad(passwords.size(), logRounds);
                    nodeProperties.markFree();
                    System.out.println("FE Node: successfully offloaded hash password operation to the BE Node.");
                    return BEResult;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    NodeManager.removeNode(nodeProperties.nodeId);
                    System.out.println("BENode at " + nodeProperties.nodeId + " is dead :( Removing from NodeManager");
                    nodeProperties = NodeManager.getAvailableNodeProperties();
                } finally {
                    if (transport != null && transport.isOpen()) transport.close();
                }
            }

            System.out.println("FE Node: failed to offload hash password operation to a BE Node. Starting operation with FE Node.");
            try {
                hashPassword(passwords, logRounds, res, 0, passwords.size());
                return Arrays.asList(res);
            } catch (Exception ex) {
                throw new IllegalArgument(ex.getMessage());
            }
        }
    }

    public List<Boolean> checkPassword(List<String> passwords, List<String> hashes) throws IllegalArgument, org.apache.thrift.TException
    {
        TTransport transport = null;
        Boolean[] res = new Boolean[passwords.size()];

        if (isBENode) {
            System.out.println("BE Node: attempting to check password");
            Tracker.receivedBatch();
            try {
                if (passwords.size() != hashes.size()) throw new Exception("passwords and hashes are not equal.");
                if (passwords.size() == 0) throw new Exception(("password list cannot be empty"));

                int size = passwords.size();
                int numThreads = Math.min(size, 4);
                int chunkSize = size / numThreads;
                CountDownLatch latch = new CountDownLatch(numThreads);
                if (size > 1) {
                    for (int i = 0; i < numThreads; i++) {
                        int startInd = i * chunkSize;
                        int endInd = i == numThreads - 1 ? size : (i + 1) * chunkSize;
                        service.execute(new MultiThreadCheck(passwords, hashes, res, startInd, endInd, latch));
                    }
                    latch.await();
                } else {
                    checkPassword(passwords, hashes, res, 0, passwords.size());
                }
                return Arrays.asList(res);
            } catch (Exception e) {
                throw new IllegalArgument(e.getMessage());
            }
        } else {
            NodeProperties nodeProperties = NodeManager.getAvailableNodeProperties();
            System.out.println("FE Node: attempting to offload check password operation to the BE Node.");
            while (nodeProperties != null) {
                System.out.println("Node Properties is not null!");
                BcryptService.Client client = nodeProperties.getClient();
                transport = nodeProperties.getTransport();
                System.out.println("moving work over to the back end node: " + nodeProperties.nodeId);
                try {
                    if (!transport.isOpen()) transport.open();
                    nodeProperties.addLoad(passwords.size(), (short)0);
                    List<Boolean> BEResult = client.checkPassword(passwords, hashes);
                    nodeProperties.reduceLoad(passwords.size(), (short)0);
                    nodeProperties.markFree();
                    System.out.println("FE Node: successfully offloaded check password operation to the BE Node.");
                    return BEResult;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    NodeManager.removeNode(nodeProperties.nodeId);
                    nodeProperties = NodeManager.getAvailableNodeProperties();
                } finally {
                    if (transport != null && transport.isOpen()) transport.close();
                }
            }
            System.out.println("FE Node: failed to offload check password operation to a BE Node. Starting operation with FE Node.");
            try {
                checkPassword(passwords, hashes, res, 0, passwords.size());
                return Arrays.asList(res);
            } catch (Exception e) {
                throw new IllegalArgument(e.getMessage());
            }
        }
    }

    private void checkPassword(List<String> passwords, List<String> hashes, Boolean[] res, int start, int end) {
        for (int i = start; i < end; i++) {
            try {
                res[i] = (BCrypt.checkpw(passwords.get(i), hashes.get(i)));
            } catch (Exception e) {
                res[i] = false;
            }
        }
    }
    
    public void heartBeat(String hostname, String port) throws IllegalArgument, org.apache.thrift.TException {
        System.out.println("Received heartbeat from host: " + hostname + ", port: " + port);
		try {
			String nodeId = hostname + port;
			if (!NodeManager.containsNode(nodeId)) {
				NodeProperties nodeProperties = new NodeProperties(hostname, port);
				NodeManager.addNode(nodeId, nodeProperties);
			}
		} catch (Exception e) {
			throw new IllegalArgument(e.getMessage());
		}
    }

    private void hashPassword(List<String> passwords, short logRounds, String[] res, int start, int end ) {
        for (int i = start; i < end; i++)
            res[i] = BCrypt.hashpw(passwords.get(i), BCrypt.gensalt(logRounds));
    }

public class MultiThreadCheck implements Runnable {
    private List<String> passwords;
    private List<String> hashes;
    private Boolean[] res;
    int start;
    int end;
    CountDownLatch latch;

    public MultiThreadCheck(List<String> passwords, List<String> hashes, Boolean[] res, int start, int end, CountDownLatch latch) {
        this.passwords = passwords;
        this.hashes = hashes;
        this.res = res;
        this.start = start;
        this.end = end;
        this.latch = latch;
    }

    @Override
    public void run() {
        checkPassword(passwords, hashes, res, start, end);
        latch.countDown();
    }
}

public class MultiThreadHash implements Runnable {
    private List<String> passwords;
    private short logRounds;
    private String[] res;
    private int start;
    private int end;
    private CountDownLatch latch;

    public MultiThreadHash(List<String> passwords, short logRounds, String[] res, int start, int end, CountDownLatch latch) {
        this.logRounds = logRounds;
        this.passwords = passwords;
        this.res = res;
        this.start = start;
        this.end = end;
        this.latch = latch;
    }

    @Override
    public void run() {
        hashPassword(passwords, logRounds, res, start, end);
        latch.countDown();
    }
}


}