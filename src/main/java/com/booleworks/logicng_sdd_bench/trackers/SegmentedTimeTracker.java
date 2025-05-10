package com.booleworks.logicng_sdd_bench.trackers;

import com.booleworks.logicng.util.Pair;
import com.booleworks.logicng_sdd_bench.experiments.results.ExperimentResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SegmentedTimeTracker implements ExperimentResult {
    private long globalStart = -1;
    private long globalTime = -1;
    private long lastTime = -1;
    private final ArrayList<Pair<String, Long>> times = new ArrayList<>();

    public SegmentedTimeTracker() {
    }

    public void start() {
        globalStart = System.currentTimeMillis();
        lastTime = System.currentTimeMillis();
    }

    public void end(final String id) {
        times.add(new Pair<>(id, System.currentTimeMillis() - lastTime));
        lastTime = System.currentTimeMillis();
        globalTime = lastTime - globalStart;
    }

    public void timeout() {
        globalTime = -1;
    }

    @Override
    public List<String> getResult() {
        return Stream.concat(Stream.of(globalTime), times.stream()).map(Object::toString).collect(Collectors.toList());
    }

    @Override
    public String getEssentialsAsCsv() {
        return String.valueOf(globalTime);
    }
}
