package com.booleworks.logicng_sdd_bench.experiments.results;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.events.ComputationStartedEvent;
import com.booleworks.logicng.handlers.events.EnumerationFoundModelsEvent;
import com.booleworks.logicng.handlers.events.LngEvent;
import com.booleworks.logicng.handlers.events.SimpleEvent;
import com.booleworks.logicng.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ModelEnumerationResult implements ExperimentResult, ComputationHandler {
    private static final int STEP_SIZE = 100;

    private final int limit;
    private final int projectionVariables;
    private final int additionalVariables;
    private final ArrayList<Pair<Long, Integer>> times;
    private final long startTime;
    private final boolean autoCommit;

    private int nextMeasurePoint = STEP_SIZE;
    private int countCommitted;
    private int countUncommitted;

    public ModelEnumerationResult(final boolean autoCommit, final int limit, final int projectionVariables,
                                  final int additionalVariables) {
        this.limit = limit;
        this.times = new ArrayList<>();
        this.projectionVariables = projectionVariables;
        this.additionalVariables = additionalVariables;
        this.startTime = System.currentTimeMillis();
        this.autoCommit = autoCommit;
    }

    public void addCount(final int count) {
        times.add(new Pair<>(System.currentTimeMillis() - startTime, count));
    }

    @Override
    public List<String> getResult() {
        final String allTimes = this.times
                .stream()
                .map(p -> p.getFirst() + "," + p.getSecond())
                .collect(Collectors.joining(","));
        return List.of(
                String.valueOf(limit),
                String.valueOf(projectionVariables),
                String.valueOf(additionalVariables),
                allTimes
        );
    }

    @Override
    public String getEssentialsAsCsv() {
        final String allTimes = this.times
                .stream()
                .map(p -> p.getFirst() + ";" + p.getSecond())
                .collect(Collectors.joining(";"));
        return limit + "," + projectionVariables + "," + additionalVariables + "," + allTimes;
    }

    @Override
    public boolean shouldResume(final LngEvent event) {
        if (event == ComputationStartedEvent.MODEL_ENUMERATION_STARTED) {
            countCommitted = 0;
            countUncommitted = 0;
        } else if (event instanceof EnumerationFoundModelsEvent) {
            final int numberOfModels = ((EnumerationFoundModelsEvent) event).getNumberOfModels();
            if (autoCommit) {
                countCommitted += numberOfModels;
            } else {
                countUncommitted += numberOfModels;
            }
        } else if (event == SimpleEvent.MODEL_ENUMERATION_COMMIT) {
            countCommitted += countUncommitted;
            countUncommitted = 0;
        } else if (event == SimpleEvent.MODEL_ENUMERATION_ROLLBACK) {
            countUncommitted = 0;
        }
        if (countCommitted >= nextMeasurePoint) {
            addCount(countCommitted);
            nextMeasurePoint = (countCommitted - (countCommitted % STEP_SIZE)) + STEP_SIZE;
        }
        return limit <= 0 || countCommitted < limit;
    }
}
