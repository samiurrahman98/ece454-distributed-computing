import java.util.*;
import java.util.concurrent.*;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.EndpointPair;
import com.google.common.base.Joiner;

class MGraph {
    private MutableGraph<Integer> mGraph = null;
    private Set<String> triangles = null;
    final private int MAXTHREADPOOLSIZE = 64;

    public MGraph() {
        mGraph = GraphBuilder.undirected().build();
    }

    public void addEdge(int firstNode, int secondNode) {
        mGraph.putEdge(firstNode, secondNode);
    }

    public void findTriangles() {
        List<Future> futures = new ArrayList<Future>();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAXTHREADPOOLSIZE);

        triangles = new HashSet<String>();

        for (EndpointPair edge: mGraph.edges()) {
            futures.add(executor.submit(new Runnable() {
                public void run() {
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
            }, triangles));
        }

        executor.shutdown();
        for(Future f: futures) {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                e.getCause().printStackTrace();
            }
        }
    }

    public String toString() {
        String output = "";
        int i = 0;
        int size = triangles.size();
        for (String triangle: triangles) {
            output += triangle;
            if (i != size - 1)
                output += "\n";
        }
        return output;
    }
}