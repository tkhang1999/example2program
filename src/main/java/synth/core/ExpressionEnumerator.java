package synth.core;

import java.util.Collections;
import java.util.LinkedList;

import synth.cfg.CFG;
import synth.cfg.NonTerminal;

public class ExpressionEnumerator extends Enumerator {

    public ExpressionEnumerator(CFG cfg) {
        super(cfg, new LinkedList<>() {{
            add(new ASTNode(new NonTerminal("E"), Collections.emptyList()));
        }});
    }
}
