package synth;

import synth.cfg.CFG;
import synth.cfg.NonTerminal;
import synth.cfg.Terminal;
import synth.core.ConstraintBasedSynthesizer;
import synth.core.DivideAndConquerSynthesizer;
import synth.core.Example;
import synth.core.ISynthesizer;
import synth.cfg.Production;
import synth.core.Program;
import synth.core.TopDownEnumSynthesizer;
import synth.core.Utils;
import synth.util.FileUtils;
import synth.util.Parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Main {
    private static final String CONSTRAINT_BASED = "constraint-based";
    private static final String DIVIDE_AND_CONQUER = "divide-conquer";
    private static final Logger LOGGER = Utils.getLogger(Main.class.getName());

    public static void main(String[] args) {
        String examplesFilePath = args[0];
        List<String> lines = FileUtils.readLinesFromFile(examplesFilePath);
        // parse all examples
        List<Example> examples = Parser.parseAllExamples(lines);
        // read the CFG
        CFG cfg = buildCFG();
        // read the synthesizer
        ISynthesizer synthesizer = buildSynthesizer(args.length > 1 ? args[1] : null);

        long startTime = System.currentTimeMillis();
        Program program = synthesizer.synthesize(cfg, examples);
        long endTime = System.currentTimeMillis();

        LOGGER.info("Time taken: " + (endTime - startTime) + "ms");
        System.out.println(program);
    }

    /**
     * Build the following context-free grammar (CFG):
     * E ::= Ite(B, E, E) | Add(E, E) | Multiply(E, E) | x | y | z | 1 | 2 | 3
     * B ::= Lt(E, E) | Eq(E, E) | And(B, B) | Or(B, B) | Not(B)
     * where x, y, z are variables. 1, 2, 3 are constants. Lt means "less than". Eq means "equals"
     *
     * @return the CFG
     */
    private static CFG buildCFG() {
        NonTerminal startSymbol = new NonTerminal("E");
        Map<NonTerminal, List<Production>> symbolToProductions = new HashMap<>();
        {
            NonTerminal retSymbol = new NonTerminal("E");
            List<Production> prods = new ArrayList<>();
            prods.add(new Production(new NonTerminal("E"), new Terminal("Ite"), List.of(new NonTerminal("B"), new NonTerminal("E"), new NonTerminal("E"))));
            prods.add(new Production(new NonTerminal("E"), new Terminal("Add"), List.of(new NonTerminal("E"), new NonTerminal("E"))));
            prods.add(new Production(new NonTerminal("E"), new Terminal("Multiply"), List.of(new NonTerminal("E"), new NonTerminal("E"))));
            prods.add(new Production(new NonTerminal("E"), new Terminal("x"), Collections.emptyList()));
            prods.add(new Production(new NonTerminal("E"), new Terminal("y"), Collections.emptyList()));
            prods.add(new Production(new NonTerminal("E"), new Terminal("z"), Collections.emptyList()));
            prods.add(new Production(new NonTerminal("E"), new Terminal("1"), Collections.emptyList()));
            prods.add(new Production(new NonTerminal("E"), new Terminal("2"), Collections.emptyList()));
            prods.add(new Production(new NonTerminal("E"), new Terminal("3"), Collections.emptyList()));
            symbolToProductions.put(retSymbol, prods);
        }
        {
            NonTerminal retSymbol = new NonTerminal("B");
            List<Production> prods = new ArrayList<>();
            prods.add(new Production(new NonTerminal("B"), new Terminal("Lt"), List.of(new NonTerminal("E"), new NonTerminal("E"))));
            prods.add(new Production(new NonTerminal("B"), new Terminal("Eq"), List.of(new NonTerminal("E"), new NonTerminal("E"))));
            prods.add(new Production(new NonTerminal("B"), new Terminal("And"), List.of(new NonTerminal("B"), new NonTerminal("B"))));
            prods.add(new Production(new NonTerminal("B"), new Terminal("Or"), List.of(new NonTerminal("B"), new NonTerminal("B"))));
            prods.add(new Production(new NonTerminal("B"), new Terminal("Not"), List.of(new NonTerminal("B"))));
            symbolToProductions.put(retSymbol, prods);
        }
        return new CFG(startSymbol, symbolToProductions);
    }

    /**
     * Build a synthesizer based on the given type
     * 
     * @param synthesizerType
     * @return the synthesizer
     */
    private static ISynthesizer buildSynthesizer(String synthesizerType) {
        if (CONSTRAINT_BASED.equals(synthesizerType)) {
            return new ConstraintBasedSynthesizer();
        } else if (DIVIDE_AND_CONQUER.equals(synthesizerType)) {
            return new DivideAndConquerSynthesizer();
        } else {
            return new TopDownEnumSynthesizer();
        }
    }
}
