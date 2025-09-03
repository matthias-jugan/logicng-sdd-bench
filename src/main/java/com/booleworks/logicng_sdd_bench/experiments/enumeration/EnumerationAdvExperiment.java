package com.booleworks.logicng_sdd_bench.experiments.enumeration;

import com.booleworks.logicng.datastructures.Model;
import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Variable;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.solvers.SatSolver;
import com.booleworks.logicng.solvers.functions.ModelEnumerationFunction;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.Experiment;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;
import com.booleworks.logicng_sdd_bench.experiments.results.ModelEnumerationResult;
import com.booleworks.logicng_sdd_bench.trackers.HandlerGroup;

import java.util.List;
import java.util.Set;
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
        final Set<Variable> additionalVars = input.quantifiedVariables();
        final var h = handler.get();
        final var countTracker = new ModelEnumerationResult(false, limit, numberVars, additionalVars.size());
        final var handlers = new HandlerGroup(List.of(countTracker, h));
        final Formula cnf = Util.encodeAsPureCnf(f, input.formula());
        final SatSolver solver = SatSolver.newSolver(f);
        solver.add(cnf);
        ModelEnumerationFunction.Builder meFunctionB = ModelEnumerationFunction.builder(input.projectedVariables());
        if (withAddVars) {
            meFunctionB = meFunctionB.additionalVariables(additionalVars);
        }
        countTracker.addCount(0);
        final ModelEnumerationFunction meFunction = meFunctionB.build();
        final LngResult<List<Model>> models = solver.execute(meFunction, handlers);
        if (models.isSuccess() || models.isPartial()) {
            countTracker.addCount(models.getPartialResult().size());
        }
        return countTracker;
    }
}
