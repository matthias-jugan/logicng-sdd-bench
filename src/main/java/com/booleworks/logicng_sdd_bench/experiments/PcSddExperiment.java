package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.events.SimpleEvent;
import com.booleworks.logicng.knowledgecompilation.sdd.algorithms.SddSize;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompiler;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerConfig;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;
import com.booleworks.logicng_sdd_bench.trackers.CompilationTracker;
import com.booleworks.logicng_sdd_bench.trackers.SimpleCounter;
import com.booleworks.logicng_sdd_bench.trackers.TrackerGroup;

import java.util.List;
import java.util.function.Supplier;

public class PcSddExperiment implements Experiment<ProjectionProblem, CompilationTracker> {
    final Supplier<SddCompilerConfig.Builder> config;

    public PcSddExperiment(final Supplier<SddCompilerConfig.Builder> config) {
        this.config = config;
    }

    public PcSddExperiment() {
        this(Util.DEFAULT_COMPILER_CONFIG);
    }

    @Override
    public CompilationTracker execute(final ProjectionProblem input, final FormulaFactory f, final Logger logger,
                                      final Supplier<ComputationHandler> handler) {
        final CompilationTracker compTracker = new CompilationTracker(handler.get());
        final SimpleCounter applyTracker = new SimpleCounter(SimpleEvent.SDD_APPLY);
        final var trackerGroup = new TrackerGroup(List.of(compTracker, applyTracker), handler.get());
        compTracker.setFormulaVariableCount(input.quantifiedVariables().size() + input.projectedVariables().size());
        compTracker.setProjectedVariableCount(input.projectedVariables().size());
        compTracker.start();
        final var c = config.get().variables(input.projectedVariables()).build();
        final var compiledResult = SddCompiler.compile(input.formula().cnf(f), c, f, trackerGroup);
        if (compiledResult.isSuccess()) {
            compTracker.done();
            compTracker.setNodeSize(SddSize.size(compiledResult.getResult().getNode()));
            compTracker.setSddSize(compiledResult.getResult().getSdd().getSddNodeCount());
        }
        return compTracker;
    }
}
