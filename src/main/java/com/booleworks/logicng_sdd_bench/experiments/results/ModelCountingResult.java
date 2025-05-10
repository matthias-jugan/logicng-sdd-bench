package com.booleworks.logicng_sdd_bench.experiments.results;

import java.math.BigInteger;
import java.util.List;

public record ModelCountingResult(long time, BigInteger count) implements ExperimentResult {
    private static final ModelCountingResult INVALID = new ModelCountingResult(-1, null);

    public static ModelCountingResult invalid() {
        return INVALID;
    }

    @Override
    public List<String> getResult() {
        return List.of(String.valueOf(time));
    }

    @Override
    public String getEssentialsAsCsv() {
        return String.valueOf(time);
    }
}
