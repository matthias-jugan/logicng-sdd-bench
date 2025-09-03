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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MinimizeSddExperiment implements Experiment<MinimizationProblem, MinimizeSddExperiment.Result> {
    final String exportPath;

    public MinimizeSddExperiment(final String exportPath) {
        this.exportPath = exportPath;
    }

    public MinimizeSddExperiment() {
        this.exportPath = null;
    }

    @Override
    public Result execute(final MinimizationProblem input, final FormulaFactory f, final Logger logger,
                          final Supplier<ComputationHandler> handler) {
        final var handlerInstance = handler.get();
        final var sddSizeTracker = new SddSizeTracker();
        final var tracker = new TrackerGroup(List.of(sddSizeTracker), handlerInstance);
        final var compConfig = SddCompilerConfig.builder().build();
        final var compilation = SddCompiler.compile(input.formula().cnf(f), compConfig, f);
        if (compilation.getSdd().getVTreeStack().isEmpty()) {
            sddSizeTracker.getAllSizes().add(new Pair<>(1L, 0L));
            return new Result(0, 1, 1, sddSizeTracker.getAllSizes());
        }
        compilation.getSdd().pin(compilation.getNode());
        final int startSize = compilation.getSdd().getActiveSize();
        sddSizeTracker.getAllSizes().add(new Pair<>((long) startSize, 0L));
        tracker.shouldResume(BenchmarkEvent.START_EXPERIMENT);
        final var config = new SddMinimizationConfig(compilation.getSdd())
                .totalTimeout(input.timeout())
                .operationTimeout(input.opTimeout())
                .nodeLimit(input.nodeLimit())
                .userHandler(tracker);
        final var minimizedResult = SddMinimization.minimize(config);
        if (minimizedResult.isPartial() || minimizedResult.isSuccess()) {
            tracker.shouldResume(BenchmarkEvent.COMPLETED_EXPERIMENT);
            final int finalSize = compilation.getSdd().getActiveSize();
            sddSizeTracker.getAllSizes().add(new Pair<>((long) finalSize, sddSizeTracker.getCurrentTime()));
            if (exportPath != null) {
                final File file = Path.of(exportPath,
                                String.format("%s_%s.sdd", ExperimentGroup.DIRTY_HACK_CURRENT_FILE.name(),
                                        input.alg()))
                        .toFile();
                try {
                    SddWriter.writeSdd(file, minimizedResult.getPartialResult().map(compilation.getNode()),
                            compilation.getSdd());
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return new Result(sddSizeTracker.getTime(), startSize, finalSize, sddSizeTracker.getAllSizes());
        } else {
            tracker.shouldResume(BenchmarkEvent.ABORTED_EXPERIMENT);
            final int finalSize = compilation.getSdd().getActiveSize();
            sddSizeTracker.getAllSizes().add(new Pair<>((long) finalSize, sddSizeTracker.getCurrentTime()));
            return new Result(-1, startSize, -1, sddSizeTracker.getAllSizes());
        }
    }

    public record Result(
            long time, int startSize, int finalSize, List<Pair<Long, Long>> allSizes
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
