import java.util.*;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.GraphBuilder;
import com.google.common.base.Joiner;

class DFS {
    private MutableGraph<Integer> mGraph = null;
    private HashSet<String> triangles = null;

    public DFS() {
        // edges = new HashMap<Integer, Integer>();
        mGraph = GraphBuilder.undirected().build();
        triangles = new HashSet<String>();
    }

    public void addEdge(int firstNode, int secondNode) {
        mGraph.putEdge(firstNode, secondNode);
    }

    public void findTriangles() {
        TreeSet<Integer> nodeSet = new TreeSet<Integer>();
        Set<Integer> nodes = mGraph.nodes();
        for(Integer node: nodes) {
            Set<Integer> adjacentNodes = mGraph.adjacentNodes(node);
            for(Integer adjNode: adjacentNodes) {
                Set<Integer> adjacentNodes2 = mGraph.adjacentNodes(adjNode);
                for (Integer adjNode2: adjacentNodes2) {
                    if (mGraph.adjacentNodes(adjNode2).contains(node)) {
                        // System.out.println(node + " " + adjNode + " " + adjNode2);
                        nodeSet.add(node);
                        nodeSet.add(adjNode);
                        nodeSet.add(adjNode2);
                        String triangle = Joiner.on(" ").join(nodeSet);
                        triangles.add(triangle);
                        nodeSet.clear();
                    }
                }
            }
        }

    }

    public String toString() {
        Iterator<String> itr = triangles.iterator();
        String output = "";
        while(itr.hasNext()) {
            output += itr.next().toString();
            output += "\n";
        }
        return output;
    }
}