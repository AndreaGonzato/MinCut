public class Contraction {
    private final String newVertex;
    private final Edge contractedEdge;

    public Contraction(String newVertex, Edge contracted) {
        this.newVertex = newVertex;
        this.contractedEdge = contracted;
    }

    public Edge getContractedEdge() {
        return contractedEdge;
    }

    public String getNewVertex() {
        return newVertex;
    }
}
