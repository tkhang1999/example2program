package synth;

import synth.cfg.CFG;
import synth.cfg.NonTerminal;
import synth.cfg.Terminal;
import synth.core.ConstraintBasedSynthesizer;
import synth.core.DivideAndConquerSynthesizer;
import synth.core.Example;
import synth.core.ISynthesizer;
import synth.cfg.Production;
import synth.core.TopDownEnumSynthesizer;
import synth.util.FileUtils;
import synth.util.Parser;
import synth.util.SynthesisTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class Main {
    static {
        // must set before the Logger
        if (System.getProperty("java.util.logging.config.file") == null) {
            System.setProperty("java.util.logging.config.file", "logging.properties");
        }
    }

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final String CONSTRAINT_BASED = "constraint-based";
    private static final String DIVIDE_AND_CONQUER = "divide-conquer";

    public static void main(String[] args) throws InterruptedException {
        String examplesFilePath = args[0];
        List<String> lines = FileUtils.readLinesFromFile(examplesFilePath);
        // parse all examples
        List<Example> examples = Parser.parseAllExamples(lines);
        // read the CFG
        CFG cfg = buildCFG();
        // read the synthesizer
        ISynthesizer synthesizer = buildSynthesizer(args.length > 1 ? args[1] : null);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(new SynthesisTask(synthesizer, cfg, examples));
        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            LOGGER.info("Unable to synthesize a program within the time limit");
        } catch (Exception e) {
            LOGGER.severe("An error occurred during synthesis: " + e.getMessage());
        } finally {
            executor.shutdownNow();
            if (!executor.awaitTermination(100, TimeUnit.MICROSECONDS)) {
                System.exit(0);
            }
        }
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
            LOGGER.info("Using the constraint-based synthesizer");
            return new ConstraintBasedSynthesizer();
        } else if (DIVIDE_AND_CONQUER.equals(synthesizerType)) {
            LOGGER.info("Using the divide-and-conquer synthesizer");
            return new DivideAndConquerSynthesizer();
        } else {
            LOGGER.info("Using the top-down enumeration synthesizer");
            return new TopDownEnumSynthesizer();
        }
    }
}
