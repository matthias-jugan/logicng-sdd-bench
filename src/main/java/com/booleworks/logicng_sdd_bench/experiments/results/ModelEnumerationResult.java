package com.booleworks.logicng_sdd_bench.experiments.results;

import com.booleworks.logicng.datastructures.Model;
import com.booleworks.logicng_sdd_bench.trackers.SegmentedTimeTracker;

import java.util.ArrayList;
import java.util.List;

public record ModelEnumerationResult(
        int limit, int modelCount, ArrayList<Model> models, SegmentedTimeTracker times, int projectionVariables,
        int additionalVariables
) implements ExperimentResult {
    @Override
    public List<String> getResult() {
        final var list = new ArrayList<String>();
        list.add(String.valueOf(modelCount));
        list.addAll(times.getResult());
        return list;
    }

    @Override
    public String getEssentialsAsCsv() {
        return times.getEssentialsAsCsv() + "," + modelCount + "," + limit + "," + projectionVariables + ","
                + additionalVariables;
    }
}
