package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng_sdd_bench.Input;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProblemFunction;
import com.booleworks.logicng_sdd_bench.experiments.results.ExperimentResult;

import java.util.function.Supplier;

public record ExperimentEntry<I, E extends ExperimentResult>(
        String name, Experiment<I, E> experiment, ProblemFunction<I> problemFunction
) {

    public E run(Input input, FormulaFactory f, Logger logger, Supplier<ComputationHandler> handler) {
        final var problem = problemFunction().generate(input, f);
        if (problem == null) {
            return null;
        } else {
            return experiment().execute(problem, f, logger, handler);
        }
    }
}
