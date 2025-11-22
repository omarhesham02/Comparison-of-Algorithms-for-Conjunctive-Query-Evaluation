import java.util.*;
import java.util.stream.Stream;

public class RecursiveJoin {

    public static Relation RecursiveJoin(QueryPlanTree.QueryPlanTreeNode U, ArrayList<Double> y, Tuple ts, ArrayList<Relation> relations) {
        Relation ret = new Relation();
        int k = U.label;
        Relation tsRelation = new Relation(Collections.singleton(ts), ts.getAttributes());

        // if u is a leaf node then
        if (U.left == null && U.right == null) {
            // j <- argmin i in k | projection of R_ei[t_s intersection e_i] | is minimum
            int j = 0;

            for (int i = 0; i < relations.size(); i++) {
                // Project t on the attributes of R_ei
                Relation projection = Relation.project(tsRelation, relations.get(i).getAttributes());
                // Project the projection on U and count the number of tuples
                Relation projectionOnU = Relation.project(projection, U.universe.toArray(new String[0]));

                // j is the i that minimizes the number of tuples in the projection
                if (projectionOnU.size() < projectionOnU.size()) {
                    j = i;
                }
            }

            // For each tuple t_U in the projection of R_ej[t s intersection e_j] on U
            Relation projection = Relation.project(tsRelation, relations.get(j).getAttributes());
            Relation projectionOnU = Relation.project(projection, U.universe.toArray(new String[0]));
            for (Tuple tU : projectionOnU.getTuples()) {
                // If tU is in the projection of R_ei[t s intersection e_i] on U for all i in k \ {j}
                boolean inAll = true;
                for (int i = 0; i < relations.size(); i++) {
                    if (i == j) {
                        continue;
                    }
                    Relation projectionI = Relation.project(tsRelation, relations.get(i).getAttributes());
                    Relation projectionIOnU = Relation.project(projectionI, U.universe.toArray(new String[0]));
                    if (!projectionIOnU.getTuples().contains(tU)) {
                        inAll = false;
                        break;
                    }
                }
                if (inAll) {
                    // Concatenate the attributes of ts and tU
                    String[] combinedAttributes = Stream.concat(Arrays.stream(ts.getAttributes()), Arrays.stream(tU.getAttributes()))
                            .toArray(String[]::new);

                    // Concatenate the values of ts and tU
                    String[] combinedValues = Stream.concat(Arrays.stream(ts.getValues()), Arrays.stream(tU.getValues()))
                            .toArray(String[]::new);

                    // Create a new tuple with the combined attributes and values
                    Tuple combinedTuple = new Tuple(combinedAttributes, combinedValues);
                    ret.addTuple(combinedTuple);
                }
            }
            return ret;
        }
        // if u.left is null then L is ts
        Relation L;
        if (U.left == null) {
            L = new Relation(Collections.singleton(ts), ts.getAttributes());
        } else {
            // L <- RecursiveJoin(u.left, y, ts, R)
            L = RecursiveJoin(U.left, y, ts, relations);
        }

        // W = U \ e_k, W- = e_k intersection U
        Relation W = new Relation(U.universe.toArray(new String[0]));
        // Remove the attributes of e_k from W
        W.removeAttributes(relations.get(k).getAttributes());

        // W- = U intersection e_k
        Relation WMinus = new Relation(U.universe.toArray(new String[0]));
        // Keep only the attributes of e_k in WMinus
        WMinus.keepAttributes(relations.get(k).getAttributes());


        // If W- is empty return L
        if (WMinus.size() == 0) {
            return L;
        }

        // For each tuple t_(s u w) = (ts, tw) in L do
        for (Tuple tsw : L.getTuples()) {
            // Split t_(s u w) into ts and tw
            // where ts has the attributes s and tw has the attributes w
            Tuple ts1 = new Tuple(Arrays.copyOfRange(tsw.getAttributes(), 0, ts.getAttributes().length),
                    Arrays.copyOfRange(tsw.getValues(), 0, ts.getValues().length));
            Tuple tw = new Tuple(Arrays.copyOfRange(tsw.getAttributes(), ts.getAttributes().length, tsw.getAttributes().length),
                    Arrays.copyOfRange(tsw.getValues(), ts.getValues().length, tsw.getValues().length));


            // if y_ek >= 1 then
            if (y.get(k) < 1) {
                // Project tsw on the attributes of R_ei
                Relation projection = Relation.project(new Relation(Collections.singleton(tsw), tsw.getAttributes()), relations.get(k).getAttributes());
                // Perform the semijoin of R_ei and projection
                Relation semijoin = Relation.semijoin(relations.get(k), projection);
                // Project the semijoin on e_i intersection W-
                String[] intersectionAttributes = Stream.concat(Arrays.stream(relations.get(k).getAttributes()), Arrays.stream(WMinus.getAttributes()))
                        .toArray(String[]::new);
                Relation projectionOnIntersection = Relation.project(semijoin, intersectionAttributes);

                // If the product of the size of projectionOnIntersection from i = 1 to k - 1 is smaller than y_ek
                double product = 1;
                for (int i = 0; i < k - 1; i++) {
                    double power = y.get(i) / (1.0 - y.get(k));
                    product *= Math.pow(Relation.project(projectionOnIntersection, relations.get(i).getAttributes()).size(), power);
                }

                // compute t_s intersection e_k semi-join R_e_k
                Relation tsIntersectionEK = Relation.semijoin(tsRelation, relations.get(k));
                // Project the result on W-
                Relation projectionOnWMinus = Relation.project(tsIntersectionEK, WMinus.getAttributes());

                // If the product is smaller than the size of projectionOnWMinus
                if (product < projectionOnWMinus.size()) {
                    // Z = RecursiveJoin(u.right, (y_ei / (1 - y_ek)) for all i in k, tsw, R)
                    ArrayList<Double> newY = new ArrayList<>(y);
                    for (int i = 0; i < k; i++) {
                        newY.set(i, y.get(i) / (1 - y.get(k)));
                    }
                    // for each tuple ts, tw, tw- in Z do
                    Relation Z = RecursiveJoin(U.right, newY, tsw, relations);
                    for (Tuple t : Z.getTuples()) {
                        // Split the tuple into ts, tw, and tw-
                        Tuple ts2 = new Tuple(Arrays.copyOfRange(t.getAttributes(), 0, ts.getAttributes().length),
                                Arrays.copyOfRange(t.getValues(), 0, ts.getValues().length));
                        Tuple tw1 = new Tuple(Arrays.copyOfRange(t.getAttributes(), ts.getAttributes().length, ts.getAttributes().length + tw.getAttributes().length),
                                Arrays.copyOfRange(t.getValues(), ts.getValues().length, ts.getValues().length + tw.getValues().length));
                        Tuple twMinus = new Tuple(Arrays.copyOfRange(t.getAttributes(), ts.getAttributes().length + tw.getAttributes().length, t.getAttributes().length),
                                Arrays.copyOfRange(t.getValues(), ts.getValues().length + tw.getValues().length, t.getValues().length));

                        Relation ts2Relation = new Relation(Collections.singleton(ts2), ts2.getAttributes());

                        // compute R_ek[t_s intersection e_k]
                        Relation tsIntersectionEK2 = Relation.semijoin(ts2Relation, relations.get(k));
                        // Project the result on W-
                        Relation projectionOnWMinus2 = Relation.project(tsIntersectionEK2, WMinus.getAttributes());

                        // If tMinus is in projectionOnWMinus2
                        if (projectionOnWMinus2.getTuples().contains(twMinus)) {
                            // Concatenate the attributes of ts, tw, and tw-
                            String[] combinedAttributes = Stream.concat(Arrays.stream(ts.getAttributes()),
                                    Stream.concat(Arrays.stream(tw.getAttributes()), Arrays.stream(twMinus.getAttributes())))
                                    .toArray(String[]::new);

                            // Concatenate the values of ts, tw, and tw-
                            String[] combinedValues = Stream.concat(Arrays.stream(ts.getValues()),
                                    Stream.concat(Arrays.stream(tw.getValues()), Arrays.stream(twMinus.getValues())))
                                    .toArray(String[]::new);

                            // Create a new tuple with the combined attributes and values
                            Tuple combinedTuple = new Tuple(combinedAttributes, combinedValues);
                            ret.addTuple(combinedTuple);
                        } else {
                            // For each tuple t_w- in projection of R_ek[ts intersection e_k] on W- do
                            for (Tuple twMinus2 : projectionOnWMinus2.getTuples()) {
                                // If t_{e_i intersection W-} is in projection of R_ei[t_(s union w) intersection e_i] for all e_i such that i < k and e_i intersection W- is not empty
                                boolean inAll = true;
                                for (int i = 0; i < k; i++) {
                                    if (relations.get(i).getAttributes().length == 0) {
                                        continue;
                                    }
                                    Relation projectionI = Relation.project(new Relation(Collections.singleton(t), t.getAttributes()), relations.get(i).getAttributes());
                                    Relation projectionIOnIntersection = Relation.project(projectionI, Stream.concat(Arrays.stream(ts.getAttributes()), Arrays.stream(tw.getAttributes())).toArray(String[]::new));
                                    if (!projectionIOnIntersection.getTuples().contains(twMinus2)) {
                                        inAll = false;
                                        break;
                                    }
                                }
                                if (inAll) {
                                    // Concatenate the attributes of ts, tw, and tw-
                                    String[] combinedAttributes = Stream.concat(Arrays.stream(ts.getAttributes()),
                                            Stream.concat(Arrays.stream(tw.getAttributes()), Arrays.stream(twMinus2.getAttributes())))
                                            .toArray(String[]::new);

                                    // Concatenate the values of ts, tw, and tw-
                                    String[] combinedValues = Stream.concat(Arrays.stream(ts.getValues()),
                                            Stream.concat(Arrays.stream(tw.getValues()), Arrays.stream(twMinus2.getValues())))
                                            .toArray(String[]::new);

                                    // Create a new tuple with the combined attributes and values
                                    Tuple combinedTuple = new Tuple(combinedAttributes, combinedValues);
                                    ret.addTuple(combinedTuple);

                                }
                            }
                        }

                    }
                }


                }
            }
        return ret;
        }

