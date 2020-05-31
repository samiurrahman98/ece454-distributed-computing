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

        // for (Integer node: mGraph.nodes()) {
        //     if (mGraph.degree(node) >= 2) {
        //         Set<Integer> adjacentNodes = mGraph.adjacentNodes(node);
        //         futures.add(executor.submit(new Runnable() {
        //             public void run() {
        //                 TreeSet<Integer> nodeSet = new TreeSet<Integer>();
        //                 for (Integer adjacentNode: adjacentNodes) {
        //                     if (mGraph.degree(adjacentNode) >= 2) {
        //                         Set<Integer> nextAdjacentNodes = mGraph.adjacentNodes(adjacentNode);
        //                         for (Integer nextAdjacentNode: nextAdjacentNodes) {
        //                             if (mGraph.degree(nextAdjacentNode) >= 2) {
        //                                 // if (mGraph.adjacentNodes(nextAdjacentNode).contains(node)) {
        //                                 //     nodeSet.add(node);
        //                                 //     nodeSet.add(adjacentNode);
        //                                 //     nodeSet.add(nextAdjacentNode);
        //                                 //     String triangle = Joiner.on(" ").join(nodeSet);                               
        //                                 //     triangles.add(triangle);
        //                                 //     nodeSet.clear();
        //                                 // }
        //                                 EndpointPair<Integer> edge = new EndpointPair<Integer>();
        //                                 edge.ordered(nextAdjacentNode, node);
        //                                 if (mGraph.edges().contains(edge)) {
        //                                     nodeSet.add(node);
        //                                     nodeSet.add(adjacentNode);
        //                                     nodeSet.add(nextAdjacentNode);
        //                                     String triangle = Joiner.on(" ").join(nodeSet);                               
        //                                     triangles.add(triangle);
        //                                     nodeSet.clear();
        //                                 }
        //                             }
        //                         }
        //                     }
        //                 }
        //             }
        //         }, triangles));
        //     }
        // }
        Set<EndpointPair<Integer>> edges = mGraph.edges();
        ConcurrentHashMap<Integer, ArrayList<EndpointPair<Integer>>> incidentEdges = new ConcurrentHashMap<Integer, ArrayList<EndpointPair<Integer>>>();
        for(EndpointPair<Integer> targetEdge: edges) {
            futures.add(executor.submit(new Runnable() {
                public void run() {
                    // System.out.println(targetEdge.nodeU() + " " + targetEdge.nodeV());
                    // HashMap<Integer, ArrayList<EndpointPair<Integer>>> incidentEdges = new HashMap<Integer, ArrayList<EndpointPair<Integer>>>();
                    for (EndpointPair<Integer> edge: edges) {
                        if(edge.nodeU() == targetEdge.nodeU() && edge.nodeV() == targetEdge.nodeV() || edge.nodeU() == targetEdge.nodeV() && edge.nodeV() == targetEdge.nodeU()) {
                            // System.out.println("Got here!");
                        } else {
                            if(incidentEdges.get(edge.nodeU()) == null || incidentEdges.get(edge.nodeV()) == null) {
                                // System.out.println("Got here!");
                                if(edge.nodeU() == targetEdge.nodeU() || edge.nodeU() == targetEdge.nodeV()) {
                                    // System.out.println("Got here!");
                                    incidentEdges.put(edge.nodeU(), new ArrayList<EndpointPair<Integer>>(Arrays.asList(edge)));
                                }
                                else if(edge.nodeV() == targetEdge.nodeV() || edge.nodeV() == targetEdge.nodeU()) {
                                    incidentEdges.put(edge.nodeV(), new ArrayList<EndpointPair<Integer>>(Arrays.asList(edge)));
                                }
                            } else {
                                if(edge.nodeU() == targetEdge.nodeU() || edge.nodeU() == targetEdge.nodeV()) {
                                    // System.out.println("Here!");
                                    incidentEdges.get(edge.nodeU()).add(edge);
                                }
                                else if(edge.nodeV() == targetEdge.nodeV()|| edge.nodeV() == targetEdge.nodeU()) {
                                    incidentEdges.get(edge.nodeV()).add(edge);
                                }
                            }
                        }
                    }
                    Iterator itr = incidentEdges.entrySet().iterator();
                    while(itr.hasNext()) {
                        Map.Entry pair = (Map.Entry)itr.next();
                        System.out.println(pair.getKey() + " = " + pair.getValue());
                    }
                    // System.out.println("done edge!");
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