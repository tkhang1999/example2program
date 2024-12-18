package synth.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import synth.cfg.CFG;
import synth.cfg.NonTerminal;
import synth.cfg.Production;

/**
 * Utility functions for the synthesizer
 */
public class Utils {

    /**
     * Check if a program satisfies all the examples
     * 
     * @param program
     * @param examples
     * @return true if the program satisfies all examples, false otherwise
     */
    public static boolean isValid(Program program, List<Example> examples) {
        // Check if the program satisfies all examples
        for (Example example : examples) {
            int value = Interpreter.evaluate(program, example.getInput());
            if (value != example.getOutput()) {
                
                return false;
            }
        }

        return true;
    }

    /**
     * Check if a predicate satisfies all the examples
     * 
     * @param node
     * @param examples
     * @return true if the predicate satisfies all examples, false otherwise
     */
    public static boolean isSatisfiable(ASTNode node, List<Example> examples) {
        for (Example example : examples) {
            Interpreter interpreter = new Interpreter(example.getInput());
            if  (!interpreter.evalPred(node)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Expand a node based on the context-free grammar
     *
     * @param root the node to be expanded
     * @param cfg  the context-free grammar
     * @return a list of expanded nodes
     */
    public static List<ASTNode> expand(ASTNode root, CFG cfg) {
        List<ASTNode> expandedNodes = new ArrayList<>();

        for (int i = 0; i < root.getChildren().size(); i++) {
            ASTNode child = root.getChild(i);
            if (!child.isComplete()) {
                for (ASTNode expandedChild : expand(child, cfg)) {
                    // Create a copy of the children list
                    List<ASTNode> children = new ArrayList<>(root.getChildren());
                    // Replace the incomplete child with the expanded child
                    children.set(i, expandedChild);
                    // Create a new node with the updated children list
                    expandedNodes.add(new ASTNode(root.getSymbol(), children));
                }
                // Expand only the first incomplete child
                return expandedNodes;
            }
        }
        
        // If the root does not have any child or any incomplete child and its symbol is non-terminal,
        // expand it based on production rules
        if (root.getSymbol().isNonTerminal()) {
            for (Production production : cfg.getProductions((NonTerminal) root.getSymbol())) {
                // Create a new node with the operator and the argument symbols from the production
                ASTNode node = new ASTNode(
                    production.getOperator(),
                    production.getArgumentSymbols().stream().map(s -> new ASTNode(s, Collections.emptyList())).collect(Collectors.toList())
                );
                expandedNodes.add(node);
            }
        }

        return expandedNodes;
    }
}
