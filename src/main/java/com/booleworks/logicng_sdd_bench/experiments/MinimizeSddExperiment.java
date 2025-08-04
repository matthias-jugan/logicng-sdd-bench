package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.io.writers.SddWriter;
import com.booleworks.logicng.knowledgecompilation.sdd.algorithms.SddMinimization;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompiler;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerConfig;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddMinimizationConfig;
import com.booleworks.logicng.util.Pair;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.experiments.problems.MinimizationProblem;
import com.booleworks.logicng_sdd_bench.experiments.results.ExperimentResult;
import com.booleworks.logicng_sdd_bench.trackers.BenchmarkEvent;
import com.booleworks.logicng_sdd_bench.trackers.SddSizeTracker;
import com.booleworks.logicng_sdd_bench.trackers.TrackerGroup;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MinimizeSddExperiment implements Experiment<MinimizationProblem, MinimizeSddExperiment.Result> {

    @Override
    public Result execute(final MinimizationProblem input, final FormulaFactory f, final Logger logger,
                          final Supplier<ComputationHandler> handler) {
        final var handlerInstance = handler.get();
        final var sddSizeTracker = new SddSizeTracker();
        final var tracker = new TrackerGroup(List.of(sddSizeTracker), handlerInstance);
        final var compConfig = SddCompilerConfig.builder().build();
        final var compilationResult = SddCompiler.compile(input.formula().cnf(f), compConfig, f, handlerInstance);
        if (!compilationResult.isSuccess()) {
            return new Result(-1, -1, -1, List.of(), null);
        }
        final var compilation = compilationResult.getResult();
        if (compilation.getSdd().getVTreeStack().isEmpty()) {
            sddSizeTracker.getAllSizes().add(new Pair<>(1L, 0L));
            return new Result(0, 1, 1, sddSizeTracker.getAllSizes(), null);
        }
        compilation.getSdd().pin(compilation.getNode());
        final int startSize = compilation.getSdd().getActiveSize();
        sddSizeTracker.getAllSizes().add(new Pair<>((long) startSize, 0L));
        tracker.shouldResume(BenchmarkEvent.START_EXPERIMENT);
        final var config = new SddMinimizationConfig.Builder(compilation.getSdd())
                .withAlgorithm(input.alg())
                .withTotalTimeout(input.timeout())
                .withOperationTimeout(input.opTimeout())
                .withNodeLimit(input.nodeLimit())
                .withUserHandler(tracker)
                .build();
        final var minimizedResult = SddMinimization.minimize(config);
        if (minimizedResult.isPartial() || minimizedResult.isSuccess()) {
            tracker.shouldResume(BenchmarkEvent.COMPLETED_EXPERIMENT);
            final int finalSize = compilation.getSdd().getActiveSize();
            sddSizeTracker.getAllSizes().add(new Pair<>((long) finalSize, sddSizeTracker.getCurrentTime()));
            final StringWriter sw = new StringWriter();
            try {
                SddWriter.writeVTree(sw, compilation.getSdd());
                sw.flush();
                sw.close();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            return new Result(sddSizeTracker.getTime(), startSize, finalSize, sddSizeTracker.getAllSizes(),
                    sw.toString());
        } else {
            tracker.shouldResume(BenchmarkEvent.ABORTED_EXPERIMENT);
            final int finalSize = compilation.getSdd().getActiveSize();
            sddSizeTracker.getAllSizes().add(new Pair<>((long) finalSize, sddSizeTracker.getCurrentTime()));
            return new Result(-1, startSize, -1, sddSizeTracker.getAllSizes(), null);
        }
    }

    public record Result(
            long time, int startSize, int finalSize, List<Pair<Long, Long>> allSizes, String vtreeExport
    ) implements ExperimentResult {
        @Override
        public List<String> getResult() {
            final String allSizes = this.allSizes
                    .stream()
                    .map(p -> p.getFirst() + "," + p.getSecond())
                    .collect(Collectors.joining(","));
            return List.of(String.valueOf(time), allSizes);
        }

        @Override
        public String getEssentialsAsCsv() {
            final String allSizes = this.allSizes
                    .stream()
                    .map(p -> p.getFirst() + ";" + p.getSecond())
                    .collect(Collectors.joining(";"));
            return time + "," + allSizes;
        }
    }
}
