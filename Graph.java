import java.util.*;

public class Graph {

    private static RandomGenerator rnd;

    private int PRODUCTS;
    private boolean DEBUG;
    private boolean buyTogether[][];
    private Map<Integer, Product> products = new HashMap<>();
    private Map<Integer, ArrayList<Product>> graph;
    private Set<Edge> graphEdges = new HashSet<>();

    public Graph(int n, RandomGenerator rnd, boolean debug) {
        PRODUCTS = n;
        DEBUG = debug;
        if (DEBUG) System.out.print("[DEBUG] Creating instances of needed data structures... ");
        buyTogether = new boolean[n][n];
        graph = new HashMap<>();
        Graph.rnd = rnd;
        if (DEBUG) System.out.println("[OK] DONE.");
    }

    public void fill() {

        if (DEBUG) System.out.print("[DEBUG] Initializing random products... ");
        //Initialize random products
        for (int i = 0; i < PRODUCTS; i++) {

            products.put(i, new Product(rnd.stringRandom(), rnd.getRnd().nextInt(10) + 1,
                    Math.round(rnd.getRnd().nextDouble() * 100.0)));

            graph.put(i, new ArrayList<>());
        }
        if (DEBUG) System.out.println("[OK] DONE.");

        if (DEBUG) System.out.print("[DEBUG] Matching products randomized, generating edges, filling graph... ");

        // Force graph to be at least a percentage connected
        while (!sparse()) {
            for (int i = 0; i < PRODUCTS; i++) {
                for (int j = i; j < PRODUCTS; j++) {
                    // A product always is bought with itself
                    if (i == j) {
                        buyTogether[i][j] = true;
                    } else {
                        // If i is bought with j then j is bought with i
                        buyTogether[i][j] = rnd.getRnd().nextBoolean();
                        buyTogether[j][i] = buyTogether[i][j];

                        if (buyTogether[i][j]) {
                            // Generate edge of the graph and save in the corresponding structures
                            Edge edge = new Edge(products.get(i), products.get(j));
                            graphEdges.add(edge);
                            products.get(i).addEdge(edge);
                            products.get(j).addEdge(edge);
                        }
                    }
                }
            }
        }
        if (DEBUG) System.out.println("[OK] DONE.");
    }

    private boolean sparse() {
        double sparse = ((double) graphEdges.size() * 2.0) / (PRODUCTS * (PRODUCTS - 1));
        if (Double.compare(sparse, 0.7) >= 0) {
            return true;
        } else {
            graphEdges.clear();
            for (int i = 0; i < PRODUCTS; i++) {
                products.get(i).getEdges().clear();
            }
            return false;
        }
    }

    public void minCutKarger() {
        if (DEBUG) System.out.println("[DEBUG] Karger's algorithm in progress...");
        while (graph.size() > 2) {
            //printGraph();
            if (DEBUG) System.out.println("[DEBUG] Selecting random edge to be removed");
            Edge edgeToRemove = getEdge(rnd.getRnd().nextInt(graphEdges.size()));
            Product p1 = edgeToRemove.getFirst();
            Product p2 = edgeToRemove.getOppositeEnd(p1);

            if (DEBUG) System.out.print("[DEBUG] Updating graph status... ");
            //graphEdges.removeAll(Collections.singleton(edgeToRemove));
            graphEdges.remove(edgeToRemove);
            p1.remove(edgeToRemove);
            p2.remove(edgeToRemove);
            if (DEBUG) System.out.println("[OK] DONE.");

            this.printProductsConnection();

            merge(p1, p2);

            this.printProductsConnection();
        }
        if (DEBUG) System.out.println("[OK] DONE.");
    }

    private void merge(Product p1, Product p2) {
        if (DEBUG) System.out.print("[DEBUG] Merging vertices " + getKeyProduct(p1) + " and " + getKeyProduct(p2) + " ");
        graph.get(getKeyProduct(p1)).add(p2);
        graph.get(getKeyProduct(p1)).addAll(graph.get(getKeyProduct(p2)));
        // Migrate all edges to the combined node
        Set<Edge> copy = p2.getEdges();
        for (Iterator<Edge> it = copy.iterator(); it.hasNext(); ) {
            Edge e = it.next();
            //System.out.println(edgeToString(e));
            it.remove();
            Product p = e.getOppositeEnd(p2);
            // Remove edge from graph and from product
            graphEdges.remove(e);
            p2.getEdges().remove(e);
            p.remove(e);
            this.printProductsConnection();
            // Set new value of edge that no longer exists
            e.replaceEndOfEdge(p2, p1);
            // Add modified edge to graph
            p1.addEdge(e);
            p.addEdge(e);
            graphEdges.add(e);
            this.printProductsConnection();
        }
        //System.out.println();

        graph.remove(getKeyProduct(p2));
        if (DEBUG) System.out.println("[OK] DONE.");
    }

