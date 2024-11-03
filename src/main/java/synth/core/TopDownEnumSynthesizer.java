package synth.core;

import synth.cfg.CFG;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static synth.core.Utils.expand;
import static synth.core.Utils.isValid;

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

                if (isValid(program, examples)) {
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
}
