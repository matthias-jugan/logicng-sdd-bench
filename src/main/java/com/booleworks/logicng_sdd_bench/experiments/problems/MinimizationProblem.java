package com.booleworks.logicng_sdd_bench.experiments.problems;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddMinimizationConfig;
import com.booleworks.logicng_sdd_bench.Input;

public record MinimizationProblem(
        Formula formula, SddMinimizationConfig.Algorithm alg, long timeout, long opTimeout, long nodeLimit
) {
    public static ProblemFunction<MinimizationProblem> minimizationWithTimeouts(
            final SddMinimizationConfig.Algorithm alg, final long timout, final long opTimeout) {
        return (final Input formula, final FormulaFactory f) -> new MinimizationProblem(formula.asFormula(), alg,
                timout, opTimeout, -1);
    }

    public static ProblemFunction<MinimizationProblem> limitedMinimization(final SddMinimizationConfig.Algorithm alg,
                                                                           final long timout, final long opTimeout,
                                                                           final long nodeLimit) {
        return (final Input formula, final FormulaFactory f) -> new MinimizationProblem(formula.asFormula(), alg,
                timout,
                opTimeout, nodeLimit);
    }
}