    public void addProduct(Product p) {
        ++PRODUCTS;
        products.put(PRODUCTS, p);
    }

    private boolean wereBoughtTogether(int i, int j) {
        return buyTogether[i][j];
    }

    private Product getProduct(int id) {
        return products.get(id);
    }

    private int getKeyProduct(Product p) {
        for (Map.Entry<Integer, Product> e : products.entrySet()) {
            if (e.getValue().equals(p)) {
                return e.getKey();
            }
        }
        return 0;
    }

    private int getKeyGraph(ArrayList<Product> p) {
        for (Map.Entry<Integer, ArrayList<Product>> e : graph.entrySet()) {
            if (e.getValue().equals(p)) {
                return e.getKey();
            }
        }
        return 0;
    }

    private Edge getEdge(int i) {
        return ((Edge) graphEdges.toArray()[i]);
    }

    private String edgeToString(Edge edge) {
        return getKeyProduct(edge.getFirst()) + " - " + getKeyProduct(edge.getSecond());
    }

    /// Todavía no es definitivo pero para aclarar va bien
    public void printGraph(){
        System.out.println("GRAPH");
        System.out.println("=====\n");
        StringBuilder stringToPrint = new StringBuilder();

        for (Map.Entry<Integer, ArrayList<Product>> entry : graph.entrySet()) {
            int i = entry.getKey();
            stringToPrint.append("Node(");
            if (graph.get(i).isEmpty()){
                stringToPrint.append(i).append(')');
            } else {
                stringToPrint.append(getKeyGraph(graph.get(i))).append(',');
            }

            for (int j = 0; j < graph.get(i).size(); j++) {
                stringToPrint.append(getKeyProduct(graph.get(i).get(j))).append(',');
            }

            if (stringToPrint.toString().lastIndexOf(',') != -1) {
                stringToPrint.deleteCharAt(stringToPrint.toString().lastIndexOf(','));
            }

            if (graph.get(i).isEmpty()){
                stringToPrint.append(" : [");
            } else {
                stringToPrint.append(") : [");
            }

            for (int j = 0; j < products.get(i).getEdges().size(); j++) {
                stringToPrint.append(getKeyProduct(products.get(i).getEdge(j).getOppositeEnd(products.get(i)))).append(",");
            }

            if (stringToPrint.toString().lastIndexOf(',') != -1) {
                System.out.println(stringToPrint.toString().substring(0, stringToPrint.toString().length() - 1) + "]");
            } else {
                System.out.println(stringToPrint.toString() + "]");
            }

            stringToPrint.setLength(0);
        }
        System.out.println();
    }

    public void printInitialGraph(){
        System.out.println("INITIAL GRAPH");
        System.out.println("=============\n");
        StringBuilder stringToPrint = new StringBuilder();
        for (int i = 0; i < products.size(); i++) {
            stringToPrint.append(i).append(": [");
            for (int j = 0; j < products.get(i).getEdges().size(); j++) {
                stringToPrint.append(getKeyProduct(products.get(i).getEdge(j).getOppositeEnd(products.get(i)))).append(",");
            }

            if (stringToPrint.toString().lastIndexOf(',') != -1) {
                System.out.println(stringToPrint.toString().substring(0, stringToPrint.toString().length() - 1) + "]");
            } else {
                System.out.println(stringToPrint.toString() + "]");
            }

            stringToPrint.setLength(0);
        }
    }

    public void printBoughtTogether(){
        System.out.println("PRODUCTS BOUGHT TOGETHER TABLE");
        System.out.println("==============================\n");
        for (boolean[] aBuyTogether : buyTogether) {
            for (int j = 0; j < buyTogether[0].length; j++) {
                int aux = aBuyTogether[j] ? 1 : 0;
                System.out.print(aux + "  ");
            }
            System.out.print("\n");
        }
        System.out.println();
    }

    public void printProducts(){
        System.out.println();
        System.out.println("PRODUCT LIST");
        System.out.println("============\n");
        for (int i = 0; i < products.size(); i++) {
            System.out.println("Product_" + i + ": " + products.get(i).toString());
        }
        System.out.println();
    }

    public void printProductsConnection() {
        for (int i = 0; i < products.size(); i++) {
            System.out.print(i + ": ");
            for (int j = 0; j < products.get(i).getEdges().size(); j++) {
                Edge e = products.get(i).getEdge(j);
                System.out.print(getKeyProduct(e.getFirst()) + " - " + getKeyProduct(e.getOppositeEnd(e.getFirst())) + " ");
            }
            System.out.println();
        }
    }
}