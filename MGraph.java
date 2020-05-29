import java.util.*;
import java.util.concurrent.*;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.EndpointPair;
import com.google.common.base.Joiner;

class MGraph {
    private MutableGraph<Integer> mGraph = null;
    private ConcurrentHashMap<Integer, String> triangleMap = null;
    private ArrayList<Thread> threads = new ArrayList<Thread>();
    public MGraph() {
        mGraph = GraphBuilder.undirected().build();
    }

    public void add(int firstNode, int secondNode) {
        mGraph.addNode(firstNode);
        mGraph.addNode(secondNode);
        mGraph.putEdge(firstNode, secondNode);
    }

    public void findTriangles() {
        triangleMap = new ConcurrentHashMap<Integer, String>();
        TreeSet<Integer> nodeSet = new TreeSet<Integer>();

        int i = 0;
        Iterator edgeItr = mGraph.edges().iterator();
        while (edgeItr.hasNext()) {
            EndpointPair edge = (EndpointPair) edgeItr.next();
            Iterator nodeItr = mGraph.nodes().iterator();

            Runnable r = new MyRunnable(mGraph, triangleMap, edge, nodeItr);
            Thread t = new Thread(r);
            t.start();
            threads.add(t);
            // while (nodeItr.hasNext()) {
            //     int nodeU = (int) edge.nodeU();
            //     int nodeV = (int) edge.nodeV();
            //     int nodeW = (int) nodeItr.next();
            //     if (nodeV != nodeW && nodeW != nodeU && mGraph.hasEdgeConnecting(nodeV, nodeW) && mGraph.hasEdgeConnecting(nodeW, nodeU)) {
            //         nodeSet.add(nodeU);
            //         nodeSet.add(nodeV);
            //         nodeSet.add(nodeW);
            //         String triangle = Joiner.on(" ").join(nodeSet);
            //         if (!triangleMap.containsValue(triangle)) {
            //             triangleMap.put(i, triangle);
            //             i++;
            //         }
            //         nodeSet.clear();
            //     }
            // }
        }

        for(Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.out.println("Main thread interrupted!");
            }
        }

        // Thread clockThread = new Thread(this, "Clock");
        // clockThread.start();
    }

    // public void run() {
    //     Thread myThread = Thread.currentThread();
    //     System.out.println("Thread Successfully run!");
    // }

    public String toString() {
        String output = "";
        int i = 0;
        int size = triangleMap.values().size();
        for (String triangle: triangleMap.values()) {
            output += triangle;
            if (i != size - 1)
                output += "\n";
        }
        return output;
    }
}