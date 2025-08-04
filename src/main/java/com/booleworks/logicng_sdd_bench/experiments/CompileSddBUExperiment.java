package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.knowledgecompilation.sdd.algorithms.SddSize;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompiler;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerConfig;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.trackers.CompilationTracker;

import java.util.function.Supplier;

public class CompileSddBUExperiment implements Experiment<Formula, CompilationTracker> {

    @Override
    public CompilationTracker execute(final Formula input, final FormulaFactory f, final Logger logger,
                                      final Supplier<ComputationHandler> handler) {
        final var tracker = new CompilationTracker(handler.get());
        final Formula cnf = input.cnf(f);
        tracker.setFormulaVariableCount(input.variables(f).size());
        tracker.start();
        final var config = SddCompilerConfig.builder()
                .compiler(SddCompilerConfig.Compiler.BOTTOM_UP)
                .build();
        final var result = SddCompiler.compile(cnf, config, f, tracker);
        if (result.isSuccess()) {
            tracker.done();
            tracker.setNodeSize(SddSize.size(result.getResult().getNode()));
            tracker.setSddSize(result.getResult().getSdd().getSddNodeCount());
        }
        return tracker;
    }
}
