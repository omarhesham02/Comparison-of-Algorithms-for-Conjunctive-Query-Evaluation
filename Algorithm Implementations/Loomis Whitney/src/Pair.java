import java.util.ArrayList;
import java.util.Set;

public class Pair {
    public Relation first;
    public Relation second;

    public Pair(Relation first, Relation second) {
        this.first = first;
        this.second = second;
    }

    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    public String getFirst() {
        return first.toString();
    }

    public String getSecond() {
        return second.toString();
    }

}
