import java.util.*;
import com.google.common.base.Joiner;

class AdjacencyMatrix {
    private Map<Integer, Set<Object>> matrix = null;
    private Map<Integer, String> triangleMap = null;

    public AdjacencyMatrix() {
        matrix = new HashMap<>();
    }

    public void addEdge(int firstNode, int secondNode) {
        if (!matrix.containsKey(firstNode)) 
            matrix.put(firstNode, new HashSet<Object>());
        if (!matrix.containsKey(secondNode))
            matrix.put(secondNode, new HashSet<Object>());

        matrix.get(firstNode).add(secondNode);
        matrix.get(secondNode).add(firstNode);
    }

    public void findTriangles() {
        triangleMap = new HashMap<Integer, String>();
        Set<Object> triangle = new HashSet<Object>();

        int i = 0;
        for (Set<Object> neighbors: matrix.values()) {
            for (Object u : neighbors) {
                for (Object v: neighbors) {
                    if ((!u.equals(v)) && (matrix.get(u).contains(v))) {
                        triangle.add(u);
                        Iterator itr = matrix.get(u).iterator();
                        while (itr.hasNext())
                            triangle.add(itr.next());
                    }
                }
            }
            if (triangle.size() > 0) {
                String triangleStr = Joiner.on(" ").join(triangle);
                if (!triangleMap.containsValue(triangleStr)) {
                    triangleMap.put(i, triangleStr);
                    i++;
                }
                triangle.clear();
            }
        }
    }

    public String toString() {
        String output = "";
        int size = triangleMap.size();
        int i = 0;
        for (String triangle: triangleMap.values()) {
            output += triangle;
            if (i != size - 1)
                output += "\n";
            i++;
        } 
        return output;
    }
}