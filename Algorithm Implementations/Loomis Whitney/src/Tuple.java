import java.util.Arrays;
import java.util.HashMap;

public class Tuple {
    private final String[] values;
    private String[] attributes;
    private final HashMap<String, String> valueMap;


    public Tuple(String[] attributes, String[] values) {
        if (attributes.length != values.length) {
            throw new IllegalArgumentException("Attributes and values must be the same length");
        }

        this.attributes = attributes;
        this.values = values;

        valueMap = new HashMap<>();
        for (int i = 0; i < attributes.length; i++) {
            valueMap.put(attributes[i], values[i]);
        }

    }
    public Tuple (String[] values) {
        this.values = values;
        this.attributes = new String[]{};
        valueMap = new HashMap<>();
    }

    public Tuple(HashMap<String, String> valueMap) {
        this.valueMap = valueMap;
        this.attributes = new String[]{};
        this.values = new String[]{};

    }

    public Tuple() {
        this.attributes = new String[]{};
        this.values = new String[]{};
        valueMap = new HashMap<>();
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i]);
            if (i < values.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public String[] getValues() {
        return values;
    }


@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Tuple tuple = (Tuple) o;
    return Arrays.equals(values, tuple.values) && Arrays.equals(attributes, tuple.attributes);
}

@Override
public int hashCode() {
    int result = Arrays.hashCode(values);
    result = 31 * result + Arrays.hashCode(values);
    return result;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public void removeAttributes(String[] attributes) {
        for (String attribute : attributes) {
            valueMap.remove(attribute);
        }
        // Remove the attributes from the attributes array
        String[] newAttributes = new String[this.attributes.length - attributes.length];
        int index = 0;
        for (String attribute : this.attributes) {
            if (!Arrays.asList(attributes).contains(attribute)) {
                newAttributes[index] = attribute;
                index++;
            }
        }
        this.attributes = newAttributes;

    }

    public void keepAttributes(String[] attributes) {
        for (String attribute : valueMap.keySet()) {
            if (!Arrays.asList(attributes).contains(attribute)) {
                valueMap.remove(attribute);
            }
        }
        this.attributes = attributes;

        // Remove the attributes from the attributes array
        String[] newAttributes = new String[this.attributes.length - attributes.length];
        int index = 0;
        for (String attribute : this.attributes) {
            if (Arrays.asList(attributes).contains(attribute)) {
                newAttributes[index] = attribute;
                index++;
            }
        }
        this.attributes = newAttributes;

    }

    public void setAttributes(String[] attributes) {
        this.attributes = attributes;
    }
}
