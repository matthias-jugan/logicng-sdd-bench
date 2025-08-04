package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompiler;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerConfig;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddCompilationResult;
import com.booleworks.logicng.knowledgecompilation.sdd.functions.SddModelCountFunction;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;
import com.booleworks.logicng_sdd_bench.experiments.results.ModelCountingResult;
import com.booleworks.logicng_sdd_bench.trackers.SegmentedTimeTracker;

import java.math.BigInteger;
import java.util.function.Supplier;

public class PmcNaiveSddExperiment implements Experiment<ProjectionProblem, ModelCountingResult> {
    @Override
    public ModelCountingResult execute(final ProjectionProblem input, final FormulaFactory f, final Logger logger,
                                       final Supplier<ComputationHandler> handler) {
        final SegmentedTimeTracker tracker = new SegmentedTimeTracker();
        final ComputationHandler h = handler.get();
        final Formula cnf = Util.encodeAsPureCnf(f, input.formula());
        tracker.start();
        final var config = SddCompilerConfig.builder().build();
        final LngResult<SddCompilationResult> compiled = SddCompiler.compile(cnf, config, f, h);
        if (!compiled.isSuccess()) {
            tracker.timeout();
            return new ModelCountingResult(null, tracker);
        }
        tracker.end("Compilation");
        final var node = compiled.getResult().getNode();
        final var sdd = compiled.getResult().getSdd();
        final BigInteger mc = node.execute(new SddModelCountFunction(input.projectedVariables(), sdd));
        tracker.end("Counting");
        return new ModelCountingResult(mc, tracker);
    }
}
