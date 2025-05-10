package com.booleworks.logicng_sdd_bench.experiments.problems;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;

public interface ProblemFunction<I> {
    public static Formula id(final Formula formula, final FormulaFactory f) {
        return formula;
    }

    I generate(Formula formula, FormulaFactory f);
}
