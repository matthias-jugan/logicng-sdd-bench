package com.booleworks.logicng_sdd_bench.setups;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentEntry;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentGroup;
import com.booleworks.logicng_sdd_bench.experiments.enumeration.EnumerationAdvExperiment;
import com.booleworks.logicng_sdd_bench.experiments.enumeration.EnumerationSddExperiment;

import java.util.List;
import java.util.function.Supplier;

public class ModelEnumerationSetups {

    public static void projectedModelEnumerationReal(
            final List<InputFile> inputs, final List<String> arguments, final Logger logger,
            final Supplier<ComputationHandler> handler) {
        final boolean withAddVars = arguments.contains("with-add-vars");
        final var results
                = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("SDD", new EnumerationSddExperiment(200_000, withAddVars),
                        ProjectedCompilationSetups.exportedPdfToProjectionProblem),
                new ExperimentEntry<>("Adv", new EnumerationAdvExperiment(200_000, withAddVars),
                        ProjectedCompilationSetups.exportedPdfToProjectionProblem)
        )).runExperiments(inputs, logger, handler);
    }
}
