package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.Sdd;
import com.booleworks.logicng.solvers.SatSolver;
import com.booleworks.logicng.solvers.functions.ModelCountingFunction;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;
import com.booleworks.logicng_sdd_bench.experiments.results.ModelCountingResult;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Supplier;

public class PmcAdvMeExperiment extends Experiment<ProjectionProblem, ModelCountingResult> {
    @Override
    public ModelCountingResult execute(final ProjectionProblem input, final FormulaFactory f, final Logger logger,
                                       final Supplier<ComputationHandler> handler) {
        final Sdd sf = Sdd.independent(f);
        final long start = System.currentTimeMillis();
        final Formula cnf = Util.encodeAsPureCnf(f, input.formula());
        final SatSolver solver = SatSolver.newSolver(f);
        solver.add(cnf);
        final LngResult<BigInteger> mc =
                ModelCountingFunction.builder(input.projectedVariables()).build().apply(solver, handler.get());

        final long end = System.currentTimeMillis();
        if (mc.isSuccess()) {
            return new ModelCountingResult(end - start, mc.getResult());
        } else {
            return ModelCountingResult.invalid();
        }
    }

    @Override
    public List<String> getLabels() {
        return List.of();
    }
}
