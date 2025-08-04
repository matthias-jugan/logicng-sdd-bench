package com.booleworks.logicng_sdd_bench.setups;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerConfig;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.vtree.DecisionVTreeGenerator;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.experiments.ExElimSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentEntry;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentGroup;
import com.booleworks.logicng_sdd_bench.experiments.PcSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProblemFunction;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ProjectedCompilationSetups {
    public static ProblemFunction<ProjectionProblem> exportedPdfToProjectionProblem = (input, f) -> {
        var formula = input.asFormula();
        var allVariables = formula.variables(f);
        var elimVars = allVariables.stream()
                .filter(v -> v.getName().endsWith("_p") || v.getName().endsWith("_c") || v.getName().endsWith("_d"))
                .collect(Collectors.toSet());
        var projectionVars = allVariables.stream().filter(v -> !elimVars.contains(v)).collect(Collectors.toSet());
        return new ProjectionProblem(formula, elimVars, projectionVars);
    };


    public static void projectedCompileReal(final List<InputFile> inputs, final List<String> arguments,
                                            final Logger logger,
                                            final Supplier<ComputationHandler> handler) {
        final Supplier<SddCompilerConfig.Builder> configSup = () -> SddCompilerConfig.builder()
                .prioritizationStrategy(DecisionVTreeGenerator.PrioritizationStrategy.NONE);
        final var results = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("Projected Compilation", new PcSddExperiment(configSup),
                        exportedPdfToProjectionProblem),
                new ExperimentEntry<>("Compile + Eliminate", new ExElimSddExperiment(configSup),
                        exportedPdfToProjectionProblem)
        )).runExperiments(inputs, logger, handler);
    }

    public static void projectedCompileRandom(final List<InputFile> inputs, final List<String> arguments,
                                              final Logger logger,
                                              final Supplier<ComputationHandler> handler) {
        final Supplier<SddCompilerConfig.Builder> configSup = () -> SddCompilerConfig.builder()
                .prioritizationStrategy(DecisionVTreeGenerator.PrioritizationStrategy.NONE);
        final var results = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("Projected Compilation (20%)", new PcSddExperiment(configSup),
                        ProjectionProblem.quantifyRandom(0.2, 1)),
                new ExperimentEntry<>("Compile + Eliminate (20%)", new ExElimSddExperiment(),
                        ProjectionProblem.quantifyRandom(0.2, 1)),
                new ExperimentEntry<>("Projected Compilation (50%)", new PcSddExperiment(configSup),
                        ProjectionProblem.quantifyRandom(0.5, 1)),
                new ExperimentEntry<>("Compile + Eliminate (50%)", new ExElimSddExperiment(),
                        ProjectionProblem.quantifyRandom(0.5, 1)),
                new ExperimentEntry<>("Projected Compilation (80%)", new PcSddExperiment(configSup),
                        ProjectionProblem.quantifyRandom(0.8, 1)),
                new ExperimentEntry<>("Compile + Eliminate (80%)", new ExElimSddExperiment(),
                        ProjectionProblem.quantifyRandom(0.8, 1))
        )).runExperiments(inputs, logger, handler);
    }

    public static void priorityStrategiesReal(final List<InputFile> inputs, final List<String> arguments,
                                              final Logger logger, final Supplier<ComputationHandler> handler) {
        final Supplier<SddCompilerConfig.Builder> stratNone = () -> SddCompilerConfig
                .builder()
                .prioritizationStrategy(DecisionVTreeGenerator.PrioritizationStrategy.NONE);
        final Supplier<SddCompilerConfig.Builder> stratUp = () -> SddCompilerConfig
                .builder()
                .prioritizationStrategy(DecisionVTreeGenerator.PrioritizationStrategy.VAR_UP);
        final Supplier<SddCompilerConfig.Builder> stratDown = () -> SddCompilerConfig
                .builder()
                .prioritizationStrategy(DecisionVTreeGenerator.PrioritizationStrategy.VAR_DOWN);
        final var results = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("None", new PcSddExperiment(stratNone), exportedPdfToProjectionProblem),
                new ExperimentEntry<>("Var Up", new PcSddExperiment(stratUp), exportedPdfToProjectionProblem),
                new ExperimentEntry<>("Var Down", new PcSddExperiment(stratDown), exportedPdfToProjectionProblem)
        )).runExperiments(inputs, logger, handler);
    }

    public static void priorityStrategiesRandom(final List<InputFile> inputs, final List<String> arguments,
                                                final Logger logger,
                                                final Supplier<ComputationHandler> handler) {
        final Supplier<SddCompilerConfig.Builder> stratNone = () -> SddCompilerConfig
                .builder()
                .prioritizationStrategy(DecisionVTreeGenerator.PrioritizationStrategy.NONE);
        final Supplier<SddCompilerConfig.Builder> stratUp = () -> SddCompilerConfig
                .builder()
                .prioritizationStrategy(DecisionVTreeGenerator.PrioritizationStrategy.VAR_UP);
        final Supplier<SddCompilerConfig.Builder> stratDown = () -> SddCompilerConfig
                .builder()
                .prioritizationStrategy(DecisionVTreeGenerator.PrioritizationStrategy.VAR_DOWN);
        final var results = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("None (20%)", new PcSddExperiment(stratNone),
                        ProjectionProblem.quantifyRandom(0.2, 1)),
                new ExperimentEntry<>("Var Up (20%)", new PcSddExperiment(stratUp),
                        ProjectionProblem.quantifyRandom(0.2, 1)),
                new ExperimentEntry<>("Var Down (20%)", new PcSddExperiment(stratDown),
                        ProjectionProblem.quantifyRandom(0.2, 1)),
                new ExperimentEntry<>("None (50%)", new PcSddExperiment(stratNone),
                        ProjectionProblem.quantifyRandom(0.5, 2)),
                new ExperimentEntry<>("Var Up (50%)", new PcSddExperiment(stratUp),
                        ProjectionProblem.quantifyRandom(0.5, 2)),
                new ExperimentEntry<>("Var Down (50%)", new PcSddExperiment(stratDown),
                        ProjectionProblem.quantifyRandom(0.5, 2)),
                new ExperimentEntry<>("None (80%)", new PcSddExperiment(stratNone),
                        ProjectionProblem.quantifyRandom(0.8, 3)),
                new ExperimentEntry<>("Var Up (80%)", new PcSddExperiment(stratUp),
                        ProjectionProblem.quantifyRandom(0.8, 3)),
                new ExperimentEntry<>("Var Down (80%)", new PcSddExperiment(stratDown),
                        ProjectionProblem.quantifyRandom(0.8, 3))
        )).runExperiments(inputs, logger, handler);
    }

}
