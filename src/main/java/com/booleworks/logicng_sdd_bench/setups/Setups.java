package com.booleworks.logicng_sdd_bench.setups;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.util.Pair;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;

import java.util.List;
import java.util.function.Supplier;

public class Setups {
    public interface RunSetup {
        void run(final List<InputFile> inputs, final List<String> arguments, final Logger logger,
                 final Supplier<ComputationHandler> handler) throws Exception;
    }

    public static final List<Pair<String, RunSetup>> SETUPS = List.of(
            new Pair<>("preprocessing", CompilationSetups::compilePreprocessing),
            new Pair<>("compile-all", CompilationSetups::compileAll),
            new Pair<>("measure-heap", CompilationSetups::measureHeap),
            new Pair<>("project-real", ProjectionSetups::projectReal),
            new Pair<>("project-random", ProjectionSetups::projectRandom),
            new Pair<>("projected-model-enumeration-real", ModelEnumerationSetups::projectedModelEnumerationReal),
            new Pair<>("model-counting", ModelCountingSetups::modelCounting),
            new Pair<>("projected-model-counting", ModelCountingSetups::projectedModelCounting),
            new Pair<>("minimize", MinimizationSetups::minimize),
            new Pair<>("projected-compile-real", ProjectionSetups::projectedCompileReal),
            new Pair<>("projected-compile-random", ProjectionSetups::projectedCompileRandom)
    );

}
