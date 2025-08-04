package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Variable;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompiler;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerConfig;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.Sdd;
import com.booleworks.logicng.knowledgecompilation.sdd.functions.SddModelCountFunction;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.results.ModelCountingResult;
import com.booleworks.logicng_sdd_bench.trackers.SegmentedTimeTracker;

import java.math.BigInteger;
import java.util.Set;
import java.util.function.Supplier;

public class CountModelsSddExperiment implements Experiment<Formula, ModelCountingResult> {

    @Override
    public ModelCountingResult execute(final Formula input, final FormulaFactory f, final Logger logger,
                                       final Supplier<ComputationHandler> handler) {
        final SegmentedTimeTracker tracker = new SegmentedTimeTracker();
        final var h = handler.get();
        final Formula cnf = Util.encodeAsPureCnf(f, input);
        final var config = SddCompilerConfig.builder().build();
        tracker.start();
        final var result = SddCompiler.compile(cnf, config, f, h);
        tracker.end("Compilation");
        if (!result.isSuccess()) {
            tracker.timeout();
            return new ModelCountingResult(null, tracker);
        }
        final Sdd sdd = result.getResult().getSdd();
        final Set<Variable> vars = input.variables(f);
        final LngResult<BigInteger> count =
                result.getResult().getNode().execute(new SddModelCountFunction(vars, sdd), h);
        if (!count.isSuccess()) {
            tracker.timeout();
            return new ModelCountingResult(null, tracker);
        }
        tracker.end("Counting");
        return new ModelCountingResult(count.getResult(), tracker);
    }
}
