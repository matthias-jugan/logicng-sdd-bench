package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Variable;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompiler;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerConfig;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.Sdd;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddCompilationResult;
import com.booleworks.logicng.knowledgecompilation.sdd.functions.SddModelCountFunction;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;
import com.booleworks.logicng_sdd_bench.experiments.results.ModelCountingResult;
import com.booleworks.logicng_sdd_bench.trackers.SegmentedTimeTracker;

import java.math.BigInteger;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PmcPcSddExperiment implements Experiment<ProjectionProblem, ModelCountingResult> {
    @Override
    public ModelCountingResult execute(final ProjectionProblem input, final FormulaFactory f, final Logger logger,
                                       final Supplier<ComputationHandler> handler) {
        final SegmentedTimeTracker tracker = new SegmentedTimeTracker();
        tracker.start();
        final Formula cnf = Util.encodeAsPureCnf(f, input.formula());
        final Set<Variable> projectedVariables = cnf.variables(f)
                .stream()
                .filter(v -> !input.quantifiedVariables().contains(v)).collect(Collectors.toSet());
        projectedVariables.addAll(input.projectedVariables());
        final var config = SddCompilerConfig.builder().variables(input.projectedVariables()).build();
        final var compiledResult = SddCompiler.compile(cnf, config, f, handler.get());
        tracker.end("Compilation");
        if (!compiledResult.isSuccess()) {
            tracker.timeout();
            return new ModelCountingResult(null, tracker);
        }
        final SddCompilationResult compiled = compiledResult.getResult();
        final Sdd sdd = compiled.getSdd();

        final BigInteger mc = compiled.getNode().execute(new SddModelCountFunction(input.projectedVariables(), sdd));
        tracker.end("Counting");
        return new ModelCountingResult(mc, tracker);
    }
}
