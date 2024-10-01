package synth.core;

import synth.cfg.CFG;
import synth.cfg.NonTerminal;
import synth.cfg.Production;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class TopDownEnumSynthesizer implements ISynthesizer {

    /**
     * Synthesize a program f(x, y, z) based on a context-free grammar and examples
     *
     * @param cfg      the context-free grammar
     * @param examples a list of examples
     * @return the program or null to indicate synthesis failure
     */
    @Override
    public Program synthesize(CFG cfg, List<Example> examples) {
        // Initialize the work list with the start symbol from CFG
        Queue<ASTNode> workList = new LinkedList<>();
        workList.add(new ASTNode(cfg.getStartSymbol(), Collections.emptyList()));

        while (!workList.isEmpty()) {
            ASTNode node = workList.remove();

            // If the node is complete, evaluate the program and check if it satisfies all examples
            if (node.isComplete()) {
                Program program = new Program(node);

                boolean isSatisfied = true;
                // Check if the program satisfies all examples
                for (Example example : examples) {
                    int value = Interpreter.evaluate(program, example.getInput());
                    if (value != example.getOutput()) {
                        isSatisfied = false;
                        break;
                    }
                }

                if (isSatisfied) {
                    return program;
                }
            } 
            // Otherwise, expand the node
            else {
                workList.addAll(expand(node, cfg));
            }
        }

        return null;
    }

    /**
     * Expand a node based on the context-free grammar
     *
     * @param root the node to be expanded
     * @param cfg  the context-free grammar
     * @return a list of expanded nodes
     */
    private List<ASTNode> expand(ASTNode root, CFG cfg) {
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
