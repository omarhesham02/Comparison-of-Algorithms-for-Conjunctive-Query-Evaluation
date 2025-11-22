import java.util.ArrayList;
import java.util.List;

public class QueryPlanTree {

    private static ArrayList<ArrayList<String>> E;

    public QueryPlanTree() {
        E = new ArrayList<>();
    }

    public static class QueryPlanTreeNode {
        public final ArrayList<String> universe;
        public QueryPlanTreeNode left;
        public QueryPlanTreeNode right;
        public int label;

        public QueryPlanTreeNode(ArrayList<String> universe) {
            this.universe = universe;
            this.left = null;
            this.right = null;
        }
    }


    private QueryPlanTreeNode BuildTree(ArrayList<String> U, int k) {
        // if e_i intersection U is empty for all i in k return null
        boolean intersectionEmpty = true;
        for (int i = 0; i <= k; i++) {
            ArrayList<String> intersection = new ArrayList<>(E.get(i));
            intersection.retainAll(U);
            if (!intersection.isEmpty()) {
                intersectionEmpty = false;
                break;
            }
        }

        if (intersectionEmpty) {
            return null;
        }

        // Create a node u with Label(u) = k and universe(u) = U
        QueryPlanTreeNode u = new QueryPlanTreeNode(U);
        u.label = k;

        // if k > 1 and there exists an i in k such that e_i is not a subset of U
        if (k > 1) {
            boolean UIsSubsetOfE = true;
            for (int i = 0; i <= k; i++) {
                if (!E.get(i).containsAll(U)) {
                    UIsSubsetOfE = false;
                    break;
                }
            }
            if (!UIsSubsetOfE) {
                ArrayList<String> difference = new ArrayList<>(U);
                difference.removeAll(E.get(k));
                u.left = BuildTree(new ArrayList<>(difference), k - 1);

                ArrayList<String> intersectionWithU = new ArrayList<>(U);
                intersectionWithU.retainAll(E.get(k));
                u.right = BuildTree(new ArrayList<>(intersectionWithU), k - 1);
            }
        }
        return u;
    }
    public QueryPlanTreeNode ConstructQueryPlanTree(ArrayList<String> V, ArrayList<Relation> relations) {

        for (Relation relation : relations) {
            E.add(new ArrayList<>(List.of(relation.getAttributes())));
        }

        int m = E.size() - 1;

        return BuildTree(V, m);
    }

    public void printTotalOrder(QueryPlanTreeNode u) {
        if (u.right == null && u.left == null) {
            System.out.println(u.universe);
        } else if (u.left == null) {
            printTotalOrder(u.right);
        } else if (u.right == null) {
            printTotalOrder(u.left);
            // Print all attributes in universe(u) that are not in universe(u.left)
            ArrayList<String> difference = new ArrayList<>(u.universe);
            difference.removeAll(u.left.universe);
            System.out.println(difference);
        } else {
            printTotalOrder(u.left);
            printTotalOrder(u.right);
        }
    }

    public void printUniverses(QueryPlanTreeNode u) {
        System.out.print(u.universe);
        System.out.println(" " + u.label);
        if (u.left == null && u.right == null) {
            System.out.println("Leaf ^^ ");
        }
        if (u.left != null) {
            System.out.println("Left: ");
            printUniverses(u.left);
        }
        if (u.right != null) {
            System.out.println("Right: ");
            printUniverses(u.right);
        }

    }


    public static void main(String[] args) {
        ArrayList<String> V = new ArrayList<>();
        V.add("1");
        V.add("2");
        V.add("3");
        V.add("4");
        V.add("5");
        V.add("6");

        ArrayList<ArrayList<String>> E = new ArrayList<>();
        ArrayList<String> e1 = new ArrayList<>();
        e1.add("1");
        e1.add("4");
        e1.add("5");
        e1.add("6");
        E.add(e1);

        ArrayList<String> e2 = new ArrayList<>();
        e2.add("1");
        e2.add("4");
        e2.add("5");
        E.add(e2);

        ArrayList<String> e3 = new ArrayList<>();
        e3.add("2");
        e3.add("3");
        E.add(e3);

        ArrayList<String> e4 = new ArrayList<>();
        e4.add("3");
        e4.add("6");
        E.add(e4);

        Relation Ra = new Relation(e1.toArray(new String[0]));
        Relation Rb = new Relation(e2.toArray(new String[0]));
        Relation Rc = new Relation(e3.toArray(new String[0]));
        Relation Rd = new Relation(e4.toArray(new String[0]));


        ArrayList<Relation> relations = new ArrayList<>();
        relations.add(Ra);
        relations.add(Rb);
        relations.add(Rc);
        relations.add(Rd);



        QueryPlanTree qpt = new QueryPlanTree();
        QueryPlanTreeNode root = qpt.ConstructQueryPlanTree(V, relations);
        qpt.printTotalOrder(root);
    }
}
