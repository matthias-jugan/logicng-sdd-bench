package com.booleworks.logicng_sdd_bench.experiments.problems;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;

public record MinimizationProblem(Formula formula, long timeout, long opTimeout) {
    public static ProblemFunction<MinimizationProblem> minimizationWithTimeouts(final long timout,
                                                                                final long opTimeout) {
        return (final Formula formula, final FormulaFactory f) -> new MinimizationProblem(formula, timout, opTimeout);
    }
}
