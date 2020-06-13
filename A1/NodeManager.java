import org.apache.thrift.transport.TSocket;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class NodeManager {
    private static ConcurrentHashMap<String, NodeProperties> nodeMap = new ConcurrentHashMap<String, NodeProperties>();

    public static synchronized NodeProperties getAvailableNodeProperties() {
        if (nodeMap.size() == 0) return null;

        for (NodeProperties node: nodeMap.values()) {
            if (node.isNotOccupied()) {
                node.markOccupied();
                return node;
            }
        }

        NodeProperties nodeProperties = nodeMap.values().iterator().next();
        for (NodeProperties node: nodeMap.values())
            if (nodeProperties.getLoad() > node.getLoad()) nodeProperties = node;

        if (nodeProperties == null) return null;

        String hostname = nodeProperties.getHostname();
        String port = nodeProperties.getPort();

        try {
            nodeProperties = new NodeProperties(hostname, port);
            return nodeProperties;
        } catch (Exception e) {
            System.out.println("failed to create new Node from " + hostname + " " + port);
        }
        return null;
    }

    public static void addNode(String nodeId, NodeProperties nodeProperties) {
        nodeMap.put(nodeId, nodeProperties);
        System.out.println("number of available nodes: " + nodeMap.size());
    }

    public static void removeNode(String nodeId) {
       NodeProperties nodeProperties = nodeMap.remove(nodeId);
       if (NodeProperties == null) System.out.println("Tried to remove " + nodeId + "from nodeMap but it did not exist");
    }

    public static boolean containsNode(String nodeId) {
        return nodeMap.containsKey(nodeId);
    }

    /**
    * A class representing a BENode info
    */
    class NodeProperties {
        private BcryptService.Client BENodeClient;
        private TTransport transport;
        private final String hostname;
        private final String port;
        private double load;
        private boolean occupied;
        public String nodeId;

        NodeProperties(String hostname, String port) {
            TSocket sock = new TSocket(hostname, Integer.parseInt(port));
            TTransport transport = new TFramedTransport(sock);
            TProtocol protocol = new TBinaryProtocol(transport);
            BcryptService.Client client = new BcryptService.Client(protocol);

            this.BENodeClient = client;
            this.transport = transport;
            this.nodeId = hostname + port;
            this.occupied = false;
            this.hostname = hostname;
            this.port = port;
        }

        public BcryptService.Client getClient() {
            return BENodeClient;
        }

        public TTransport getTransport() {
            return transport;
        }

        public void markOccupied() {
            occupied = true;
        }

        public void markAvailable() {
            occupied = false;
        }

        public boolean isNotOccupied() {
            return !occupied;
        }

        public void addLoad(int numPasswords, short logRounds) {
            load += numPasswords * Math.pow(2, logRounds);
        }

        public void reduceLoad(int numPasswords, short logRounds) {
            load -= numPasswords * Math.pow(2, logRounds);
        }

        public double getLoad() {
            return load;
        }

        public String getHostname() {
            return hostname;
        }

        public String getPort() {
            return port;
        }
    }
}