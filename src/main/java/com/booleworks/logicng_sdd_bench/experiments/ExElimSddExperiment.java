package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.knowledgecompilation.sdd.algorithms.SddSize;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompiler;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerConfig;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.Sdd;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddNode;
import com.booleworks.logicng.knowledgecompilation.sdd.functions.SddProjectionFunction;
import com.booleworks.logicng.util.Pair;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;
import com.booleworks.logicng_sdd_bench.experiments.results.ExperimentResult;
import com.booleworks.logicng_sdd_bench.trackers.SegmentedTimeTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ExElimSddExperiment implements Experiment<ProjectionProblem, ExElimSddExperiment.Result> {
    final Supplier<SddCompilerConfig.Builder> config;

    public ExElimSddExperiment(final Supplier<SddCompilerConfig.Builder> config) {
        this.config = config;
    }

    public ExElimSddExperiment() {
        this(Util.DEFAULT_COMPILER_CONFIG);
    }

    @Override
    public Result execute(final ProjectionProblem input, final FormulaFactory f, final Logger logger,
                          final Supplier<ComputationHandler> handler) {
        final SegmentedTimeTracker tracker = new SegmentedTimeTracker();
        final ComputationHandler h = handler.get();
        tracker.start();
        final var c = config.get().build();
        final var compiled = SddCompiler.compile(input.formula().cnf(f), c, f, h);
        if (!compiled.isSuccess()) {
            tracker.timeout();
            return new Result(tracker, -1, -1);
        }
        final Sdd sdd = compiled.getResult().getSdd();
        tracker.end("Compilation");

        final LngResult<SddNode> projected =
                compiled.getResult().getNode().execute(new SddProjectionFunction(input.projectedVariables(), sdd), h);
        if (!projected.isSuccess()) {
            tracker.timeout();
            return new Result(tracker, -1, -1);
        }
        tracker.end("Quantification");
        return new Result(tracker, SddSize.size(projected.getResult()), sdd.getSddNodeCount());
    }

    public record Result(SegmentedTimeTracker tracker, long nodeSize, long sddSize) implements ExperimentResult {

        @Override
        public List<String> getResult() {
            final var res = new ArrayList<>(tracker.getResult());
            res.add(String.valueOf(nodeSize));
            res.add(String.valueOf(sddSize));
            return res;
        }

        @Override
        public String getEssentialsAsCsv() {
            final var compilationTime = tracker.getTimes().stream()
                    .filter(p -> p.getFirst().equals("Compilation"))
                    .map(Pair::getSecond)
                    .findFirst()
                    .orElse(-1L);
            final var quantificationTime = tracker.getTimes().stream()
                    .filter(p -> p.getFirst().equals("Quantification"))
                    .map(Pair::getSecond)
                    .findFirst()
                    .orElse(-1L);
            return tracker.getEssentialsAsCsv() + "," + compilationTime + "," + quantificationTime + "," + nodeSize
                    + "," + sddSize;
        }
    }
}
