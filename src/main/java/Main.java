import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    // parameter
    private static final int GRAPH_SIZE = 6;
    private static final double EDGE_PROBABILITY = 0.3;

    // fixed constant
    private static final int STOPPING_CRITERION_SIZE = 6;


    public static void main(String[] args) {


        int[] graphSizesCase2 = {3, 5, 10, 20, 40};
        System.out.println("Graph Size | Algorithm |  Time  | Cut Size");
        for (int graphSize : graphSizesCase2) {
            Graph graph = new Graph(graphSize, EDGE_PROBABILITY);

            long reference = System.nanoTime();
            Cut kargerCut = startFastCut(graph);
            long finishm = System.nanoTime();
            double time = ((double) (finishm - reference)) / 1000000000.0;

            System.out.format("\t%d \t\t FastCut \t %.4f \t\t %d\n", graphSize, time, kargerCut.getSize());

        }

        int[] graphSizesCase1 = {6, 20, 30, 50};
        System.out.println();
        System.out.println("Graph Size | Algorithm |  Time  | Cut Size");
        for (int graphSize : graphSizesCase1) {
            Graph graph = new Graph(graphSize, EDGE_PROBABILITY);

            long reference = System.nanoTime();
            Cut kargerCut = kargerAlgorithm(graph);
            long finishm = System.nanoTime();
            double time = ((double) (finishm - reference)) / 1000000000.0;

            System.out.format("\t%d \t\t Karger Algorithm \t %.4f \t\t %d\n", graphSize, time, kargerCut.getSize());

        }






    }

    public static Cut startFastCut(Graph graph){
        Map<String, Edge> contractionsMap = new HashMap<>();
        return fastCut(graph, contractionsMap);
    }


    public static Cut fastCut(Graph graph,  Map<String, Edge> contractionsMap){
        if (graph.vertexSet().size() <= STOPPING_CRITERION_SIZE){
            return startFindMinCutBruteForceVersion(graph);
        }

        int n = graph.vertexSet().size();
        int t = (int) Math.ceil( 1+n/Math.sqrt(2));

        Graph copyGraphH1 = (Graph) graph.clone();
        Graph copyGraphH2 = (Graph) graph.clone();
        Map<String, Edge> contractionsMapH1 = new HashMap<>(contractionsMap);
        Map<String, Edge> contractionsMapH2 = new HashMap<>(contractionsMap);

        int sizeToReduce = n-t;

        // reduce till graph have size t
        Optional<String> disconnectedNodeH1 = contractGraphRecursively(copyGraphH1, contractionsMapH1, sizeToReduce);
        Optional<String> disconnectedNodeH2 = contractGraphRecursively(copyGraphH2, contractionsMapH2, sizeToReduce);

        Cut cutH1;
        Cut cutH2;
        if (disconnectedNodeH1.isPresent()){
            // corner case: a node is disconnected to the graph
            cutH1 = determineCut(copyGraphH1, contractionsMapH1, disconnectedNodeH1);
        }else {
            cutH1 = fastCut(copyGraphH1, contractionsMapH1);
        }

        if (disconnectedNodeH2.isPresent()){
            // corner case: a node is disconnected to the graph
            cutH2 = determineCut(copyGraphH2, contractionsMapH2, disconnectedNodeH2);
        }else{
            cutH2 = fastCut(copyGraphH2, contractionsMapH2);
        }

        return smallerCut(cutH1, cutH2);
    }

    public static Cut startFindMinCutBruteForceVersion(Graph graph){
        Map<String, Edge> contractionsMap = new HashMap<>();
        Set<Edge> visitedEdges = new HashSet<>();
        return findMinCutBruteForceVersion(graph, contractionsMap, visitedEdges);
    }

    public static Cut findMinCutBruteForceVersion(Graph graph, Map<String, Edge> contractionsMap, Set<Edge> visitedEdges){
        // TODO something is not working here... spot and fix the bug
        if (graph.vertexSet().size() <= 2){
            // base case
            return determineCut(graph, contractionsMap, Optional.empty());
        }

        Cut bestCut = new Cut(Integer.MAX_VALUE);

        for (DefaultEdge defaultEdge : graph.edgeSet()){
            Edge edge = new Edge(defaultEdge.toString());

            if (visitedEdges.contains(edge)){
                continue;
            }
            Graph copyGraph = (Graph) graph.clone();
            Map<String, Edge> contractionsMapCopy = new HashMap<>(contractionsMap);
            Set<Edge> visitedEdgesCopy = new HashSet<>(visitedEdges);

            Contraction contraction = contractEdge(copyGraph, defaultEdge);
            contractionsMapCopy.put(contraction.getNewVertex(), contraction.getContractedEdge());
            visitedEdgesCopy.add(edge);

            Optional<String> disconnectedNode = findDisconnectedNode(copyGraph);
            if (disconnectedNode.isPresent()){
                return determineCut(copyGraph, contractionsMapCopy, disconnectedNode);
            }

            Cut cut = findMinCutBruteForceVersion(copyGraph, contractionsMapCopy, visitedEdgesCopy);
            bestCut = smallerCut(cut, bestCut);
        }
        return bestCut;
    }


    public static Cut kargerAlgorithm(Graph graph){
        int graphSize = graph.vertexSet().size();
        int repetition = (int)(graphSize*graphSize/2);
        return selectBestCutFromMultipleExecutionsOfFindCut(graph, repetition);
    }

    public static Cut selectBestCutFromMultipleExecutionsOfFindCut(Graph graph, int repetition){
        if (repetition <= 0){
            throw new IllegalArgumentException("select a number of repetition that is grater than zero");
        }

        Cut bestCut = new Cut(Integer.MAX_VALUE);

        for (int i = 0; i < repetition; i++) {
            Graph copyGraph = (Graph) graph.clone();
            Cut cut = findCut(copyGraph);
            bestCut = smallerCut(bestCut, cut);
        }

        return bestCut;
    }

    private static Cut smallerCut(Cut cut1, Cut cut2){
        if (cut1.getSize() < cut2.getSize()){
            return cut1;
        }else {
            return cut2;
        }
    }

    /**
     * Try to reduce the graph Recursively.

     * @param graph : a graph to contract several times. after the invocation of the method the graph is changed (smaller size)
     * @param contractionsMap : a data structure that keeps track of all the contractions
     * @param sizeToReduce : an integer that denote how many times we want to contract the graph
     * @return the disconnectedNode node in case during the process we found one
     */
    public static Optional<String> contractGraphRecursively(Graph graph, Map<String, Edge> contractionsMap, int sizeToReduce) {
        if (sizeToReduce <= 0 || graph.vertexSet().size() <= 2){
            // stop criteria
            return Optional.empty();
        }else {

            // corner case: a node is disconnected to the graph
            Optional<String> disconnectedNode = findDisconnectedNode(graph);
            if (disconnectedNode.isPresent()){
                return disconnectedNode;
            }

            Contraction contraction = contractRandomEdge(graph);
            contractionsMap.put(contraction.getNewVertex(), contraction.getContractedEdge());

            sizeToReduce--;
            return contractGraphRecursively(graph, contractionsMap, sizeToReduce);
        }


    }

    public static Cut findCut(Graph graph) {
        Map<String, Edge> contractionsMap = new HashMap<>();

        while (graph.vertexSet().size() > 2){

            // corner case: a node is disconnected to the graph
            Optional<String> disconnectedNode = findDisconnectedNode(graph);
            if (disconnectedNode.isPresent()){
                return determineCut(graph, contractionsMap, disconnectedNode);
            }

            Contraction contraction = contractRandomEdge(graph);
            contractionsMap.put(contraction.getNewVertex(), contraction.getContractedEdge());
        }

        return determineCut(graph, contractionsMap, Optional.empty());

    }

    private static Optional<String> findDisconnectedNode(Graph graph){
        for (String vertex : graph.vertexSet()){
            if (graph.edgesOf(vertex).size() <= 0 ){
                return Optional.of(vertex);
            }
        }
        return Optional.empty();
    }



    private static Cut determineCut(Graph graph, Map<String, Edge> contractionsMap, Optional<String> disconnectedVertex){
        if (graph.vertexSet().size() > 2 && disconnectedVertex.isEmpty()){
            throw new IllegalArgumentException("determineCut need a reduced graph of size 2 or a disconnected vertex");
        }

        Set<String> set1 = new HashSet<>();
        Set<String> set2 = new HashSet<>();
        List<String> verticesToAddToSet1 = new LinkedList<>();
        List<String> verticesToAddToSet2 = new LinkedList<>();
        int cutSize = graph.getEdgeSet().size();


        if (graph.vertexSet().size() == 2){
            List<String> twoVertices = new ArrayList<>(graph.vertexSet());
            verticesToAddToSet1.add(twoVertices.get(0));
            verticesToAddToSet2.add(twoVertices.get(1));
        }

        if (disconnectedVertex.isPresent()){
            cutSize = 0;
            verticesToAddToSet1.add(disconnectedVertex.get());
            for (String vertex : graph.vertexSet()){
                if (!vertex.equals(disconnectedVertex.get())){
                    verticesToAddToSet2.add(vertex);
                }
            }
        }


        while (verticesToAddToSet1.size() > 0){
            if(contractionsMap.get(verticesToAddToSet1.get(0)) != null){
                verticesToAddToSet1.add(contractionsMap.get(verticesToAddToSet1.get(0)).getVertices()[0]);
                verticesToAddToSet1.add(contractionsMap.get(verticesToAddToSet1.get(0)).getVertices()[1]);
            }else {
                // no mapping -> original node
                set1.add(verticesToAddToSet1.get(0));
            }
            verticesToAddToSet1.remove(0);
        }

        while (verticesToAddToSet2.size() > 0){
            if(contractionsMap.get(verticesToAddToSet2.get(0)) != null){
                verticesToAddToSet2.add(contractionsMap.get(verticesToAddToSet2.get(0)).getVertices()[0]);
                verticesToAddToSet2.add(contractionsMap.get(verticesToAddToSet2.get(0)).getVertices()[1]);
            }else {
                // no mapping -> original node
                set2.add(verticesToAddToSet2.get(0));
            }
            verticesToAddToSet2.remove(0);
        }


        return new Cut(set1, set2, cutSize);
    }

    public static Contraction contractEdge(Graph graph, DefaultEdge defaultEdgeToContract) {
        if (graph.edgeSet().stream().noneMatch(defaultEdge -> defaultEdge.equals(defaultEdgeToContract))){
            throw new IllegalArgumentException("to contract an edge, the edge need to be in the graph");
        }

        Edge selectedEdge = new Edge(defaultEdgeToContract.toString());

        String vertexX = selectedEdge.getVertices()[0];
        String vertexY = selectedEdge.getVertices()[1];

        // add vertex Z
        String vertexZ = graph.addOneVertex();

        List<Edge> edgesToRemove = graph.edgesOf(vertexX)
                .stream()
                .map(defaultEdge -> new Edge(defaultEdge.toString()))
                .collect(Collectors.toList());

        edgesToRemove.addAll(
                graph.edgesOf(vertexY)
                        .stream()
                        .map(defaultEdge -> new Edge(defaultEdge.toString()))
                        .collect(Collectors.toList())
        );

        // edgesToRemove contains all the edges that involve vertexX or vertexY
        for (Edge edge : edgesToRemove) {

            // corner case
            if ((edge.getVertices()[0].equals(vertexX) && edge.getVertices()[1].equals(vertexY)) ||
                    (edge.getVertices()[0].equals(vertexY) && edge.getVertices()[1].equals(vertexX))) {
                graph.removeEdge(edge.getVertices()[0], edge.getVertices()[1]);
                continue;
            }

            // add the new edges to the new vertex Z
            if ((edge.getVertices()[0].equals(vertexX) || edge.getVertices()[0].equals(vertexY))) {
                graph.addEdge(vertexZ, edge.getVertices()[1]);
            }
            if ((edge.getVertices()[1].equals(vertexX) || edge.getVertices()[1].equals(vertexY))) {
                graph.addEdge(edge.getVertices()[0], vertexZ);
            }


            // remove the old edge
            graph.removeEdge(edge.getVertices()[0], edge.getVertices()[1]);
        }

        // remove the two vertices X and Y
        graph.removeVertex(vertexX);
        graph.removeVertex(vertexY);

        return new Contraction(vertexZ, selectedEdge);
    }

    public static Contraction contractRandomEdge(Graph graph) {

        List<DefaultEdge> edges = new ArrayList<>(graph.getEdgeSet());
        Collections.shuffle(edges);
        if (edges.size() <= 0) {
            throw new IllegalArgumentException("To contract a graph it is needed at least one edge");
        }

        DefaultEdge defaultEdgeToContract = edges.get(0);
        return contractEdge(graph, defaultEdgeToContract);
    }


}
