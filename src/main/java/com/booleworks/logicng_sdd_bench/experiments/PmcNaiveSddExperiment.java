package com.booleworks.logicng_sdd_bench.experiments;

import static com.booleworks.logicng.knowledgecompilation.sdd.algorithms.Util.varsToIndicesOnlyKnown;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.handlers.NopHandler;
import com.booleworks.logicng.knowledgecompilation.sdd.algorithms.SddQuantification;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerTopDown;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.Sdd;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddCompilationResult;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddNode;
import com.booleworks.logicng.knowledgecompilation.sdd.functions.SddModelCountFunction;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;
import com.booleworks.logicng_sdd_bench.experiments.results.ModelCountingResult;
import com.booleworks.logicng_sdd_bench.experiments.results.TimingResult;
import com.booleworks.logicng_sdd_bench.trackers.SegmentedTimeTracker;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class PmcNaiveSddExperiment extends Experiment<ProjectionProblem, ModelCountingResult> {
    @Override
    public ModelCountingResult execute(final ProjectionProblem input, final FormulaFactory f, final Logger logger,
                                       final Supplier<ComputationHandler> handler) {
        final SegmentedTimeTracker tracker = new SegmentedTimeTracker();
        tracker.start();
        final Formula cnf = Util.encodeAsPureCnf(f, input.formula());
        final SddCompilationResult compiled = SddCompilerTopDown.compile(cnf, f, NopHandler.get()).getResult();
        final Sdd sdd = compiled.getSdd();
        tracker.end("Compilation");

        final Set<Integer> quantifiedVariableIdxs =
                varsToIndicesOnlyKnown(input.quantifiedVariables(), sdd, new HashSet<>());
        final LngResult<SddNode> quantified =
                SddQuantification.exists(quantifiedVariableIdxs, compiled.getNode(), sdd, handler.get());
        if (!quantified.isSuccess()) {
            tracker.timeout();
            return new ModelCountingResult(null, tracker);
        }
        tracker.end("Quantification");
        System.out.println("N: " + sdd.getSddNodeCount());

        final BigInteger mc = sdd.apply(new SddModelCountFunction(input.projectedVariables(), quantified.getResult()));
        tracker.end("Counting");
        return new ModelCountingResult(mc, tracker);
    }

    @Override
    public List<String> getLabels() {
        return TimingResult.getLabels();
    }
}
