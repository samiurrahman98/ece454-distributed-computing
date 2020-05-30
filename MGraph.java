import java.util.*;
import java.util.concurrent.*;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.EndpointPair;
import com.google.common.base.Joiner;

class MGraph {
    private MutableGraph<Integer> mGraph = null;
    private List<String> triangles = null;

    public MGraph() {
        mGraph = GraphBuilder.undirected().build();
    }

    public void addEdge(int firstNode, int secondNode) {
        mGraph.putEdge(firstNode, secondNode);
    }

    public void findTriangles() {
        List<Future> futures = new ArrayList<Future>();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        triangles = new ArrayList<String>();

        for (Integer node: mGraph.nodes()) {
            Set<Integer> adjacentNodes = mGraph.adjacentNodes(node);
            futures.add(executor.submit(new Runnable() {
                public void run() {
                    TreeSet<Integer> nodeSet = new TreeSet<Integer>();
                    for (Integer adjacentNode: adjacentNodes) {
                        Set<Integer> nextAdjacentNodes = mGraph.adjacentNodes(adjacentNode);
                        for (Integer nextAdjacentNode: nextAdjacentNodes) {
                            if (mGraph.adjacentNodes(nextAdjacentNode).contains(node)) {
                                nodeSet.add(node);
                                nodeSet.add(adjacentNode);
                                nodeSet.add(nextAdjacentNode);
                                String triangle = Joiner.on(" ").join(nodeSet);
                                synchronized(triangles) { 
                                    if (!triangles.contains(triangle))                                         
                                        triangles.add(triangle);
                                }
                                nodeSet.clear();
                            }
                        }
                    }
                }
            }));
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