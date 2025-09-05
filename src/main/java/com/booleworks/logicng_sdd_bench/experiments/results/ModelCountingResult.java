package com.booleworks.logicng_sdd_bench.experiments.results;

import com.booleworks.logicng.util.Pair;
import com.booleworks.logicng_sdd_bench.trackers.SegmentedTimeTracker;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public record ModelCountingResult(BigInteger count, SegmentedTimeTracker times) implements ExperimentResult {
    @Override
    public List<String> getResult() {
        final var list = new ArrayList<String>();
        list.add(String.valueOf(count));
        list.addAll(times.getResult());
        return list;
    }

    @Override
    public String getEssentialsAsCsv() {
        final var compilationTime = times.getTimes().stream()
                .filter(p -> p.getFirst().equals("Compilation"))
                .map(Pair::getSecond)
                .findFirst()
                .orElse(-1L);
        final var projectionTime = times.getTimes().stream()
                .filter(p -> p.getFirst().equals("Projection"))
                .map(Pair::getSecond)
                .findFirst()
                .orElse(-1L);
        final var countingTime = times.getTimes().stream()
                .filter(p -> p.getFirst().equals("Counting"))
                .map(Pair::getSecond)
                .findFirst()
                .orElse(-1L);
        return count + "," + times.getEssentialsAsCsv() + "," + compilationTime + "," + projectionTime + ","
                + countingTime;
    }
}
