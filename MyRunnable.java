import java.util.*;
import java.util.concurrent.*;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.EndpointPair;
import com.google.common.base.Joiner;

public class MyRunnable implements Runnable {
    private MutableGraph<Integer> mGraph = null;
    private Set<String> triangles = null;
    public MyRunnable(MutableGraph<Integer> mGraph, Set<String> triangles) {
        this.mGraph = mGraph;
        this.triangles = triangles;
    }

    public void run() {
        for (EndpointPair edge: mGraph.edges()) {
            int nodeU = (int) edge.nodeU();
            int nodeV = (int) edge.nodeV();
            if (mGraph.degree(nodeU) >= 2 && mGraph.degree(nodeV) >= 2) {
                Set<Integer> intersection = new HashSet(mGraph.adjacentNodes(nodeU));
                intersection.retainAll(mGraph.adjacentNodes(nodeV));
                TreeSet<Integer> nodeSet = new TreeSet<Integer>();
                for (Integer thirdNode: intersection) {
                    nodeSet.add(nodeU);
                    nodeSet.add(nodeV);
                    nodeSet.add(thirdNode);
                    String triangle = Joiner.on(" ").join(nodeSet);                                       
                    triangles.add(triangle);
                    nodeSet.clear();
                }
            }
        }
        for(String triangle: triangles) {
            System.out.println(triangle);
        }
    }
}