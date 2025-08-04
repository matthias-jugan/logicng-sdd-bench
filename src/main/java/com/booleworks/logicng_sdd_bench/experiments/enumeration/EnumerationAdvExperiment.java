package com.booleworks.logicng_sdd_bench.experiments.enumeration;

import com.booleworks.logicng.datastructures.Model;
import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.handlers.NumberOfModelsHandler;
import com.booleworks.logicng.solvers.SatSolver;
import com.booleworks.logicng.solvers.functions.ModelEnumerationFunction;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.Experiment;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;
import com.booleworks.logicng_sdd_bench.experiments.results.ModelEnumerationResult;
import com.booleworks.logicng_sdd_bench.trackers.HandlerGroup;
import com.booleworks.logicng_sdd_bench.trackers.SegmentedTimeTracker;

import java.util.List;
import java.util.function.Supplier;

public class EnumerationAdvExperiment implements Experiment<ProjectionProblem, ModelEnumerationResult> {
    final int limit;
    final boolean withAddVars;

    public EnumerationAdvExperiment(final int limit, final boolean withAddVars) {
        this.limit = limit;
        this.withAddVars = withAddVars;
    }

    @Override
    public ModelEnumerationResult execute(final ProjectionProblem input, final FormulaFactory f, final Logger logger,
                                          final Supplier<ComputationHandler> handler) {
        final int numberVars = input.projectedVariables().size();
        final int additionalVars = 0;
        final SegmentedTimeTracker tracker = new SegmentedTimeTracker();
        final Formula cnf = Util.encodeAsPureCnf(f, input.formula());
        tracker.start();
        final SatSolver solver = SatSolver.newSolver(f);
        solver.add(cnf);
        final ComputationHandler meHandler;
        if (limit > 0) {
            final var modelLimitHandler = new NumberOfModelsHandler(limit);
            meHandler = new HandlerGroup(List.of(handler.get(), modelLimitHandler));
        } else {
            meHandler = handler.get();
        }
        ModelEnumerationFunction.Builder meFunctionB = ModelEnumerationFunction
                .builder(input.projectedVariables());
        if (withAddVars) {
            meFunctionB = meFunctionB.additionalVariables(input.quantifiedVariables());
        }
        final ModelEnumerationFunction meFunction = meFunctionB.build();
        final LngResult<List<Model>> models = solver.execute(meFunction, meHandler);

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
