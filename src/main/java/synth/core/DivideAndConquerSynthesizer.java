package synth.core;

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

        // Initialize the mapping from expressions to satisfied examples
        Map<ASTNode, Set<Example>> exprToExamples = new HashMap<>();
        Map<Example, Boolean> coveredByExpr = new HashMap<>();
        // Initialize the mapping from predicates to satisfied examples
        Map<ASTNode, Set<Example>> predToExamples = new HashMap<>();
        Map<Example, Boolean> coveredByPred = new HashMap<>();
        // Initialize the covered statuses for each example
        for (Example example : examples) {
            coveredByExpr.put(example, false);
            coveredByPred.put(example, false);
        }

        // Enumerate expressions and predicates until each examples is covered by at least one expression and one predicate
        while (coveredByExpr.containsValue(false)) {
            ASTNode node = nextDistinctNode(exprEnumerator, exprToExamples, examples);
            for (Example example : exprToExamples.get(node)) {
                coveredByExpr.put(example, true);
            }
        }
        while (coveredByPred.containsValue(false)) {
            ASTNode node = nextDistinctNode(predEnumerator, predToExamples, examples);
            for (Example example : predToExamples.get(node)) {
                coveredByPred.put(example, true);
            }
        }

        Program program = null;
        do {
            ASTNode node = unify(exprToExamples, predToExamples, new HashSet<>(examples));
            if (node != null) {
                program = new Program(node);
            } else {
                nextDistinctNode(exprEnumerator, exprToExamples, examples);
                nextDistinctNode(predEnumerator, predToExamples, examples);
            }
        } while (program == null);

        assert isValid(program, examples) : "Unexpected validation failure for the synthesized program: " + program;
        return program;
    }

    /**
     * Enumerate the next distinct node for the given enumerator
     * 
     * @param enumerator
     * @param nodeToExamples
     * @param examples
     * @return the next distinct node or throw an exception if no node can be enumerated
     */
    private ASTNode nextDistinctNode(Enumerator enumerator, Map<ASTNode, Set<Example>> nodeToExamples, List<Example> examples) {
        boolean enumerated = false;
        ASTNode node = null;

        while (!enumerated) {
            node = enumerator.enumerate();
            if (node == null) {
                throw new RuntimeException("Cannot enumerate any node");
            }
    
            // Check if the node can satisfy any example
            Set<Example> satisfiedExamples = new HashSet<>();
            for (Example example : examples) {
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

            if (!satisfiedExamples.isEmpty() && !nodeToExamples.values().contains(satisfiedExamples)) {
                nodeToExamples.put(node, satisfiedExamples);
                enumerated = true;
            }
        }

        return node;
    }

    /**
     * Unify the expressions and predicates to an AST node that can satisfy the given examples
     * 
     * @param exprToExamples
     * @param predToExamples
     * @param examples
     * @return the unified AST node if exists, otherwise null
     */
    private ASTNode unify(Map<ASTNode, Set<Example>> exprToExamples, Map<ASTNode, Set<Example>> predToExamples, Set<Example> examples) {
        // Iterate over all expressions and unify them with predicates if possible
        for (Map.Entry<ASTNode, Set<Example>> expEntry : exprToExamples.entrySet()) {
            ASTNode expr = expEntry.getKey();
            Set<Example> satisfiedExamples = expEntry.getValue();
            Set<Example> unsatisfiedExamples = new HashSet<>();
            for (Example example : examples) {
                if (!satisfiedExamples.contains(example)) {
                    unsatisfiedExamples.add(example);
                }
            }

            // Skip the expression if it cannot satisfy any example
            if (unsatisfiedExamples.equals(examples)) {
                continue;
            }

            // Return the expression if it satisfies all examples as no predicate is needed
            if (unsatisfiedExamples.isEmpty()) {
                return expr;
            }

            // Check if the expression can be unified with a predicate
            for (Map.Entry<ASTNode, Set<Example>> predEntry : predToExamples.entrySet()) {
                // The unified predicate should satisfy the same set of examples as the expression
                if (predEntry.getValue().equals(satisfiedExamples)) {
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

        return null;
    }
}
