package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.knowledgecompilation.sdd.algorithms.SddMinimization;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerTopDown;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddMinimizationConfig;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.experiments.problems.MinimizationProblem;
import com.booleworks.logicng_sdd_bench.experiments.results.ExperimentResult;
import com.booleworks.logicng_sdd_bench.trackers.BenchmarkEvent;
import com.booleworks.logicng_sdd_bench.trackers.SddSizeTracker;
import com.booleworks.logicng_sdd_bench.trackers.SimpleTimeTracker;
import com.booleworks.logicng_sdd_bench.trackers.TrackerGroup;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MinimizeSddExperiment extends Experiment<MinimizationProblem, MinimizeSddExperiment.Result> {

    @Override
    public Result execute(final MinimizationProblem input, final FormulaFactory f, final Logger logger,
                          final Supplier<ComputationHandler> handler) {
        final var handlerInstance = handler.get();
        final var timeTracker = new SimpleTimeTracker();
        final var sddSizeTracker = new SddSizeTracker();
        final var tracker = new TrackerGroup(List.of(timeTracker, sddSizeTracker), handlerInstance);
        final var compilationResult = SddCompilerTopDown.compile(input.formula(), f, handlerInstance);
        if (!compilationResult.isSuccess()) {
            return new Result(-1, -1, -1, List.of());
        }
        final var compilation = compilationResult.getResult();
        compilation.getSdd().pin(compilation.getNode());
        final int startSize = compilation.getSdd().getActiveSize();
        sddSizeTracker.getAllSizes().add(startSize);
        tracker.shouldResume(BenchmarkEvent.START_EXPERIMENT);
        final var config = new SddMinimizationConfig.Builder(compilation.getSdd())
                .withTotalTimeout(input.timeout())
                .withOperationTimeout(input.opTimeout())
                .withUserHandler(tracker)
                .build();
        final var minimizedResult = SddMinimization.minimize(compilation.getSdd(), config);
        if (minimizedResult.isPartial() || minimizedResult.isSuccess()) {
            tracker.shouldResume(BenchmarkEvent.COMPLETED_EXPERIMENT);
            final int finalSize = compilation.getSdd().getActiveSize();
            sddSizeTracker.getAllSizes().add(finalSize);
            return new Result(timeTracker.getTime(), startSize, finalSize, sddSizeTracker.getAllSizes());
        } else {
            tracker.shouldResume(BenchmarkEvent.ABORTED_EXPERIMENT);
            return new Result(-1, startSize, -1, sddSizeTracker.getAllSizes());
        }
    }

    @Override
    public List<String> getLabels() {
        return List.of("Time", "Start Size", "Final Size", "All Sizes");
    }

    public record Result(long time, int startSize, int finalSize, List<Integer> allSizes) implements ExperimentResult {
        @Override
        public List<String> getResult() {
            final String allSizes = this.allSizes.stream().map(String::valueOf).collect(Collectors.joining(","));
            return List.of(String.valueOf(time), String.valueOf(startSize), String.valueOf(finalSize), allSizes);
        }

        @Override
        public String getEssentialsAsCsv() {
            return String.valueOf(time);
        }
    }
}
