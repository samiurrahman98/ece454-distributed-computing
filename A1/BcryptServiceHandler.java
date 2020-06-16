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

    /* 
    ** Function: hashPassword
    ** Purpose: offload crypto hash computation to a BE node if available, otherwise get FE node to do instead
    ** Parameters: 
    ** -- List<String> passwords -- list of passwords to hash
    ** -- short logRounds -- number of log rounds
    ** Returns: list of hashed passwords
    */
    public List<String> hashPassword(List<String> passwords, short logRounds) throws IllegalArgument, org.apache.thrift.TException
    {
        if (passwords.size() == 0) throw new IllegalArgument("hashPassword: cannot have empty password list");
        if (logRounds < 4 || logRounds > 16) throw new IllegalArgument("hashPassword: logRounds parameter must be between 4 and 16");

        TTransport transport = null;
        String[] result = new String[passwords.size()];

        if (isBENode) {
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
                        service.execute(new MultithreadHash(passwords, logRounds, result, start, end, latch));
                    }
                    latch.await();
                } else
					hashPassword(passwords, logRounds, result, 0, passwords.size());

                Tracker.receivedBatch();

                return Arrays.asList(result);
            } catch (Exception e) {
                throw new IllegalArgument(e.getMessage());
            }
        } else {
            NodeProperties nodeProperties = NodeManager.getAvailableNodeProperties();
            while (nodeProperties != null) {
                BcryptService.Client client = nodeProperties.getClient();
                transport = nodeProperties.getTransport();
                try {
                    if (!transport.isOpen()) transport.open();

                    nodeProperties.addLoad(passwords.size(), logRounds);
                    List<String> BEResult = client.hashPassword(passwords, logRounds);
                    nodeProperties.reduceLoad(passwords.size(), logRounds);
                    nodeProperties.markFree();

                    return BEResult;
                } catch (Exception e) {
                    e.printStackTrace();
                    NodeManager.removeNode(nodeProperties.nodeId);
                    nodeProperties = NodeManager.getAvailableNodeProperties();
                } finally {
                    if (transport != null && transport.isOpen()) transport.close();
                }
            }

            try {
                hashPassword(passwords, logRounds, result, 0, passwords.size());

                return Arrays.asList(result);
            } catch (Exception ex) {
                throw new IllegalArgument(ex.getMessage());
            }
        }
    }

    /* 
    ** Function: checkPassword
    ** Purpose: offload crypto check computation to a BE node if available, otherwise get FE node to do instead
    ** Parameters: 
    ** -- List<String> passwords -- list of passwords
    ** -- List<String> hashes -- list of hashed passwords
    ** Returns: list of results confirming whether the input lists are equal
    */
    public List<Boolean> checkPassword(List<String> passwords, List<String> hashes) throws IllegalArgument, org.apache.thrift.TException
    {
        if (passwords.size() != hashes.size()) throw new IllegalArgument("checkPassword: passwords list and hashes list are not equal in length");
        if (passwords.size() == 0) throw new IllegalArgument(("checkPassword: passwords list cannot be empty"));
        if (hashes.size() == 0) throw new IllegalArgument(("checkPassword: hashes list cannot be empty"));


        TTransport transport = null;
        Boolean[] result = new Boolean[passwords.size()];

        if (isBENode) {
            Tracker.receivedBatch();
            try {
                int size = passwords.size();
                int numThreads = Math.min(size, 4);
                int chunkSize = size / numThreads;
                CountDownLatch latch = new CountDownLatch(numThreads);
                if (size > 1) {
                    for (int i = 0; i < numThreads; i++) {
                        int startInd = i * chunkSize;
                        int endInd = i == numThreads - 1 ? size : (i + 1) * chunkSize;
                        service.execute(new MultithreadCheck(passwords, hashes, result, startInd, endInd, latch));
                    }
                    latch.await();
                } else
                    checkPassword(passwords, hashes, result, 0, passwords.size());

                return Arrays.asList(result);
            } catch (Exception e) {
                throw new IllegalArgument(e.getMessage());
            }
        } else {
            NodeProperties nodeProperties = NodeManager.getAvailableNodeProperties();
            while (nodeProperties != null) {
                BcryptService.Client client = nodeProperties.getClient();
                transport = nodeProperties.getTransport();
                try {
                    if (!transport.isOpen()) transport.open();

                    nodeProperties.addLoad(passwords.size(), (short)0);
                    List<Boolean> BEResult = client.checkPassword(passwords, hashes);
                    nodeProperties.reduceLoad(passwords.size(), (short)0);
                    nodeProperties.markFree();

                    return BEResult;
                } catch (Exception e) {
                    e.printStackTrace();
                    NodeManager.removeNode(nodeProperties.nodeId);
                    nodeProperties = NodeManager.getAvailableNodeProperties();
                } finally {
                    if (transport != null && transport.isOpen()) transport.close();
                }
            }
            try {
                checkPassword(passwords, hashes, result, 0, passwords.size());

                return Arrays.asList(result);
            } catch (Exception e) {
                throw new IllegalArgument(e.getMessage());
            }
        }
    }

    /* 
    ** Function: hashPassword
    ** Purpose: utility function to invoke BCryptService hashpw function
    ** Parameters: 
    ** -- List<String> passwords -- list of passwords
    ** -- short logRounds -- number of log rounds
    ** -- String[] result -- result object
    ** -- int start -- chunk starting index
    ** -- int end -- chunk ending index
    ** Returns: void; mutates input parameter [result]
    */
    private void hashPassword(List<String> passwords, short logRounds, String[] result, int start, int end ) {
        for (int i = start; i < end; i++)
            result[i] = BCrypt.hashpw(passwords.get(i), BCrypt.gensalt(logRounds));
    }

    /* 
    ** Function: checkPassword
    ** Purpose: utility function to invoke BCryptService checkpw function
    ** Parameters: 
    ** -- List<String> passwords -- list of passwords
    ** -- List<String> hashes -- list of hashed passwords
    ** -- String[] result -- result object
    ** -- int start -- chunk starting index
    ** -- int end -- chunk ending index
    ** Returns: void; mutates input parameter [result]
    */
    private void checkPassword(List<String> passwords, List<String> hashes, Boolean[] result, int start, int end) {
        for (int i = start; i < end; i++) {
            try {
                result[i] = (BCrypt.checkpw(passwords.get(i), hashes.get(i)));
            } catch (Exception e) {
                result[i] = false;
            }
        }
    }
    
    /* 
    ** Function: heartBeat
    ** Purpose: establish connection from BE node to FE node
    ** Parameters: 
    ** -- String hostname -- name of host (localhost, eceubuntu, ecetesla, etc.)
    ** -- String port -- port number
    ** Returns: void; mutates data structure controlled by the NodeManager
    */
    public void heartBeat(String hostname, String port) throws IllegalArgument, org.apache.thrift.TException {
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

    public class MultithreadCheck implements Runnable {
        private List<String> passwords;
        private List<String> hashes;
        private Boolean[] result;
        int start;
        int end;
        CountDownLatch latch;

        public MultithreadCheck(List<String> passwords, List<String> hashes, Boolean[] result, int start, int end, CountDownLatch latch) {
            this.passwords = passwords;
            this.hashes = hashes;
            this.result = result;
            this.start = start;
            this.end = end;
            this.latch = latch;
        }

        @Override
        public void run() {
            checkPassword(passwords, hashes, result, start, end);
            latch.countDown();
        }
    }

    public class MultithreadHash implements Runnable {
        private List<String> passwords;
        private short logRounds;
        private String[] result;
        private int start;
        private int end;
        private CountDownLatch latch;

        public MultithreadHash(List<String> passwords, short logRounds, String[] result, int start, int end, CountDownLatch latch) {
            this.logRounds = logRounds;
            this.passwords = passwords;
            this.result = result;
            this.start = start;
            this.end = end;
            this.latch = latch;
        }

        @Override
        public void run() {
            hashPassword(passwords, logRounds, result, start, end);
            latch.countDown();
        }
    }
}