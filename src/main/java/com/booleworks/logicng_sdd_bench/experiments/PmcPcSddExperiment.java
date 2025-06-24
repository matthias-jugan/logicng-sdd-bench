package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerTopDown;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.Sdd;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddCompilationResult;
import com.booleworks.logicng.knowledgecompilation.sdd.functions.SddModelCountFunction;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;
import com.booleworks.logicng_sdd_bench.experiments.results.ModelCountingResult;
import com.booleworks.logicng_sdd_bench.experiments.results.TimingResult;
import com.booleworks.logicng_sdd_bench.trackers.SegmentedTimeTracker;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Supplier;

public class PmcPcSddExperiment extends Experiment<ProjectionProblem, ModelCountingResult> {
    @Override
    public ModelCountingResult execute(final ProjectionProblem input, final FormulaFactory f, final Logger logger,
                                       final Supplier<ComputationHandler> handler) {
        final SegmentedTimeTracker tracker = new SegmentedTimeTracker();
        tracker.start();
        final Formula cnf = Util.encodeAsPureCnf(f, input.formula());
        final LngResult<SddCompilationResult> compiledResult =
                SddCompilerTopDown.compileProjected(cnf, input.projectedVariables(), f, handler.get());
        if (!compiledResult.isSuccess()) {
            return new ModelCountingResult(null, tracker);
        }
        final SddCompilationResult compiled = compiledResult.getResult();
        final Sdd sdd = compiled.getSdd();
        tracker.end("Compilation");
        System.out.println("P: " + sdd.getSddNodeCount());

        final BigInteger mc = sdd.apply(new SddModelCountFunction(input.projectedVariables(), compiled.getNode()));
        tracker.end("MC");
        return new ModelCountingResult(mc, tracker);
    }

    @Override
    public List<String> getLabels() {
        return TimingResult.getLabels();
    }
}
