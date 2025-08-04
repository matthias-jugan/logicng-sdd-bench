package com.booleworks.logicng_sdd_bench.experiments.enumeration;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.NumberOfModelsHandler;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompiler;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerConfig;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.Sdd;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddNode;
import com.booleworks.logicng.knowledgecompilation.sdd.functions.SddModelEnumerationFunction;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.Experiment;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;
import com.booleworks.logicng_sdd_bench.experiments.results.ModelEnumerationResult;
import com.booleworks.logicng_sdd_bench.trackers.HandlerGroup;
import com.booleworks.logicng_sdd_bench.trackers.SegmentedTimeTracker;

import java.util.List;
import java.util.function.Supplier;

public class EnumerationSddExperiment implements Experiment<ProjectionProblem, ModelEnumerationResult> {
    final Supplier<SddCompilerConfig.Builder> config;
    final int limit;
    boolean withAddVars = false;

    public EnumerationSddExperiment(final int limit, final Supplier<SddCompilerConfig.Builder> config) {
        this.config = config;
        this.limit = limit;
    }

    public EnumerationSddExperiment(final int limit) {
        this(limit, Util.DEFAULT_COMPILER_CONFIG);
    }

    public EnumerationSddExperiment(final int limit, final boolean withAddVars) {
        this(limit, Util.DEFAULT_COMPILER_CONFIG);
        this.withAddVars = withAddVars;
    }

    @Override
    public ModelEnumerationResult execute(final ProjectionProblem input, final FormulaFactory f, final Logger logger,
                                          final Supplier<ComputationHandler> handler) {
        final int numberVars = input.projectedVariables().size();
        final int additionalVars = 0;
        final SegmentedTimeTracker tracker = new SegmentedTimeTracker();
        final ComputationHandler h = handler.get();
        final Formula cnf = Util.encodeAsPureCnf(f, input.formula());
        tracker.start();
        final var c = config.get().build();
        final var compiled = SddCompiler.compile(cnf, c, f, h);
        if (!compiled.isSuccess()) {
            tracker.timeout();
            return new ModelEnumerationResult(limit, -1, null, tracker, numberVars, additionalVars);
        }
        final Sdd sdd = compiled.getResult().getSdd();
        final SddNode node = compiled.getResult().getNode();
        tracker.end("Compilation");

        final ComputationHandler meHandler;
        if (limit > 0) {
            final var modelLimitHandler = new NumberOfModelsHandler(limit);
            meHandler = new HandlerGroup(List.of(h, modelLimitHandler));
        } else {
            meHandler = h;
        }
        var meFuncB = SddModelEnumerationFunction.builder(input.projectedVariables(), sdd);
        if (withAddVars) {
            meFuncB = meFuncB.additionalVariables(input.quantifiedVariables());
        }
        final var meFunc = meFuncB.build();

        final var models = node.execute(meFunc, meHandler);
        if (models.isSuccess() || models.isPartial()) {
            tracker.end("Enumeration");
        } else {
            tracker.timeout();
            return new ModelEnumerationResult(limit, -1, null, tracker, numberVars, additionalVars);
        }
        return new ModelEnumerationResult(limit, models.getPartialResult().size(),
                null, tracker, numberVars, additionalVars);
    }
}
