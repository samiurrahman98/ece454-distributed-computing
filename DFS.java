import java.util.*;

class DFS {
    private Map<Integer, Set<Object>> matrix = null;
    private Map<Integer, Set<Object>> triangleMap = null;
    private ArrayList<ArrayList<Integer>> edges = new ArrayList<ArrayList<Integer>>();
    private ArrayList<ArrayList<Integer>> triangles = new ArrayList<ArrayList<Integer>>();

    public DFS() {
        matrix = new HashMap<>();
    }

    public void addEdge(int firstNode, int secondNode) {
        if (!matrix.containsKey(firstNode)) 
            matrix.put(firstNode, new HashSet<Object>());
        if (!matrix.containsKey(secondNode))
            matrix.put(secondNode, new HashSet<Object>());

        matrix.get(firstNode).add(secondNode);
        matrix.get(secondNode).add(firstNode);
        
        ArrayList<Integer> edge = new ArrayList<Integer>();
        edge.add(firstNode);
        edge.add(secondNode);
        edges.add(edge);
    }

    public void findTriangles() {
        for(int i = 0; i < edges.size() - 1; i++) {
            for (int j = i+1; j < edges.size(); j++) {
                ArrayList<Integer> e1 = edges.get(i);
                ArrayList<Integer> e2 = edges.get(j);
                // System.out.println(e1.get(0) + " " + e2.get(0));
                if (e1.get(0) == e2.get(0)) {
                    if (edges.contains(e1) && edges.contains(e2)) {
                        // System.out.println(e1.get(0) + " " + e1.get(1) + " " + e2.get(1));
                        ArrayList<Integer> triangle = new ArrayList<Integer>();
                        triangle.add(e1.get(0));
                        triangle.add(e1.get(1));
                        triangle.add(e2.get(1));
                        triangles.add(triangle);
                    } else {
                        break;
                    }
                }
                
            }
        }
    }

    public String toString() {
        System.out.println("TRIANGLE");
        for (ArrayList<Integer> triangle : triangles) {
            for (Integer vertex : triangle) {
                System.out.print(vertex + " "); 
            }         
            System.out.println();
         } 
        return "";
    }
}