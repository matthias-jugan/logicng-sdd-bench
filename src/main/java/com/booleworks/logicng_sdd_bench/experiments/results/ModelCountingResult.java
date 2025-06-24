package com.booleworks.logicng_sdd_bench.experiments.results;

import com.booleworks.logicng_sdd_bench.trackers.SegmentedTimeTracker;

import java.math.BigInteger;
import java.util.List;

public record ModelCountingResult(BigInteger count, SegmentedTimeTracker times) implements ExperimentResult {
    @Override
    public List<String> getResult() {
        return times.getResult();
    }

    @Override
    public String getEssentialsAsCsv() {
        return times.getEssentialsAsCsv();
    }
}
