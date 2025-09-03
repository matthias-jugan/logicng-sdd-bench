package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;
import com.booleworks.logicng_sdd_bench.experiments.results.ModelEnumerationResult;

import java.io.File;
import java.util.function.Supplier;

public class MinPMEExperiment implements Experiment<ProjectionProblem, ModelEnumerationResult> {
    final File path;
    final int limit;

    public MinPMEExperiment(final int limit, final File path) {
        this.limit = limit;
        this.path = path;
    }

    @Override
    public ModelEnumerationResult execute(final ProjectionProblem input, final FormulaFactory f, final Logger logger,
                                          final Supplier<ComputationHandler> handler) {
        //        final var file = Arrays.stream(Objects.requireNonNull(path.listFiles()))
        //                .filter(p -> p.getName().startsWith(ExperimentGroup.DIRTY_HACK_CURRENT_FILE.name()))
        //                .findFirst()
        //                .get();
        //        logger.event(String.format("Import sdd from %s", file));
        //        final int numberVars = input.projectedVariables().size();
        //        final Set<Variable> additionalVars = input.quantifiedVariables();
        //        final SegmentedTimeTracker tracker = new SegmentedTimeTracker();
        //        final ComputationHandler h = handler.get();
        //        tracker.start();
        //        try {
        //            final Pair<SddNode, Sdd> imp = SddReader.readSdd(file, f);
        //            final Sdd sdd = imp.getSecond();
        //            final SddNode node = imp.getFirst();
        //            tracker.end("Import");
        //            final ComputationHandler meHandler;
        //            if (limit > 0) {
        //                final var modelLimitHandler = new NumberOfModelsHandler(limit);
        //                meHandler = new HandlerGroup(List.of(h, modelLimitHandler));
        //            } else {
        //                meHandler = h;
        //            }
        //            final var meFunc = SddModelEnumerationFunction
        //                    .builder(input.projectedVariables(), sdd)
        //                    .additionalVariables(additionalVars)
        //                    .build();
        //            final var models = node.execute(meFunc, meHandler);
        //            if (models.isSuccess() || models.isPartial()) {
        //                tracker.end("Enumeration");
        //                return new ModelEnumerationResult(limit, models.getPartialResult().size(),
        //                        null, tracker, numberVars, additionalVars.size());
        //            } else {
        //                tracker.timeout();
        //                return new ModelEnumerationResult(limit, -1, null, tracker, numberVars, additionalVars.size
        //                ());
        //            }
        //        } catch (final ParserException | IOException e) {
        //            throw new RuntimeException(e);
        //        }
        return null;
    }
}
