import org.ssclab.log.SscLogger;
import org.ssclab.pl.milp.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FractionalEdgeCover {

    public static void FractionalEdgeCoverSolution(ArrayList<Relation> relations) throws Exception {
        // Collect unique vertices
        Set<String> uniqueVertices = new HashSet<>();
        ArrayList<ArrayList<String>> edges = new ArrayList<>();
        for (Relation relation : relations) {
            for (String attribute : relation.getAttributes()) {
                uniqueVertices.add(attribute);
            }
            edges.add(new ArrayList<>(List.of(relation.getAttributes())));
        }

        // Coefficient array for the objective function
        double[] c = new double[edges.size()]; // Use edges.size() for the size of c
        for (int i = 0; i < edges.size(); i++) {
            c[i] = Math.log(relations.get(i).size());
        }

        // Define the objective function: minimize log(Ne) * xe
        LinearObjectiveFunction f = new LinearObjectiveFunction(c, GoalType.MIN);

        // Constraints
        ArrayList<Constraint> constraints = new ArrayList<>();

        // Sum of all x_v for each vertex v is at least 1
        for (String vertex : uniqueVertices) {
            double[] coefficients = new double[edges.size()]; // Use edges.size() for the size of coefficients
            for (int i = 0; i < edges.size(); i++) {
                if (edges.get(i).contains(vertex)) {
                    coefficients[i] = 1.0;  // 1.0 indicates that the edge covers the vertex
                } else {
                    coefficients[i] = 0.0;
                }
            }
            constraints.add(new Constraint(coefficients, ConsType.GE, 1));
        }

        // Create the linear program
        LP lp = new LP(f, constraints);
        SolutionType solution_type = lp.resolve();

        // Check for optimal solution and print results
        if (solution_type == SolutionType.OPTIMUM) {
            Solution solution = lp.getSolution();
            for (Variable var : solution.getVariables()) {
                SscLogger.log("Variable name: " + var.getName() + " value: " + var.getValue());
            }
            SscLogger.log("Optimal Value: " + solution.getOptimumValue());
        } else {
            SscLogger.log("No optimal solution found: " + solution_type);
        }
    }

    public static void main(String[] args) throws Exception {
        // Example input sets
        Relation R1 = new Relation(new String[]{"A", "B", "C"});
        R1.addTuple(new Tuple(new String[]{"1", "2", "3"}));
        R1.addTuple(new Tuple(new String[]{"4", "5", "6"}));
        R1.addTuple(new Tuple(new String[]{"7", "8", "9"}));

        Relation R2 = new Relation(new String[]{"B", "C", "D"});
        R2.addTuple(new Tuple(new String[]{"2", "3", "4"}));
        R2.addTuple(new Tuple(new String[]{"5", "6", "7"}));
        R2.addTuple(new Tuple(new String[]{"8", "9", "10"}));
        R2.addTuple(new Tuple(new String[]{"11", "12", "13"}));
        R2.addTuple(new Tuple(new String[]{"14", "15", "16"}));


        ArrayList<Relation> relations = new ArrayList<>();
        relations.add(R1);
        relations.add(R2);

        FractionalEdgeCoverSolution(relations);
    }
}