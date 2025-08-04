package com.booleworks.logicng_sdd_bench.setups;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddMinimizationConfig;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentEntry;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentGroup;
import com.booleworks.logicng_sdd_bench.experiments.MinimizeSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.problems.MinimizationProblem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
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
                        MinimizationProblem.limitedMinimization(SddMinimizationConfig.Algorithm
                                .BOTTOM_UP, -1, -1, 10_000_000L)),
                new ExperimentEntry<>("Dec_Threshold", new MinimizeSddExperiment(),
                        MinimizationProblem.limitedMinimization(SddMinimizationConfig.Algorithm
                                .DEC_THRESHOLD, -1, -1, 10_000_000L)),
                new ExperimentEntry<>("Const_Dec", new MinimizeSddExperiment(),
                        MinimizationProblem.limitedMinimization(SddMinimizationConfig.Algorithm
                                        .CONST_DEC, -1, -1,
                                10_000_000L)), new ExperimentEntry<>("Window", new MinimizeSddExperiment(),
                        MinimizationProblem.limitedMinimization(SddMinimizationConfig.Algorithm
                                .WINDOW, -1, -1, 10_000_000L))
        )).runExperiments(inputs, logger, handler);
        if (export.isPresent()) {
            final String path = export.get().split(":")[1];
            for (final var resultEntry : results.entrySet()) {
                for (final var experimentResult : resultEntry.getValue().results()) {
                    if (experimentResult.getSecond().vtreeExport() != null) {
                        final File file = Path.of(path,
                                        String.format("%s_%s.vtree", resultEntry.getKey().name(),
                                                experimentResult.getFirst()))
                                .toFile();
                        try (
                                final FileWriter writer = new FileWriter(file)
                        ) {
                            writer.write(experimentResult.getSecond().vtreeExport());
                            writer.flush();
                        }
                    }
                }
            }
        }
        printResult(results, logger);
    }

    public static void minimize2(
            final List<InputFile> inputs, final List<String> arguments, final Logger logger,
            final Supplier<ComputationHandler> handler) {
        final LinkedHashMap<InputFile, ExperimentGroup.MergeResult<MinimizeSddExperiment.Result>> results
                = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("Minimize (Unlim)", new MinimizeSddExperiment(),
                        MinimizationProblem.limitedMinimization(SddMinimizationConfig.Algorithm.DEC_THRESHOLD, -1, -1,
                                10_000_000L))
        )).runExperiments(inputs, logger, handler);
        printResult(results, logger);
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