    public static void main(String[] args) {
        // Create a QueryPlanTree
        QueryPlanTree.QueryPlanTreeNode U = new QueryPlanTree.QueryPlanTreeNode(new ArrayList<>(Arrays.asList("A", "B", "C")));
        U.label = 2;
        QueryPlanTree.QueryPlanTreeNode left = new QueryPlanTree.QueryPlanTreeNode(new ArrayList<>(Arrays.asList("A", "B")));
        left.label = 1;
        QueryPlanTree.QueryPlanTreeNode right = new QueryPlanTree.QueryPlanTreeNode(new ArrayList<>(Arrays.asList("B", "C")));
        right.label = 1;
        U.left = left;
        U.right = right;

        // Create a list of y values
        ArrayList<Double> y = new ArrayList<>(Arrays.asList(0.5, 1.0, 1.5));


        // Create Relations
        Relation R1 = new Relation(new String[]{"A", "B"});
        Relation R2 = new Relation(new String[]{"A", "C"});
        Relation R3 = new Relation(new String[] {"B", "D"});



        R1.addTuple(new String[]{"1", "2"});
        R2.addTuple(new String[]{"2", "3"});
        R3.addTuple(new String[]{"1", "3"});

        Tuple ts = new Tuple();

        ArrayList<Relation> relations = new ArrayList<>();
        relations.add(R1);
        relations.add(R2);
        relations.add(R3);



        // Call the RecursiveJoin method
        Relation result = RecursiveJoin(U, y, ts, relations);
        System.out.println(result);

        // Print the result

    }
    }
