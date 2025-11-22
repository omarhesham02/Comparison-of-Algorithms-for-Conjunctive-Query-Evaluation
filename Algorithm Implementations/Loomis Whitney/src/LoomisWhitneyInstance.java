import java.util.*;
import java.util.concurrent.TimeUnit;

public class LoomisWhitneyInstance {

    // Represents a node in the binary tree T
    public static class BinaryTreeNode {
        private Set<String> label;
        private BinaryTreeNode left;
        private BinaryTreeNode right;

        public BinaryTreeNode(Set<String> label) {
            this.label = label;
        }

    }

    // Constructs T using Algorithm 1 from the paper
    public static BinaryTreeNode constructT(Set<String> vertexSet, Set<String> globalVertexSet) {
        // Base case: if vertex set has only one attribute, return the corresponding leaf node
        if (vertexSet.size() == 1) {
            // Make the label the difference between this vertex and the vertex set
            Set<String> label = new HashSet<>(globalVertexSet);
            label.removeAll(vertexSet);
            return new BinaryTreeNode(label);
        }

        // Divide the vertex set into two parts
        Set<String> leftVertexSet = new HashSet<>();
        Set<String> rightVertexSet = new HashSet<>();

        int i = 0;
        for (String vertex : vertexSet) {
            if (i < vertexSet.size() / 2) {
                leftVertexSet.add(vertex);
            } else {
                rightVertexSet.add(vertex);
            }
            i++;
        }

        // Recursively construct the left and right subtrees
        BinaryTreeNode left = constructT(leftVertexSet, globalVertexSet);
        BinaryTreeNode right = constructT(rightVertexSet, globalVertexSet);

        Set<String> intersection = new HashSet<>(left.label);
        intersection.retainAll(right.label);
        BinaryTreeNode root = new BinaryTreeNode(intersection);

        root.left = left;
        root.right = right;

        return root;
    }

    public static Pair LoomisWhitneyAlgorithm (ArrayList<Relation> relations, BinaryTreeNode u) {
        //P = product of e in E of Ne to the power 1/(n-1) where n is the number of attributes in the relation and E is the set of relations and Ne is the number of tuples in the relation
        double P = 1;
        double n = relations.size();

        for (Relation r : relations) {
            P *= Math.pow(r.size(), 1 / (n - 1));
        }

        // (C, D) = LW(relations, u)
        return LW(relations, u, P);
}

