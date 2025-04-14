package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng_sdd_bench.experiments.results.ExperimentResult;

import java.util.List;

public record CompilationData(long time, int logicalSize, long physicalSize) implements ExperimentResult {
    @Override
    public List<String> getResult() {
        return List.of(String.valueOf(time), String.valueOf(logicalSize), String.valueOf(physicalSize));
    }
}
