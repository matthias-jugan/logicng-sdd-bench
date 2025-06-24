package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerTopDown;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddCompilationResult;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.experiments.results.TimingResult;
import com.booleworks.logicng_sdd_bench.trackers.CompilationTimeTracker;

import java.util.List;
import java.util.function.Supplier;

public class CompileSddExperiment extends Experiment<Formula, CompilationTimeTracker> {

    @Override
    public CompilationTimeTracker execute(final Formula input, final FormulaFactory f, final Logger logger,
                                          final Supplier<ComputationHandler> handler) {
        final CompilationTimeTracker tracker = new CompilationTimeTracker(handler.get());
        final LngResult<SddCompilationResult> result = SddCompilerTopDown.compile(input.cnf(f), f, tracker);
        if (result.isSuccess()) {
            tracker.done();
        } else {
            System.out.println("Timeout");
        }
        return tracker;
    }

    @Override
    public List<String> getLabels() {
        return TimingResult.getLabels();
    }
}
