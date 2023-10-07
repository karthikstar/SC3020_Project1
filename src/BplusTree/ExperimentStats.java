package BplusTree;

public class ExperimentStats {

    private static int totalNumberOfNodes;
    private static int totalHeight;
    private static int totalNumberOfNodeReadQueries;
    private static int totalNumberOfRangeQueries;



    public int getTotalNumberOfNodes() {
        return totalNumberOfNodes;
    }

    public static void addOneNode() {
        totalNumberOfNodes++;
    }

    public static void deleteOneNode() {
        totalNumberOfNodes--;
    }

    public int getTotalHeight() {
        return totalHeight;
    }

    public static void addOneLevel() {
        totalHeight++;
    }

    public static void deleteOneLevel() {
        totalHeight--;
    }

    public int getTotalNumberOfNodeReadQueries() {
        return totalNumberOfNodeReadQueries;
    }

    public static void addOneNodeReadQuery() {
        totalNumberOfNodeReadQueries++;
    }

    public int getTotalNumberOfRangeQueries() {
        return totalNumberOfRangeQueries;
    }

    public static void addOneRangeQuery() {
        totalNumberOfRangeQueries++;
        addOneNodeReadQuery();
    }

}
