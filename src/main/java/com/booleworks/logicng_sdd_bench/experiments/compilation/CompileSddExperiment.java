package com.booleworks.logicng_sdd_bench.experiments.compilation;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.knowledgecompilation.sdd.algorithms.SddSize;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompiler;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerConfig;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.Experiment;
import com.booleworks.logicng_sdd_bench.trackers.CompilationTracker;

import java.util.function.Supplier;

public class CompileSddExperiment implements Experiment<Formula, CompilationTracker> {
    final Supplier<SddCompilerConfig.Builder> config;

    public CompileSddExperiment(final Supplier<SddCompilerConfig.Builder> config) {
        this.config = config;
    }

    public CompileSddExperiment() {
        this(Util.DEFAULT_COMPILER_CONFIG);
    }

    @Override
    public CompilationTracker execute(final Formula input, final FormulaFactory f, final Logger logger,
                                      final Supplier<ComputationHandler> handler) {
        final CompilationTracker tracker = new CompilationTracker(handler.get());
        tracker.setFormulaVariableCount(input.variables(f).size());
        final var c = config.get().build();
        final var result = SddCompiler.compile(input.cnf(f), c, f, tracker);
        if (result.isSuccess()) {
            tracker.done();
            tracker.setNodeSize(SddSize.size(result.getResult().getNode()));
            tracker.setSddSize(result.getResult().getSdd().getSddNodeCount());
        }
        return tracker;
    }
}
