package synth.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import synth.cfg.CFG;
import synth.cfg.Terminal;

import static synth.core.Utils.isValid;
import static synth.core.Utils.isSatisfiable;

public class DivideAndConquerSynthesizer implements ISynthesizer {

    @Override
    public Program synthesize(CFG cfg, List<Example> examples) {
        // Initialize the expression and predicate enumerators
        Enumerator exprEnumerator = new ExpressionEnumerator(cfg);
        Enumerator predEnumerator = new PredicateEnumerator(cfg);

        // Enumerate expressions to cover all examples
        Map<ASTNode, Set<Example>> exprToExamples = new HashMap<>();
        Map<Example, Boolean> coveredExpr = new HashMap<>();
        // Enumerate predicates to cover all examples
        Map<ASTNode, Set<Example>> predToExamples = new HashMap<>();
        Map<Example, Boolean> coveredPred = new HashMap<>();

        for (Example example : examples) {
            coveredExpr.put(example, false);
            coveredPred.put(example, false);
        }

        while (coveredExpr.containsValue(false)) {
            enumerate(exprEnumerator, exprToExamples, coveredExpr);
        }
        while (coveredPred.containsValue(false)) {
            enumerate(predEnumerator, predToExamples, coveredPred);
        }

        System.out.println("Expressions: " + exprToExamples);
        System.out.println("Predicates: " + predToExamples);

        Program program = null;
        do {
            ASTNode node = unify(exprToExamples, predToExamples, new HashSet<>(examples));
            if (node != null) {
                program = new Program(node);
            } else {
                enumerate(exprEnumerator, exprToExamples, coveredExpr);
                enumerate(predEnumerator, predToExamples, coveredPred);
            }
        } while (program == null);

        System.out.println("Sanity check: " + isValid(program, examples));
        return program;
    }

    private ASTNode enumerate(Enumerator enumerator, Map<ASTNode, Set<Example>> nodeToExamples, Map<Example, Boolean> covered) {
        boolean enumerated = false;
        ASTNode node = null;

        while (!enumerated) {
            node = enumerator.enumerate();
            if (node == null) {
                throw new RuntimeException("Cannot enumerate any node");
            }

            System.out.println("Enumerated: " + node);
    
            // Check if the node is valid for any example
            Set<Example> satisfiedExamples = new HashSet<>();
            for (Example example : covered.keySet()) {
                // check instanceOf enumerator
                if (enumerator instanceof ExpressionEnumerator) {
                    if (isValid(new Program(node), List.of(example))) {
                        satisfiedExamples.add(example);
                    }
                } else if (enumerator instanceof PredicateEnumerator) {
                    if (isSatisfiable(node, List.of(example))) {
                        satisfiedExamples.add(example);
                    }
                } else {
                    throw new RuntimeException("Unknown enumerator: " + enumerator);
                }
            }

            System.out.println("Satisfied examples: " + satisfiedExamples);
            if (!satisfiedExamples.isEmpty() && !nodeToExamples.values().contains(satisfiedExamples)) {
                nodeToExamples.put(node, satisfiedExamples);
                for (Example example : satisfiedExamples) {
                    covered.put(example, true);
                }
                enumerated = true;
            }
        }

        return node;
    }

    private ASTNode unify(Map<ASTNode, Set<Example>> exprToExamples, Map<ASTNode, Set<Example>> predToExamples, Set<Example> examples) {
        System.out.println("Unifying expressions and predicates for examples: " + examples);
        for (Map.Entry<ASTNode, Set<Example>> expEntry : exprToExamples.entrySet()) {
            ASTNode expr = expEntry.getKey();
            Set<Example> satisfiedExamples = expEntry.getValue();
            Set<Example> unsatisfiedExamples = new HashSet<>();
            for (Example example : examples) {
                if (!satisfiedExamples.contains(example)) {
                    unsatisfiedExamples.add(example);
                }
            }
            if (unsatisfiedExamples.equals(examples)) {
                continue;
            }

            for (Map.Entry<ASTNode, Set<Example>> predEntry : predToExamples.entrySet()) {
                if (predEntry.getValue().equals(satisfiedExamples)) {
                    if (unsatisfiedExamples.isEmpty()) {
                        return expr;
                    } else {
                        System.out.println("Expression: " + expr);
                        System.out.println("Predicate: " + predEntry.getKey());
                        System.out.println("Satisfied examples: " + satisfiedExamples);
                        System.out.println("Uncovered examples: " + unsatisfiedExamples);
                        ASTNode child = unify(exprToExamples, predToExamples, unsatisfiedExamples);
                        if (child != null) {
                            ASTNode pred = predEntry.getKey();
                            return new ASTNode(new Terminal("Ite"), List.of(pred, expr, child));
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        return null;
    }
}
