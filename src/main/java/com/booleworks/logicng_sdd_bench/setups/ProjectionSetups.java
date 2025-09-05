package com.booleworks.logicng_sdd_bench.setups;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.ExElimSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentEntry;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentGroup;
import com.booleworks.logicng_sdd_bench.experiments.compilation.PcSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProblemFunction;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ProjectionSetups {
    public static ProblemFunction<ProjectionProblem> exportedPdfToProjectionProblem = (input, f) -> {
        var formula = input.asFormula();
        var allVariables = formula.variables(f);
        var elimVars = allVariables.stream()
                .filter(v -> v.getName().endsWith("_p") || v.getName().endsWith("_c") || v.getName().endsWith("_d"))
                .collect(Collectors.toSet());
        var projectionVars = allVariables.stream().filter(v -> !elimVars.contains(v)).collect(Collectors.toSet());
        return new ProjectionProblem(formula, elimVars, projectionVars);
    };

    public static void projectReal(final List<InputFile> inputs, final List<String> arguments,
                                   final Logger logger,
                                   final Supplier<ComputationHandler> handler) {
        final var results = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("Project", new ExElimSddExperiment(Util.DEFAULT_COMPILER_CONFIG),
                        exportedPdfToProjectionProblem)
        )).runExperiments(inputs, logger, handler);
    }

    public static void projectRandom(final List<InputFile> inputs, final List<String> arguments,
                                     final Logger logger,
                                     final Supplier<ComputationHandler> handler) {
        final var results = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("Compile + Eliminate (20%)", new ExElimSddExperiment(),
                        ProjectionProblem.quantifyRandom(0.2, 1)),
                new ExperimentEntry<>("Compile + Eliminate (50%)", new ExElimSddExperiment(),
                        ProjectionProblem.quantifyRandom(0.5, 1)),
                new ExperimentEntry<>("Compile + Eliminate (80%)", new ExElimSddExperiment(),
                        ProjectionProblem.quantifyRandom(0.8, 1))
        )).runExperiments(inputs, logger, handler);
    }

    public static void projectedCompileReal(final List<InputFile> inputs, final List<String> arguments,
                                            final Logger logger,
                                            final Supplier<ComputationHandler> handler) {
        final var results = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("Projected Compilation", new PcSddExperiment(Util.DEFAULT_COMPILER_CONFIG),
                        exportedPdfToProjectionProblem),
                new ExperimentEntry<>("Compile + Eliminate", new ExElimSddExperiment(),
                        exportedPdfToProjectionProblem)
        )).runExperiments(inputs, logger, handler);
    }

    public static void projectedCompileRandom(final List<InputFile> inputs, final List<String> arguments,
                                              final Logger logger,
                                              final Supplier<ComputationHandler> handler) {
        final var results = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("Projected Compilation (20%)", new PcSddExperiment(Util.DEFAULT_COMPILER_CONFIG),
                        ProjectionProblem.quantifyRandom(0.2, 1)),
                new ExperimentEntry<>("Compile + Eliminate (20%)", new ExElimSddExperiment(),
                        ProjectionProblem.quantifyRandom(0.2, 1)),
                new ExperimentEntry<>("Projected Compilation (50%)", new PcSddExperiment(Util.DEFAULT_COMPILER_CONFIG),
                        ProjectionProblem.quantifyRandom(0.5, 1)),
                new ExperimentEntry<>("Compile + Eliminate (50%)", new ExElimSddExperiment(),
                        ProjectionProblem.quantifyRandom(0.5, 1)),
                new ExperimentEntry<>("Projected Compilation (80%)", new PcSddExperiment(Util.DEFAULT_COMPILER_CONFIG),
                        ProjectionProblem.quantifyRandom(0.8, 1)),
                new ExperimentEntry<>("Compile + Eliminate (80%)", new ExElimSddExperiment(),
                        ProjectionProblem.quantifyRandom(0.8, 1))
        )).runExperiments(inputs, logger, handler);
    }
}
