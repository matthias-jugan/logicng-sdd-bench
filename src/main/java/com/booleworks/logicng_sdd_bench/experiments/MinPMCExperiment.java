package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.io.parsers.ParserException;
import com.booleworks.logicng.io.readers.SddReader;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.Sdd;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddNode;
import com.booleworks.logicng.knowledgecompilation.sdd.functions.SddModelCountFunction;
import com.booleworks.logicng.util.Pair;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;
import com.booleworks.logicng_sdd_bench.experiments.results.ModelCountingResult;
import com.booleworks.logicng_sdd_bench.trackers.SegmentedTimeTracker;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

public class MinPMCExperiment implements Experiment<ProjectionProblem, ModelCountingResult> {
    final File path;

    public MinPMCExperiment(final File path) {
        this.path = path;
    }

    @Override
    public ModelCountingResult execute(final ProjectionProblem input, final FormulaFactory f, final Logger logger,
                                       final Supplier<ComputationHandler> handler) {
        final var file = Arrays.stream(Objects.requireNonNull(path.listFiles()))
                .filter(p -> p.getName().startsWith(ExperimentGroup.DIRTY_HACK_CURRENT_FILE.name()))
                .findFirst()
                .get();
        logger.event(String.format("Import sdd from %s", file));
        final SegmentedTimeTracker tracker = new SegmentedTimeTracker();
        final ComputationHandler h = handler.get();
        tracker.start();
        try {
            final Pair<SddNode, Sdd> imp = SddReader.readSdd(file, f);
            final Sdd sdd = imp.getSecond();
            final SddNode node = imp.getFirst();
            tracker.end("Import");
            final var mcFunc = new SddModelCountFunction(input.projectedVariables(), sdd);
            final var models = node.execute(mcFunc, h);
            if (models.isSuccess() || models.isPartial()) {
                tracker.end("Enumeration");
                return new ModelCountingResult(models.getResult(), tracker);
            } else {
                tracker.timeout();
                return new ModelCountingResult(null, tracker);
            }
        } catch (final ParserException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
