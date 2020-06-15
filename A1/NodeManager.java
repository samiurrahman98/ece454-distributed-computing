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
        System.out.println("nodeMap: " + nodeMap.toString());
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
        System.out.println("nodeId: " + nodeId);
        System.out.println("hostname: " + nodeProperties.getHostname());
        System.out.println("port: " + nodeProperties.getPort());
        System.out.println("is not occupied? " + nodeProperties.isNotOccupied());
        System.out.println("number of available nodes: " + nodeMap.size());
    }

    public static void removeNode(String nodeId) {
       NodeProperties nodeProperties = nodeMap.remove(nodeId);
       if (nodeProperties == null) System.out.println("Tried to remove " + nodeId + "from nodeMap but it did not exist");
    }

    public static boolean containsNode(String nodeId) {
        return nodeMap.containsKey(nodeId);
    }
}