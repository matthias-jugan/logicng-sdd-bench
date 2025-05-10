package com.booleworks.logicng_sdd_bench.experiments.results;

import java.util.List;

public interface ExperimentResult {
    List<String> getResult();

    String getEssentialsAsCsv();
}
