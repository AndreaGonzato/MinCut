import java.util.HashSet;
import java.util.Set;

public class Cut {
    Set<String> verticesSet1;
    Set<String> verticesSet2;
    private final int size;

    public Cut(Set<String> verticesSet1, Set<String> verticesSet2, int size) {
        Set<String> intersection = new HashSet<>(verticesSet1); // use the copy constructor
        intersection.retainAll(verticesSet2);
        if (intersection.size() > 0){
            throw new IllegalArgumentException("The two sets need to have a empty intersection");
        }
        this.verticesSet1 = verticesSet1;
        this.verticesSet2 = verticesSet2;
        this.size = size;
    }

    public Cut(int size){
        this.size = size;
    }



    public Set<String> getVerticesSet1() {
        return verticesSet1;
    }

    public Set<String> getVerticesSet2() {
        return verticesSet2;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "Cut{" +
                "verticesSet1=" + verticesSet1 +
                ", verticesSet2=" + verticesSet2 +
                ", size=" + size +
                '}';
    }
}
