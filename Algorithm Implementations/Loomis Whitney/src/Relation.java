import java.util.*;

public class Relation {
    public Set<Tuple> tuples;
    public String[] attributes;

    public Relation() {
        this.tuples = new HashSet<>();
        this.attributes = new String[0];
    }
    public Relation(String[] attributes) {
        this.tuples = new HashSet<>();
        this.attributes = attributes;
    }
    public Relation(Set<Tuple> tuples, String[] attributes) {
        this.tuples = tuples;
        this.attributes = attributes;
    }

    public Relation(Set<Tuple> tuples) {
        this.tuples = tuples;
        // Check if all the tuples have the same attributes
        String[] firstAttributes = tuples.iterator().next().getAttributes();
        for (Tuple tuple : tuples) {
            if (!Arrays.equals(tuple.getAttributes(), firstAttributes)) {
                throw new IllegalArgumentException("All tuples must have the same attributes");
            }
        }
        this.attributes = firstAttributes;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Attributes: ");
        sb.append(Arrays.toString(attributes));
        sb.append("\n");
        for (Tuple tuple : tuples) {
            sb.append(Arrays.toString(tuple.getValues()));
            sb.append("\n");
        }
        return sb.toString();
    }


    public String[] getAttributes() {
        return attributes;
    }

    public Set<Tuple> getTuples() {
        return tuples;
    }

    public void addTuple (String[] values) {
        // Check if the tuple has the same attributes as the relation
        if (values.length != attributes.length) {
            throw new IllegalArgumentException("Invalid number of values");
        }
        tuples.add(new Tuple(attributes, values));
    }

    public static Relation join(Relation R1, Relation R2) {
        // Make a set containing all the attributes from R1 and R2
        Set<String> commonAttributes = new HashSet<>(Arrays.asList(R1.getAttributes()));
        commonAttributes.retainAll(Arrays.asList(R2.getAttributes()));

        // Make a list with the attributes of the two relations, removing the common attributes from R2
        List<String> allAttributes = new ArrayList<>(Arrays.asList(R1.getAttributes()));
        allAttributes.addAll(Arrays.stream(R2.getAttributes()).filter(a -> !commonAttributes.contains(a)).toList());

        // Sort the attributes alphabetically
        Collections.sort(allAttributes);

        // Make a new relation with the sorted attributes
        Relation result = new Relation(allAttributes.toArray(new String[0]));

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
                    // Concatenate the values of the two tuples, such that the values of t1 come before the values of t2 in the result, but the common attributes are only included once
                    String[] values = new String[result.getAttributes().length];
                    for (int i = 0; i < result.getAttributes().length; i++) {
                        if (Arrays.asList(R1.getAttributes()).contains(result.getAttributes()[i])) {
                            values[i] = t1.getValues()[Arrays.asList(R1.getAttributes()).indexOf(result.getAttributes()[i])];
                        } else if (Arrays.asList(R2.getAttributes()).contains(result.getAttributes()[i])) {
                            values[i] = t2.getValues()[Arrays.asList(R2.getAttributes()).indexOf(result.getAttributes()[i])];
                        }
                    }
                    result.addTuple(values);
                }
            }
        }
        return result;
    }

    // Method to compute the semijoin of two relations
    public static Relation semijoin(Relation R1, Relation R2) {
        Set<String> commonAttributes = new HashSet<>(Arrays.asList(R1.getAttributes()));
        commonAttributes.retainAll(Arrays.asList(R2.getAttributes()));

        // Make a new relation with the attributes of the two relations, removing the common attributes from R2
        Relation result = new Relation(R1.getAttributes());
        for (String attribute : commonAttributes) {
            result.getAttributes()[Arrays.asList(result.getAttributes()).indexOf(attribute)] = R2.getAttributes()[Arrays.asList(R2.getAttributes()).indexOf(attribute)];
        }
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
                    // Keep the tuple from R1
                    result.addTuple(t1.getValues());
                }
            }
        }
        return result;
    }


    // Method to project a relation on a set of attributes
    public static Relation project(Relation R, String[] attributes) {

        if (attributes.length == 0) return R;

        if (R.attributes.length == 0) return R;

        // Check if the attributes are in the relation
        for (String attribute : attributes) {
            if (!Arrays.asList(R.getAttributes()).contains(attribute)) {
                throw new IllegalArgumentException("The attribute " + attribute + " is not in the relation");
            }
        }
        // Make a new relation with the specified attributes
        Relation result = new Relation(attributes);
        for (Tuple t : R.getTuples()) {
            // Get the values of the tuple that correspond to the attributes
            String[] values = new String[attributes.length];
            for (int i = 0; i < attributes.length; i++) {
                int index = Arrays.asList(R.getAttributes()).indexOf(attributes[i]);
                values[i] = t.getValues()[index];
            }
            result.addTuple(values);
        }
        return result;

    }

    public static Relation tSection(Relation R, Tuple t) {
        String[] S = t.getAttributes();
        String[] A = R.getAttributes();

        Set<Tuple> tupleSet = new HashSet<>();
        tupleSet.add(t);

        Relation tsRelation = new Relation(tupleSet);
        // R[ts] = projection of R semijion ts on a difference s
        Relation Rts = semijoin(R, tsRelation);
        Rts = project(Rts, Arrays.stream(A).filter(a -> !Arrays.asList(S).contains(a)).toArray(String[]::new));
        return Rts;

    }

    public int size() {
        return tuples.size();
    }

    public void addTuple(Tuple newTuple) {
        // Check if the tuple has the same attributes as the relation
        if (newTuple.getAttributes().length == 0) {
            newTuple.setAttributes(attributes);
        } else if (!Arrays.equals(newTuple.getAttributes(), attributes)) {
            throw new IllegalArgumentException("Invalid attributes");
        }

        tuples.add(newTuple);
    }

    public void addTuples(Tuple[] array) {
        for (Tuple tuple : array) {
            // Check if the tuple has the same attributes as the relation
            if (!Arrays.equals(tuple.getAttributes(), attributes)) {
                throw new IllegalArgumentException("Invalid attributes");
            }
            addTuple(tuple);
        }
    }

    public void removeTuples(Set<Tuple> tuples) {
        this.tuples.removeAll(tuples);
    }

    public void removeAttributes(String[] attributes) {
        List<String> newAttributes = new ArrayList<>(Arrays.asList(this.attributes));
        newAttributes.removeAll(Arrays.asList(attributes));
        this.attributes = newAttributes.toArray(new String[0]);
        for (Tuple tuple : tuples) {
            tuple.removeAttributes(attributes);
        }
    }

    public void keepAttributes(String[] attributes) {
        List<String> newAttributes = new ArrayList<>(Arrays.asList(this.attributes));
        newAttributes.retainAll(Arrays.asList(attributes));
        this.attributes = newAttributes.toArray(new String[0]);
        for (Tuple tuple : tuples) {
            tuple.keepAttributes(attributes);
        }
    }
}
