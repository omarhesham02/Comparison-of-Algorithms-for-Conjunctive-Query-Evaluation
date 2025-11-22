import java.util.ArrayList;

public class SearchTree {
    public static class SearchTreeNode {
        private ArrayList<SearchTreeNode> children;
        private String value;
    }

    private SearchTreeNode root;

    public SearchTree() {
        root = new SearchTreeNode();
        root.children = new ArrayList<>();
        root.value = "";
    }

    public void constructTree(Relation R, String[] attributes) {

    }
}
