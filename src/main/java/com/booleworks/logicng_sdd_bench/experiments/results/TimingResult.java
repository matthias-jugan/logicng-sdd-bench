package com.booleworks.logicng_sdd_bench.experiments.results;

import java.util.List;

public record TimingResult(long time) implements ExperimentResult {
    private static final TimingResult INVALID = new TimingResult(-1);

    public static TimingResult invalid() {
        return INVALID;
    }

    @Override
    public List<String> getResult() {
        return List.of(String.valueOf(time));
    }

    public static List<String> getLabels() {
        return List.of("time_ms");
    }

    @Override
    public String getEssentialsAsCsv() {
        return String.valueOf(time);
    }
}
