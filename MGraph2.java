import java.util.*;
import java.util.concurrent.*;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.EndpointPair;
import com.google.common.base.Joiner;

class MGraph2 {
    public MutableGraph<Integer> mGraph = null;
    private List<String> triangles = null;
    final private int MAXTHREADPOOLSIZE = 64;

    public MGraph2() {
        mGraph = GraphBuilder.undirected().build();
    }

    public void addEdge(int firstNode, int secondNode) {
        mGraph.putEdge(firstNode, secondNode);
    }

    public void findTriangles(EndpointPair<Integer> targetEdge) {
        // System.out.println(targetEdge);
        List<Future> futures = new ArrayList<Future>();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAXTHREADPOOLSIZE);

        triangles = new ArrayList<String>();

        // for (Integer node: mGraph.nodes()) {
        //     Set<Integer> adjacentNodes = mGraph.adjacentNodes(node);
        //     futures.add(executor.submit(new Runnable() {
        //         public void run() {
        //             TreeSet<Integer> nodeSet = new TreeSet<Integer>();
        //             for (Integer adjacentNode: adjacentNodes) {
        //                 Set<Integer> nextAdjacentNodes = mGraph.adjacentNodes(adjacentNode);
        //                 for (Integer nextAdjacentNode: nextAdjacentNodes) {
        //                     if (mGraph.adjacentNodes(nextAdjacentNode).contains(node)) {
        //                         nodeSet.add(node);
        //                         nodeSet.add(adjacentNode);
        //                         nodeSet.add(nextAdjacentNode);
        //                         String triangle = Joiner.on(" ").join(nodeSet);
        //                         synchronized(triangles) { 
        //                             if (!triangles.contains(triangle))                                         
        //                                 triangles.add(triangle);
        //                         }
        //                         nodeSet.clear();
        //                     }
        //                 }
        //             }
        //         }
        //     }));
        // }

        // executor.shutdown();
        // for(Future f: futures) {
        //     try {
        //         f.get();
        //     } catch (InterruptedException | ExecutionException e) {
        //         e.getCause().printStackTrace();
        //     }
        // }

        // Set<EndpointPair<Integer>> edges = mGraph.edges();
        HashMap<Integer, ArrayList<EndpointPair<Integer>>> incidentEdges = new HashMap<Integer, ArrayList<EndpointPair<Integer>>>();
        for (EndpointPair<Integer> edge: mGraph.edges()) {
            if(edge.nodeU() == targetEdge.nodeU() && edge.nodeV() == targetEdge.nodeV()) {
                // System.out.println("Got here!");
            } else {
                if(incidentEdges.get(edge.nodeU()) == null || incidentEdges.get(edge.nodeV()) == null) {
                    // System.out.println("Got here!");
                    if(edge.nodeU() == targetEdge.nodeU()) {
                        // System.out.println("Got here!");
                        incidentEdges.put(edge.nodeU(), new ArrayList<EndpointPair<Integer>>(Arrays.asList(edge)));
                    }
                    else if(edge.nodeV() == targetEdge.nodeV()) {
                        incidentEdges.put(edge.nodeV(), new ArrayList<EndpointPair<Integer>>(Arrays.asList(edge)));
                    }
                } else {
                    if(edge.nodeU() == targetEdge.nodeU()) {
                        incidentEdges.get(edge.nodeU()).add(edge);
                    }
                    else if(edge.nodeV() == targetEdge.nodeV()) {
                        incidentEdges.get(edge.nodeV()).add(edge);
                    }
                }
            }
            // System.out.println(edge.nodeU());
            // System.out.println(edge.nodeV());
        }
        // System.out.println("got here!");
        // System.out.println(incidentEdges.size());
        for (Integer i : incidentEdges.keySet()) {
            // System.out.println(targetEdge.nodeU() + " " + targetEdge.nodeV());
            // System.out.println("key: " + i + " value: " + incidentEdges.get(i));
            // System.out.println("Got it!");
            TreeSet<Integer> nodes = new TreeSet<Integer>();
            nodes.add(targetEdge.nodeU());
            nodes.add(targetEdge.nodeV());
            nodes.add(incidentEdges.get(i).get(0).nodeU());
            nodes.add(incidentEdges.get(i).get(0).nodeV());
            String triangle = Joiner.on(" ").join(nodes);
            if (!triangles.contains(triangle))                                         
                triangles.add(triangle);
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