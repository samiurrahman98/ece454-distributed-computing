import java.util.*;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.EndpointPair;
import com.google.common.base.Joiner;

class MyRunnable implements Runnable {
    
    private TreeSet<Integer> nodeSet = new TreeSet<Integer>();
    private MutableGraph<Integer> mGraph;
    private Map<Integer, String> triangleMap;
    private EndpointPair edge;
    private Iterator nodeItr;
    private static int i = 0;
    // private EndpointPair edge = (EndpointPair) edgeItr.next();
    // private Iterator nodeItr = mGraph.nodes().iterator(); 
    public MyRunnable(MutableGraph<Integer> mGraph, Map<Integer, String> triangleMap, EndpointPair edge, Iterator nodeItr) {
        this.mGraph = mGraph;
        this.triangleMap = triangleMap;
        this.edge = edge;
        this.nodeItr = nodeItr;
    }

    public void run() {
        // System.out.println("Thread created successfully!");
        while (nodeItr.hasNext()) {
            int nodeU = (int) edge.nodeU();
            int nodeV = (int) edge.nodeV();
            int nodeW = (int) nodeItr.next();
            if (nodeV != nodeW && nodeW != nodeU && mGraph.hasEdgeConnecting(nodeV, nodeW) && mGraph.hasEdgeConnecting(nodeW, nodeU)) {
                nodeSet.add(nodeU);
                nodeSet.add(nodeV);
                nodeSet.add(nodeW);
                String triangle = Joiner.on(" ").join(nodeSet);
                // System.out.println("Thread: " + triangle);
                if (!triangleMap.containsValue(triangle)) {
                    synchronized(this) {                        
                        triangleMap.put(i, triangle);
                        i++;
                    }
                }
                nodeSet.clear();
            }
        }
    }
}