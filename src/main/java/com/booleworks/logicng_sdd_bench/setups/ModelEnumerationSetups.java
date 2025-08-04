package com.booleworks.logicng_sdd_bench.setups;

import com.booleworks.logicng.datastructures.Assignment;
import com.booleworks.logicng.datastructures.Model;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.ValidationFunction;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentEntry;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentGroup;
import com.booleworks.logicng_sdd_bench.experiments.enumeration.EnumerationAdvExperiment;
import com.booleworks.logicng_sdd_bench.experiments.enumeration.EnumerationSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.results.ModelEnumerationResult;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ModelEnumerationSetups {

    public final static ValidationFunction<ModelEnumerationResult> compareModels = results -> {
        String baseName = null;
        Set<Assignment> baseModels = null;
        for (final var r : results) {
            final Set<Assignment> currentModels =
                    r.getSecond().models().stream().map(Model::toAssignment).collect(
                            Collectors.toSet());
            if (baseModels == null) {
                baseName = r.getFirst();
                baseModels = currentModels;
            } else {
                if (!baseModels.containsAll(currentModels) || !currentModels.containsAll(baseModels)) {
                    return String.format("The models differ between %s and %s", baseName, r.getFirst());
                }
            }
            r.getSecond().models().clear();
        }
        return null;
    };

    public final static ValidationFunction<ModelEnumerationResult> clearModels = results -> {
        for (final var r : results) {
            if (r.getSecond().models() != null) {
                r.getSecond().models().clear();
            }
        }
        return null;
    };

    public static void projectedModelEnumerationReal(
            final List<InputFile> inputs, final List<String> arguments, final Logger logger,
            final Supplier<ComputationHandler> handler) {
        final boolean withAddVars = arguments.contains("--with-add-vars");
        final var results
                = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("SDD 50_000", new EnumerationSddExperiment(10_000, withAddVars),
                        ProjectedCompilationSetups.exportedPdfToProjectionProblem),
                new ExperimentEntry<>("Adv 50_000", new EnumerationAdvExperiment(10_000, withAddVars),
                        ProjectedCompilationSetups.exportedPdfToProjectionProblem),
                new ExperimentEntry<>("SDD 100_000", new EnumerationSddExperiment(100_000, withAddVars),
                        ProjectedCompilationSetups.exportedPdfToProjectionProblem),
                new ExperimentEntry<>("Adv 100_000", new EnumerationAdvExperiment(100_000, withAddVars),
                        ProjectedCompilationSetups.exportedPdfToProjectionProblem),
                new ExperimentEntry<>("SDD 200_000", new EnumerationSddExperiment(500_000, withAddVars),
                        ProjectedCompilationSetups.exportedPdfToProjectionProblem),
                new ExperimentEntry<>("Adv 200_000", new EnumerationAdvExperiment(500_000, withAddVars),
                        ProjectedCompilationSetups.exportedPdfToProjectionProblem)
        )).runExperiments(inputs, logger, handler, clearModels);
    }
}
