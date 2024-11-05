package synth.core;

import java.util.Queue;

import synth.cfg.CFG;

public abstract class Enumerator {
    private CFG cfg;
    private Queue<ASTNode> workList;

    public Enumerator(CFG cfg, Queue<ASTNode> workList) {
        this.cfg = cfg;
        this.workList = workList;
    }

    public CFG getCFG() {
        return cfg;
    }

    public Queue<ASTNode> getWorkList() {
        return workList;
    }

    /**
     * Enumerate the next AST node that is complete
     * 
     * @return a complete AST node
     */
    public ASTNode enumerate() {
        while (!workList.isEmpty()) {
            ASTNode node = workList.remove();

            if (node.isComplete()) {
                return node;
            } else {
                workList.addAll(Utils.expand(node, cfg));
            }
        }

        return null;
    }
}
