import java.util.*;

class DFS {
    private Map<Integer, Set<Object>> matrix = null;
    private Map<Integer, Set<Object>> triangleMap = null;
    private ArrayList<ArrayList<String>> edges = new ArrayList<ArrayList<String>>();

    public DFS() {
        matrix = new HashMap<>();
    }

    public void addEdge(int firstNode, int secondNode) {
        // if (!matrix.containsKey(firstNode)) 
        //     matrix.put(firstNode, new HashSet<Object>());
        // if (!matrix.containsKey(secondNode))
        //     matrix.put(secondNode, new HashSet<Object>());

        // matrix.get(firstNode).add(secondNode);
        // matrix.get(secondNode).add(firstNode);
        edges.get(Integer.toString(firstNode)).add(Integer.toString(secondNode));
    }

    // public void findTriangles() {
    //     triangleMap = new HashMap<Integer, Set<Object>>();
    //     Set<Object> triangle = new HashSet<Object>();

    //     int i = 0;
    //     for (Set<Object> neighbors: matrix.values()) {
    //         for (Object u : neighbors) {
    //             for (Object v: neighbors) {
    //                 if ((!u.equals(v)) && (matrix.get(u).contains(v))) {
    //                     triangle.add(u);
    //                     Iterator itr = matrix.get(u).iterator();
    //                     while (itr.hasNext())
    //                         triangle.add(itr.next());
    //                 }
    //             }
    //         }
    //         triangleMap.put(i, triangle);
    //     }
    // }

    public String toString() {
        // triangleMap.forEach((key, value) -> System.out.println(key + ":" + value));
        // return "";
        for(ArrayList<String> element : edges) {
            System.out.println(element[0] + " --> " + element[1]);
        }
    }
}