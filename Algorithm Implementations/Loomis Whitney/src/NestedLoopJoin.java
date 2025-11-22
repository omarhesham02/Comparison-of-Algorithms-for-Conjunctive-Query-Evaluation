import java.util.ArrayList;
import java.util.Arrays;

public class NestedLoopJoin {
    public static Relation NestedJoin(ArrayList<Relation> relations) {
        // Check if there are at least two relations
        if (relations.size() < 2) {
            throw new IllegalArgumentException("At least two relations are required");
        }

        // Initialize the result as the first relation
        Relation result = relations.get(0);

        // Join the remaining relations with the result
        for (int i = 1; i < relations.size(); i++) {
            result = TwoRelationNestedLoopJoin(result, relations.get(i));
        }

        return result;
    }

    private static Relation TwoRelationNestedLoopJoin(Relation R1, Relation R2) {
    // Get the common attributes
    String[] commonAttributes = getCommonAttributes(R1, R2);

    // Make a new relation with the attributes of the two relations
    ArrayList<String> newAttributes = new ArrayList<>(Arrays.asList(R1.getAttributes()));
    for (String attribute : R2.getAttributes()) {
        if (!newAttributes.contains(attribute)) {
            newAttributes.add(attribute);
        }
    }
    Relation result = new Relation(newAttributes.toArray(new String[0]));

    for (Tuple t1 : R1.getTuples()) {
        for (Tuple t2 : R2.getTuples()) {
            boolean match = true;
            // Check if the tuples match on the common attributes
            for (String attribute : commonAttributes) {
                int index1 = Arrays.asList(R1.getAttributes()).indexOf(attribute);
                int index2 = Arrays.asList(R2.getAttributes()).indexOf(attribute);
                if (!t1.getValues()[index1].equals(t2.getValues()[index2])) {
                    match = false;
                    break;
                }
            }
            if (match) {
                // Create a new tuple with the values from both tuples
                ArrayList<String> newValues = new ArrayList<>(Arrays.asList(t1.getValues()));
                for (String attribute : R2.getAttributes()) {
                    if (!Arrays.asList(R1.getAttributes()).contains(attribute)) {
                        int index = Arrays.asList(R2.getAttributes()).indexOf(attribute);
                        newValues.add(t2.getValues()[index]);
                    }
                }
                result.addTuple(new Tuple(newValues.toArray(new String[0])));
            }
        }
    }

    return result;
}

    private static String[] getCommonAttributes(Relation r1, Relation r2) {

        ArrayList<String> commonAttributes = new ArrayList<>();
        for (String attribute : r1.getAttributes()) {
            if (Arrays.asList(r2.getAttributes()).contains(attribute)) {
                commonAttributes.add(attribute);
            }
        }
        return commonAttributes.toArray(new String[0]);
    }

    public static void main(String[] args) {
        // Example input sets
        Relation R1 = new Relation(new String[]{"A", "B", "C"});
        R1.addTuple(new Tuple(new String[]{"1", "2", "6"}));
        R1.addTuple(new Tuple(new String[]{"4", "5", "6"}));
        R1.addTuple(new Tuple(new String[]{"7", "8", "9"}));

        Relation R2 = new Relation(new String[]{"B", "C", "D"});
        R2.addTuple(new Tuple(new String[]{"2", "3", "4"}));
        R2.addTuple(new Tuple(new String[]{"5", "6", "7"}));
        R2.addTuple(new Tuple(new String[]{"8", "9", "10"}));

        Relation R3 = new Relation(new String[]{"C", "D", "E"});
        R3.addTuple(new Tuple(new String[]{"3", "4", "5"}));
        R3.addTuple(new Tuple(new String[]{"6", "7", "8"}));
        R3.addTuple(new Tuple(new String[]{"9", "10", "11"}));

        Relation R4 = new Relation(new String[]{"D", "F", "A"});
        R4.addTuple(new Tuple(new String[]{"4", "5", "1"}));
        R4.addTuple(new Tuple(new String[]{"7", "8", "4"}));
        R4.addTuple(new Tuple(new String[]{"10", "12", "7"}));



        ArrayList<Relation> relations = new ArrayList<>();
        relations.add(R1);
        relations.add(R2);
        relations.add(R3);
        relations.add(R4);


        Relation result = NestedJoin(relations);
        System.out.println(result);
    }

}
