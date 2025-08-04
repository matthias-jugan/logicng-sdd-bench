package com.booleworks.logicng_sdd_bench.experiments.results;

import java.util.List;

public record EmptyResult() implements ExperimentResult {
    @Override
    public List<String> getResult() {
        return List.of();
    }

    @Override
    public String getEssentialsAsCsv() {
        return "";
    }
}
