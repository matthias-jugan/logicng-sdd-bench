package com.booleworks.logicng_sdd_bench.experiments.results;

import java.util.List;

public record SizeResult(long size) implements ExperimentResult {
    @Override
    public List<String> getResult() {
        return List.of(String.valueOf(size));
    }

    @Override
    public String getEssentialsAsCsv() {
        return String.valueOf(size);
    }
}
