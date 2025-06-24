package com.booleworks.logicng_sdd_bench.setups;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.util.Pair;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Settings;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentGroup;
import com.booleworks.logicng_sdd_bench.experiments.MinimizeSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.problems.MinimizationProblem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

public class MinimizationSetups {
    public static void minimize(
            final List<InputFile> inputs, final Settings settings, final Logger logger,
            final Supplier<ComputationHandler> handler) {
        final LinkedHashMap<InputFile, ExperimentGroup.MergeResult<MinimizeSddExperiment.Result>> results
                = new ExperimentGroup<>(List.of(
                new Pair<>("Minimize (Unlim)", new MinimizeSddExperiment())
        )).runExperiments(inputs, logger, MinimizationProblem.minimizationWithTimeouts(-1, -1), handler);
        printResult(results, logger);
    }

    public static void minimize2(
            final List<InputFile> inputs, final Settings settings, final Logger logger,
            final Supplier<ComputationHandler> handler) {
        final LinkedHashMap<InputFile, ExperimentGroup.MergeResult<MinimizeSddExperiment.Result>> results
                = new ExperimentGroup<>(List.of(
                new Pair<>("Minimize", new MinimizeSddExperiment())
        )).runExperiments(inputs, logger, MinimizationProblem.minimizationWithTimeouts(-1, 5), handler);
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
