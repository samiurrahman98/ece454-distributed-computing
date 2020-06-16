import org.apache.thrift.transport.TSocket;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class NodeManager {
    private static ConcurrentHashMap<String, NodeProperties> nodeMap = new ConcurrentHashMap<String, NodeProperties>();

    /* 
    ** Function: getAvailableNodeProperties
    ** Purpose: invoked to determine whether a FE Node can offload work to a BE node
    ** Parameters: none
    ** Returns: NodeProperties node (first available node with enough capacity) or null
    */
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

        try {
            nodeProperties = new NodeProperties(nodeProperties.getHostname(), nodeProperties.getPort());

            return nodeProperties;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void addNode(String nodeId, NodeProperties nodeProperties) {
        nodeMap.put(nodeId, nodeProperties);
    }

    public static void removeNode(String nodeId) {
       NodeProperties nodeProperties = nodeMap.remove(nodeId);
    }

    public static boolean containsNode(String nodeId) {
        return nodeMap.containsKey(nodeId);
    }
}