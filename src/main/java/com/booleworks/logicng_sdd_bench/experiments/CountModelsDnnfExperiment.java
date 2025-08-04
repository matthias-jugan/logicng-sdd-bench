package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.knowledgecompilation.dnnf.DnnfCompiler;
import com.booleworks.logicng.knowledgecompilation.dnnf.datastructures.Dnnf;
import com.booleworks.logicng.knowledgecompilation.dnnf.functions.DnnfModelCountFunction;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.results.ModelCountingResult;
import com.booleworks.logicng_sdd_bench.trackers.SegmentedTimeTracker;

import java.math.BigInteger;
import java.util.function.Supplier;

public class CountModelsDnnfExperiment implements Experiment<Formula, ModelCountingResult> {
    @Override
    public ModelCountingResult execute(final Formula input, final FormulaFactory f, final Logger logger,
                                       final Supplier<ComputationHandler> handler) {
        final SegmentedTimeTracker tracker = new SegmentedTimeTracker();
        final Formula cnf = Util.encodeAsPureCnf(f, input);
        tracker.start();
        final LngResult<Dnnf> dnnf = DnnfCompiler.compile(f, cnf, handler.get());
        tracker.end("Compilation");
        if (!dnnf.isSuccess()) {
            tracker.timeout();
            return new ModelCountingResult(null, tracker);
        }
        final BigInteger count = dnnf.getResult().execute(new DnnfModelCountFunction(f));
        tracker.end("Counting");
        return new ModelCountingResult(count, tracker);
    }
}
