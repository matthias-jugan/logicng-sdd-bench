package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.handlers.events.SimpleEvent;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerTopDown;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddCompilationResult;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;
import com.booleworks.logicng_sdd_bench.experiments.results.TimingResult;
import com.booleworks.logicng_sdd_bench.trackers.CompilationTimeTracker;
import com.booleworks.logicng_sdd_bench.trackers.SimpleCounter;
import com.booleworks.logicng_sdd_bench.trackers.TrackerGroup;

import java.util.List;
import java.util.function.Supplier;

public class PcSddExperiment extends Experiment<ProjectionProblem, CompilationTimeTracker> {
    @Override
    public CompilationTimeTracker execute(final ProjectionProblem input, final FormulaFactory f, final Logger logger,
                                          final Supplier<ComputationHandler> handler) {
        logger.event("Started PcSddExperiment");
        final CompilationTimeTracker compTracker = new CompilationTimeTracker(handler.get());
        final SimpleCounter applyTracker = new SimpleCounter(SimpleEvent.SDD_APPLY);
        final var trackerGroup = new TrackerGroup(List.of(compTracker, applyTracker), handler.get());
        compTracker.start();
        final LngResult<SddCompilationResult> compiledResult =
                SddCompilerTopDown.compileProjected(input.formula(), input.projectedVariables(), f, trackerGroup);
        if (compiledResult.isSuccess()) {
            logger.event("Completed PcSddExperiment");
            compTracker.done();
        } else {
            logger.event("Aborted PcSddExperiment");
        }
        System.out.println(applyTracker.getCounter());
        return compTracker;
    }

    @Override
    public List<String> getLabels() {
        return TimingResult.getLabels();
    }
}
