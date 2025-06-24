package com.booleworks.logicng_sdd_bench.setups;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Settings;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Setups {
    public interface RunSetup {
        void run(final List<InputFile> inputs, final Settings settings, final Logger logger,
                 final Supplier<ComputationHandler> handler);
    }

    public static final Map<String, RunSetup> SETUPS = Map.of(
            "compile-sdb", CompilationSetups::compileSdb,
            "compile-sd", CompilationSetups::compileSd,
            "compile-s", CompilationSetups::compileS,
            "compilation-progress", CompilationSetups::compilationProgress,
            "model-counting", ModelCountingSetups::modelCounting,
            "projected-model-counting", ModelCountingSetups::pmc,
            "minimize", MinimizationSetups::minimize,
            "minimize2", MinimizationSetups::minimize2,
            "projected-compile", CompilationSetups::projectedCompile,
            "compile-cnf", CompilationSetups::compileCNF
    );

}
