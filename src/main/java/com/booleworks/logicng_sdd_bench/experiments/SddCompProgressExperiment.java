package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.events.SimpleEvent;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerTopDown;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.experiments.results.ExperimentResult;
import com.booleworks.logicng_sdd_bench.trackers.BenchmarkEvent;
import com.booleworks.logicng_sdd_bench.trackers.SimpleCounter;
import com.booleworks.logicng_sdd_bench.trackers.SimpleTimeTracker;
import com.booleworks.logicng_sdd_bench.trackers.TrackerGroup;

import java.util.List;
import java.util.function.Supplier;

public class SddCompProgressExperiment extends Experiment<Formula, SddCompProgressExperiment.Result> {

    @Override
    public Result execute(final Formula input, final FormulaFactory f, final Logger logger,
                          final Supplier<ComputationHandler> handler) {
        final var timeTracker = new SimpleTimeTracker();
        final var expansionTracker = new SimpleCounter(SimpleEvent.SDD_SHANNON_EXPANSION);
        final var tracker = new TrackerGroup(List.of(expansionTracker, timeTracker), handler.get());
        tracker.shouldResume(BenchmarkEvent.START_EXPERIMENT);
        final var result = SddCompilerTopDown.compile(input, f, tracker);
        if (result.isSuccess()) {
            tracker.shouldResume(BenchmarkEvent.COMPLETED_EXPERIMENT);
        } else {
            tracker.shouldResume(BenchmarkEvent.ABORTED_EXPERIMENT);
        }
        return new Result(expansionTracker.getCounter(), timeTracker.getTime());
    }

    @Override
    public List<String> getLabels() {
        return List.of();
    }

    public record Result(long expansions, long time) implements ExperimentResult {

        @Override
        public List<String> getResult() {
            return List.of(String.valueOf(expansions), String.valueOf(time));
        }

        @Override
        public String getEssentialsAsCsv() {
            return String.valueOf(time);
        }
    }
}
