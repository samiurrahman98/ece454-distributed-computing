import java.util.*;

class DFS {
    private Map<Integer, Set<Object>> matrix = null;
    private Map<Integer, Set<Object>> triangleMap = null;
    private ArrayList<ArrayList<Integer>> edges = new ArrayList<ArrayList<Integer>>();

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
        for(int i = 0; i < edges.size() - 1; i++) {
            for (int j = i+1; j < edges.size(); j++) {
                ArrayList<Integer> e1 = edges.get(i);
                ArrayList<Integer> e2 = edges.get(j);
                System.out.println(e1.get(0) + " " + e2.get(0));
                if (e1.get(0) == e2.get(0)) {
                    if (edges.contains(e1) && edges.contains(e2)) {
                        System.out.println(e1.get(0) + " " + e1.get(1) + " " + e2.get(1));
                    }
                }
                
            }
        }
    }

    public String toString() {
        // triangleMap.forEach((key, value) -> System.out.println(key + ":" + value));
        // forEach(ArrayList<Integer> element : edges) {
        //     System.out.println(element[0] + " <--> " + element[1]);
        // }
        for (ArrayList<Integer> edge : edges) {
            for (Integer n : edge) {
                System.out.print(n + " "); 
            }         
            System.out.println();
         } 
        return "";
    }
}