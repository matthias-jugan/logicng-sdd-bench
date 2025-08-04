package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.experiments.results.ExperimentResult;

import java.util.function.Supplier;

public interface Experiment<I, R extends ExperimentResult> {
    R execute(I input, FormulaFactory f, Logger logger, Supplier<ComputationHandler> handler);
}
