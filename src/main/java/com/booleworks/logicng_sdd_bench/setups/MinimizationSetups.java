package com.booleworks.logicng_sdd_bench.setups;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.knowledgecompilation.sdd.algorithms.SddMinimizationStrategy;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentEntry;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentGroup;
import com.booleworks.logicng_sdd_bench.experiments.MinimizeSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.problems.MinimizationProblem;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

public class MinimizationSetups {
    public static void minimize(
            final List<InputFile> inputs, final List<String> arguments, final Logger logger,
            final Supplier<ComputationHandler> handler) throws IOException {
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
    }
}