    private static Pair LW (ArrayList<Relation> relations, BinaryTreeNode x, double P) {
        Relation C = new Relation();
        Relation D;

        // If x is a leaf return the relation corresponding to the label of x
        if (x.left == null && x.right == null) {
            for (Relation r : relations) {
                if (Arrays.equals(r.getAttributes(), x.label.toArray(new String[0]))) {
                    return new Pair(new Relation(), r);
                }
            }
        }

        // (CL, DL) = LW(relations, x.left)
        assert x.left != null;
        Pair CLDL = LW(relations, x.left, P);
        // (CR, DR) = LW(relations, x.right)
        Pair CRDR = LW(relations, x.right, P);

        // F = Project(DL, x.label) intersect Project(DR, x.label)
        // DL is CLDL.second, DR is CRDR.second

     Relation ProjectDL = Relation.project(CLDL.second, new Relation(x.label.toArray(new String[0])).getAttributes());
     Relation ProjectDR = Relation.project(CRDR.second, new Relation(x.label.toArray(new String[0])).getAttributes());

     Relation F = keepCommonTuples(ProjectDL, ProjectDR);

     // G = all tuples in F such that they match at least one tuple in DL in values on the common attribute
        // If the attributes of F are not a subset of the attributes of DL, throw an exception
        if (!new HashSet<>(Arrays.asList(ProjectDL.getAttributes())).containsAll(Arrays.asList(F.getAttributes()))) {
            throw new IllegalArgumentException("The attributes of F must be a subset of the attributes of DL");
        }
        Relation G = new Relation(F.getAttributes());


        for (Tuple t : F.getTuples()) {
            int DLSectionSize = 0;
            // Size of tSection(DL) + 1 is <= P / size of DR
            Relation DLSection = Relation.tSection(CLDL.second, t);

            if (DLSection.size() + 1 <= P / CRDR.second.size()) {
                G.addTuple(t);
            }
        }

        // If x is the root of T
        if (x.label.isEmpty()) {

    // C = DL join DR union CL union CR
            Relation DLJoinDR = Relation.join(CLDL.second, CRDR.second);

            // The attributes of C are the union of the attributes of DL and DR
            String[] attributes = new String[CLDL.second.getAttributes().length + CRDR.second.getAttributes().length];
            System.arraycopy(CLDL.second.getAttributes(), 0, attributes, 0, CLDL.second.getAttributes().length);
            System.arraycopy(CRDR.second.getAttributes(), 0, attributes, CLDL.second.getAttributes().length, CRDR.second.getAttributes().length);
            // Sort the attributes alphabetically
            Arrays.sort(attributes);
            C.attributes = attributes;

            C.getTuples().addAll(DLJoinDR.getTuples());
            C.getTuples().addAll(CLDL.first.getTuples());
            C.getTuples().addAll(CRDR.first.getTuples());

    // D is the empty set
            D = new Relation();
        } else {
            // C = join(DL, DR) semijoin G
          C = Relation.semijoin(Relation.join(CLDL.second, CRDR.second), G);

        // D is F difference G
            D = new Relation(F.getAttributes());
            for (Tuple t : F.getTuples()) {
                if (!G.getTuples().contains(t)) {
                    D.addTuple(t.getValues());
                }
            }

        }
        return new Pair(C, D);
    }

// Method to keep common tuples in two sets of tuples
public static Relation keepCommonTuples(Relation R1, Relation R2) {
    // Get the common attributes
    List<String> commonAttributes = new ArrayList<>();
    for (String attribute : R1.getAttributes()) {
        if (Arrays.asList(R2.getAttributes()).contains(attribute)) {
            commonAttributes.add(attribute);
        }
    }

    // Project R1 and R2 on the common attributes
    Relation ProjectR1 = Relation.project(R1, commonAttributes.toArray(new String[0]));
    Relation ProjectR2 = Relation.project(R2, commonAttributes.toArray(new String[0]));

    // Keep only the tuples that are in both ProjectR1 and ProjectR2
    Set<Tuple> result = new HashSet<>();
    for (Tuple t : ProjectR1.getTuples()) {
        for (Tuple t2 : ProjectR2.getTuples()) {
            if (t.equals(t2)) {
                result.add(t);
            }
        }
    }
    return new Relation(result, commonAttributes.toArray(new String[0]));
}

public static Relation prune (Relation C, ArrayList<Relation> relations) {
    // Iterate over the tuples in C and keep only the tuples whose projections on the attributes of each relation are in the relation
    Relation result = new Relation(C.getAttributes());
    for (Tuple t : C.getTuples()) {
        boolean keep = false;
        for (Relation r : relations) {
          // Project t on the attributes of r
            String[] values = new String[r.getAttributes().length];
            for (int i = 0; i < r.getAttributes().length; i++) {
                int index = Arrays.asList(C.getAttributes()).indexOf(r.getAttributes()[i]);
                values[i] = t.getValues()[index];
            }
            Relation projection = Relation.project(C, r.getAttributes());
            if (r.getTuples().contains(new Tuple(r.getAttributes(), values))) {
                keep = true;
            } else {
                keep = false;
                break;
            }
        }
        if (keep) {
            result.addTuple(t.getValues());
        }
    }

    return result;
}
public static void printT(BinaryTreeNode u) {
        if (u == null) {
            return;
        }

        System.out.println(u.label);

        printT(u.left);
        printT(u.right);
}
    public static void main(String[] args) throws InterruptedException {


        Relation R = new Relation(new String[]{"A", "B"});
        Relation S = new Relation(new String[]{"B", "C"});
        Relation T = new Relation(new String[]{"A", "C"});

        // Create a random number generator
        Random rand = new Random();

//         Add 100 tuples to R
        for (int i = 0; i < 100000; i++) {
            String[] values = new String[]{String.valueOf(i), String.valueOf(rand.nextInt(50))};
            R.addTuple(values);
        }

        // Add 50 tuples to S
        for (int i = 0; i < 50000; i++) {
            String[] values = new String[]{String.valueOf(i), String.valueOf(rand.nextInt(10))};
            S.addTuple(values);
        }

        // Add 10 tuples to T
        for (int i = 0; i < 50000; i++) {
            String[] values = new String[]{String.valueOf(i), String.valueOf(i)};
            T.addTuple(values);
        }
        

        ArrayList<Relation> relations = new ArrayList<>();
        relations.add(R);
        relations.add(S);
        relations.add(T);

        Set<String> globalVertexSet = new HashSet<>();
        globalVertexSet.addAll(Arrays.asList(R.getAttributes()));
        globalVertexSet.addAll(Arrays.asList(S.getAttributes()));
        globalVertexSet.addAll(Arrays.asList(T.getAttributes()));


        // Construct the binary tree T
        BinaryTreeNode u = constructT(globalVertexSet, globalVertexSet);

        // Compute the Loomis-Whitney algorithm

        long startTime = System.nanoTime();
//        System.out.println(prune(LoomisWhitneyAlgorithm(relations, u).first, relations));
        long stopTime = System.nanoTime();
        double elapsedTime = (double) (stopTime - startTime) / 1_000_000_000;
        System.out.printf("Elapsed time: %.3f seconds%n", elapsedTime);

        long startTime2 = System.nanoTime();
        System.out.println(NestedLoopJoin.NestedJoin(relations));
        long stopTime2 = System.nanoTime();
        double elapsedTime2 = (double) (stopTime2 - startTime2) / 1_000_000_000;
        System.out.printf("Elapsed time: %.3f seconds%n", elapsedTime2);

    }
}

