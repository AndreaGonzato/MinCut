import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Edge {
    private final String[] vertices;

    public Edge(String definition) {
        Pattern pattern = Pattern.compile("v[0-9]+");
        Matcher matcher = pattern.matcher(definition);

        vertices = new String[2];
        for (int i = 0; i < 2; i++) {
            if (matcher.find()) {
                vertices[i] = matcher.group();
            }
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;

        Edge edge = (Edge) o;
        if (vertices[0].equals(edge.vertices[0]) && vertices[1].equals(edge.vertices[1])) return true;
        if (vertices[0].equals(edge.vertices[1]) && vertices[1].equals(edge.vertices[0])) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vertices);
    }

    public String[] getVertices() {
        return vertices;
    }

    @Override
    public String toString() {
        return "(" +
                vertices[0] + " ," + vertices[1] +
                ')';
    }
}
