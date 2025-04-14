package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng_sdd_bench.experiments.results.ExperimentResult;

import java.util.List;
import java.util.function.Supplier;

public abstract class Experiment<R extends ExperimentResult> {
    public abstract R execute(Formula input, FormulaFactory f, Supplier<ComputationHandler> handler);

    public abstract List<String> getLabels();
}
