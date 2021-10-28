import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Graph extends Multigraph<String, DefaultEdge>{
    private int counterVertices = 0;
    Random generator = new Random(13);
    static private final boolean REPEATABLE_TEST = false;

    public Graph(int size, double edgeProbability) {
        super(DefaultEdge.class);
        initVertices(size);
        initEdges(edgeProbability);
    }

    @Override
    public Object clone() {
        return (Graph) super.clone();
    }

    public String addOneVertex(){
        String vertexName = "v"+counterVertices;
        addVertex((String) vertexName);
        counterVertices++;
        return vertexName;
    }

    private void initVertices(int quantity){
        for (int i = 0; i < quantity; i++) {
            addOneVertex();
        }
    }

    private void initEdges(double edgeProbability){
        List<String> vertices = new ArrayList<>(this.vertexSet());

        for (int i = 0; i < vertices.size(); i++) {
            for (int j = i+1; j < vertices.size(); j++) {
                if (REPEATABLE_TEST){
                    if (generator.nextDouble() < edgeProbability){
                        addEdge(vertices.get(i), vertices.get(j));
                    }
                }else {
                    if (Math.random() < edgeProbability){
                        addEdge(vertices.get(i), vertices.get(j));
                    }
                }

            }
        }
    }


    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        Iterator<String> stringIterator = new DepthFirstIterator<>(this);
        while (stringIterator.hasNext()) {
            String vertex = stringIterator.next();

            Pattern pattern = Pattern.compile("v[0-9]+");
            List<String> neighboringVertices = new ArrayList<>();
            Matcher matcher = pattern.matcher(edgesOf(vertex).toString());
            while (matcher.find()) {
                String neighboringVertice = matcher.group();
                if (!neighboringVertice.equals(vertex)){
                    neighboringVertices.add(neighboringVertice);
                }
            }

            result.append("Vertex ").append(vertex).append(" is connected to: ").append(neighboringVertices).append("\n");
        }
        return result.toString();
    }

    public int getCounterVertices() {
        return counterVertices;
    }

    public Set<DefaultEdge> getEdgeSet(){
        return edgeSet();
    }

    public Set<String> getVertexSet(){
        return vertexSet();
    }
}
