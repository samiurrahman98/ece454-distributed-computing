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

        for (Integer node: mGraph.nodes()) {
            Set<Integer> adjacentNodes = mGraph.adjacentNodes(node);            
            ArrayList<String> threadTriangles = new ArrayList<String>();
            Runnable task = new Runnable() {
                public void run() {
                    TreeSet<Integer> nodeSet = new TreeSet<Integer>();
                    // ArrayList<String> threadTriangles = new ArrayList<String>();
                    for (Integer adjacentNode: adjacentNodes) {
                        Set<Integer> nextAdjacentNodes = mGraph.adjacentNodes(adjacentNode);
                        for (Integer nextAdjacentNode: nextAdjacentNodes) {
                            if (mGraph.adjacentNodes(nextAdjacentNode).contains(node)) {
                                nodeSet.add(node);
                                nodeSet.add(adjacentNode);
                                nodeSet.add(nextAdjacentNode);
                                String triangle = Joiner.on(" ").join(nodeSet);
                                // synchronized(triangles) { 
                                    if (!threadTriangles.contains(triangle))                                         
                                        threadTriangles.add(triangle);
                                // }
                                nodeSet.clear();
                            }
                        }
                    }
                }
            };
            futures.add(executor.submit(task, threadTriangles));
        }

        executor.shutdown();
        for(Future f: futures) {
            try {
                triangles.addAll((ArrayList<String>)f.get());
                // System.out.println(f.get());
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