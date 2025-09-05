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
        final int vars = input.formula().variables(f).size();
        final int projVars = input.projectedVariables().size();
        final SegmentedTimeTracker tracker = new SegmentedTimeTracker();
        final ComputationHandler h = handler.get();
        tracker.start();
        final var c = config.get().build();
        final var compiled = SddCompiler.compile(input.formula().cnf(f), c, f, h);
        if (!compiled.isSuccess()) {
            tracker.timeout();
            return new Result(tracker, -1, -1, -1, -1, vars, projVars);
        }
        final Sdd sdd = compiled.getResult().getSdd();
        sdd.pin(compiled.getResult().getNode());
        final long originalNodeSize = SddSize.size(compiled.getResult().getNode());
        final long originalContainerSize = sdd.getActiveSize();
        tracker.end("Compilation");

        final LngResult<SddNode> projected =
                compiled.getResult().getNode().execute(new SddProjectionFunction(input.projectedVariables(), sdd), h);
        if (!projected.isSuccess()) {
            tracker.timeout();
            return new Result(tracker, originalNodeSize, originalContainerSize, -1, -1, vars, projVars);
        }
        tracker.end("Quantification");
        sdd.pin(projected.getResult());
        final long projectedNodeSize = SddSize.size(projected.getResult());
        final long projectedContainerSize = sdd.getActiveSize();
        return new Result(tracker, originalNodeSize, originalContainerSize, projectedNodeSize, projectedContainerSize,
                vars, projVars);
    }

    public record Result(
            SegmentedTimeTracker tracker, long originalNodeSize, long originalContainerSize, long projectedNodeSize,
            long projectedContainerSize, int vars, int projVars
    ) implements ExperimentResult {

        @Override
        public List<String> getResult() {
            final var res = new ArrayList<>(tracker.getResult());
            res.add(String.valueOf(originalNodeSize));
            res.add(String.valueOf(originalContainerSize));
            res.add(String.valueOf(projectedNodeSize));
            res.add(String.valueOf(projectedContainerSize));
            res.add(String.valueOf(vars));
            res.add(String.valueOf(projVars));
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
            return tracker.getEssentialsAsCsv() + "," + compilationTime + "," + quantificationTime + ","
                    + originalNodeSize
                    + "," + originalContainerSize + "," + projectedNodeSize + "," + projectedContainerSize + "," + vars
                    + "," + projVars;
        }
    }
}
