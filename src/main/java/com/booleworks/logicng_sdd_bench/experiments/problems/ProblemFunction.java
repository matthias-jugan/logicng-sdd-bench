package com.booleworks.logicng_sdd_bench.experiments.problems;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng_sdd_bench.Input;

public interface ProblemFunction<I> {
    static Formula id(final Input input, final FormulaFactory f) {
        return input.asFormula();
    }

    I generate(Input input, FormulaFactory f);
}
