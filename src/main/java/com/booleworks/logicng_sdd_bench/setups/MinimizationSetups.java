package com.booleworks.logicng_sdd_bench.setups;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.knowledgecompilation.sdd.algorithms.SddMinimizationStrategy;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerConfig;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentEntry;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentGroup;
import com.booleworks.logicng_sdd_bench.experiments.MinPMCExperiment;
import com.booleworks.logicng_sdd_bench.experiments.MinPMEExperiment;
import com.booleworks.logicng_sdd_bench.experiments.MinimizeSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.PmcNaiveSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.PmcPcSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.compilation.CompileSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.compilation.RecompileExperiment;
import com.booleworks.logicng_sdd_bench.experiments.enumeration.EnumerationSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.problems.MinimizationProblem;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProblemFunction;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

public class MinimizationSetups {
    public static void minimize(
            final List<InputFile> inputs, final List<String> arguments, final Logger logger,
            final Supplier<ComputationHandler> handler) throws IOException {
        final var export = arguments.stream().filter(a -> a.startsWith("export-vtree:")).findFirst();
        final LinkedHashMap<InputFile, ExperimentGroup.MergeResult<MinimizeSddExperiment.Result>> results
                = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("Bottom_Up", new MinimizeSddExperiment(),
                        MinimizationProblem.limitedMinimization(SddMinimizationStrategy.Strategies
                                .BOTTOM_UP, -1, -1, 10_000_000L)),
                new ExperimentEntry<>("Dec_Threshold", new MinimizeSddExperiment(),
                        MinimizationProblem.limitedMinimization(SddMinimizationStrategy.Strategies
                                .DEC_THRESHOLD, -1, -1, 10_000_000L)),
                new ExperimentEntry<>("Window", new MinimizeSddExperiment(),
                        MinimizationProblem.limitedMinimization(SddMinimizationStrategy.Strategies
                                .WINDOW, -1, -1, 10_000_000L))
        )).runExperiments(inputs, logger, handler);
        printResult(results, logger);
    }

    public static void minimizeDT(
            final List<InputFile> inputs, final List<String> arguments, final Logger logger,
            final Supplier<ComputationHandler> handler) throws IOException {
        final var export = arguments.stream()
                .filter(a -> a.startsWith("export:"))
                .findFirst()
                .map(it -> it.split(":")[1])
                .orElseGet(() -> null);
        final LinkedHashMap<InputFile, ExperimentGroup.MergeResult<MinimizeSddExperiment.Result>> results
                = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("Dec_Threshold", new MinimizeSddExperiment(export),
                        MinimizationProblem.limitedMinimization(SddMinimizationStrategy.Strategies
                                .DEC_THRESHOLD, -1, -1, 10_000_000L))
        )).runExperiments(inputs, logger, handler);
        printResult(results, logger);
    }

    public static void recompile(final List<InputFile> inputs, final List<String> arguments, final Logger logger,
                                 final Supplier<ComputationHandler> handler) throws IOException {
        final var vtreePath = new File(arguments.stream()
                .filter(a -> a.startsWith("import-path:"))
                .findFirst()
                .get()
                .split(":")[1]
        );
        final var results = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("Recompile", new RecompileExperiment(vtreePath), ProblemFunction::id),
                new ExperimentEntry<>("Default (TD)", new CompileSddExperiment(), ProblemFunction::id),
                new ExperimentEntry<>("Default (BU)",
                        new CompileSddExperiment(() -> Util.DEFAULT_COMPILER_CONFIG.get()
                                .compiler(SddCompilerConfig.Compiler.BOTTOM_UP)), ProblemFunction::id)
        )).runExperiments(inputs, logger, handler);
    }

    public static void minPME(final List<InputFile> inputs, final List<String> arguments, final Logger logger,
                              final Supplier<ComputationHandler> handler) throws IOException {
        final var importPath = new File(arguments.stream()
                .filter(a -> a.startsWith("import-path:"))
                .findFirst()
                .get()
                .split(":")[1]
        );
        final var results = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("Minimized PME", new MinPMEExperiment(100_000, importPath),
                        ProjectedCompilationSetups.exportedPdfToProjectionProblem),
                new ExperimentEntry<>("Normal PME", new EnumerationSddExperiment(100_000, true)
                        .withPureEncoding(false), ProjectedCompilationSetups.exportedPdfToProjectionProblem)
        )).runExperiments(inputs, logger, handler);
    }

    public static void minPMC(final List<InputFile> inputs, final List<String> arguments, final Logger logger,
                              final Supplier<ComputationHandler> handler) throws IOException {
        final var importPath = new File(arguments.stream()
                .filter(a -> a.startsWith("import-path:"))
                .findFirst()
                .get()
                .split(":")[1]
        );
        final var results = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("Minimized PME", new MinPMCExperiment(importPath),
                        ProjectedCompilationSetups.exportedPdfToProjectionProblem),
                new ExperimentEntry<>("Normal PME", new PmcPcSddExperiment(),
                        ProjectedCompilationSetups.exportedPdfToProjectionProblem),
                new ExperimentEntry<>("Normal PME", new PmcNaiveSddExperiment(),
                        ProjectedCompilationSetups.exportedPdfToProjectionProblem)
        )).runExperiments(inputs, logger, handler, ModelCountingSetups.COMPARE_MC);
    }

    private static void printResult(
            final LinkedHashMap<InputFile, ExperimentGroup.MergeResult<MinimizeSddExperiment.Result>> results,
            final Logger logger) {
        long total = 0;
        long sddTime = 0;
        long completed = 0;
        long crashed = 0;
        long startSize = 0;
        long finalSize = 0;
        for (final var result : results.values()) {
            final var r = result.results().getFirst().getSecond();
            if (r == null) {
                continue;
            }
            total++;
            if (r.time() == -1) {
                crashed += 1;
            } else {
                completed += 1;
                sddTime += r.time();
                startSize += r.startSize();
                finalSize += r.finalSize();
            }
        }

        logger.summary("=== Minimize Summary ===");
        logger.summary("");
        logger.summary(String.format("Total Problems: %d", total));
        logger.summary(String.format("Completed: %d", completed));
        logger.summary(String.format("Crashed: %d", crashed));
        if (completed > 0) {
            final double avgSddTime = ((double) sddTime) / completed;
            final long avgSddStartSize = startSize / completed;
            final long avgSddFinalSize = finalSize / completed;
            final double avgImprovement = ((double) startSize / finalSize);
            logger.summary(String.format("Time: %dms (%.2fms/p)", sddTime, avgSddTime));
            logger.summary(String.format("Start Size: %d nodes (%d nodes/p)", startSize, avgSddStartSize));
            logger.summary(String.format("Final Size: %d nodes (%d nodes/p)", finalSize, avgSddFinalSize));
            logger.summary(String.format("Improvement: %.2f", avgImprovement));
        }
    }
}
