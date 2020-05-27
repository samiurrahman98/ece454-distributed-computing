import java.util.*;

class Graph {
    private HashMap<Integer, Set<Integer>> graphMap = null;
    private mapLength = 0;

    public Graph() {
        graphMap = new HashMap<Integer, Set<Integer>>();
    }

    public void addNodes(int firstNode, int secondNode) {
        // if map doesn't contain either node
        Boolean containsFirst = graphMap.containsValue(firstNode);
        Boolean containsSecond = graphMap.containsValue(secondNode);

        if (containsFirst && containsSecond)
            return;

        if (!containsFirst && !containsSecond) {
            // need to create new Set
            Set<Integer> nodeSet = new HashSet<Integer>() {{
                add(firstNode);
                add(secondNode);
            }}
            graphMap.put(mapLength, nodeSet);
            mapLength++;
            return;
        }

        if (containsFirst) {
            // add second node to set where first node exists
            for (int key: graphMap.keySet()) {
                if (graphMap.get(key).contains(firstNode))
                    graphMap.get(key).add(secondNode);
            }
            return;
        }

        if (containsSecond) {
            // add first node to set where second node exists
            for (int key: graphMap.keySet()) {
                if (graphMap.get(key).contains(secondNode))
                    graphMap.get(key).add(firstNode);
            }
            return;
        }
    }

    public void construct(int firstNode, int secondNode) {
        addNodes(firstNode, secondNode);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int key: graphMap.keySet()) {
            sb.append(graphMap[key].toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}