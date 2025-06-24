package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Variable;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerTopDown;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.Sdd;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddCompilationResult;
import com.booleworks.logicng.knowledgecompilation.sdd.functions.SddModelCountFunction;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.results.ModelCountingResult;
import com.booleworks.logicng_sdd_bench.experiments.results.TimingResult;
import com.booleworks.logicng_sdd_bench.trackers.SegmentedTimeTracker;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class CountModelsSddExperiment extends Experiment<Formula, ModelCountingResult> {

    @Override
    public ModelCountingResult execute(final Formula input, final FormulaFactory f, final Logger logger,
                                       final Supplier<ComputationHandler> handler) {
        final SegmentedTimeTracker tracker = new SegmentedTimeTracker();
        final Formula cnf = Util.encodeAsPureCnf(f, input);
        final LngResult<SddCompilationResult> result = SddCompilerTopDown.compile(cnf, f, handler.get());
        final Sdd sdd = result.getResult().getSdd();
        if (!result.isSuccess()) {
            tracker.timeout();
            return new ModelCountingResult(null, tracker);
        }
        tracker.start();
        final Set<Variable> vars = input.variables(f);
        final BigInteger count =
                sdd.apply(new SddModelCountFunction(vars, result.getResult().getNode()));
        tracker.end("Counting");
        return new ModelCountingResult(count, tracker);
    }

    @Override
    public List<String> getLabels() {
        return TimingResult.getLabels();
    }
}
