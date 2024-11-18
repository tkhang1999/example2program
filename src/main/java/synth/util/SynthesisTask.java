package synth.util;

import java.util.List;
import java.util.logging.Logger;

import synth.cfg.CFG;
import synth.core.ISynthesizer;
import synth.core.Program;
import synth.core.Example;

public class SynthesisTask implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(SynthesisTask.class.getName());

    private ISynthesizer synthesizer;
    private CFG cfg;
    private List<Example> examples;

    public SynthesisTask(ISynthesizer synthesizer, CFG cfg, List<Example> examples) {
        this.synthesizer = synthesizer;
        this.cfg = cfg;
        this.examples = examples;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        Program program = synthesizer.synthesize(cfg, examples);
        long endTime = System.currentTimeMillis();

        LOGGER.info("Time taken: " + (endTime - startTime) + "ms");
        System.out.println(program);
    }
}
