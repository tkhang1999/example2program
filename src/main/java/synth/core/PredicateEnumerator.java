package synth.core;

import java.util.Collections;
import java.util.LinkedList;

import synth.cfg.CFG;
import synth.cfg.NonTerminal;

public class PredicateEnumerator extends Enumerator {

    public PredicateEnumerator(CFG cfg) {
        super(cfg, new LinkedList<>() {{
            add(new ASTNode(new NonTerminal("B"), Collections.emptyList()));
        }});
    }
}
