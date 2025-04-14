package com.booleworks.logicng_sdd_bench;

import com.booleworks.logicng.util.Pair;
import com.booleworks.logicng_sdd_bench.experiments.results.ExperimentResult;

import java.util.List;

public interface ValidationFunction<E extends ExperimentResult> {
    static <R extends ExperimentResult> ValidationFunction<R> valid() {
        return (var l) -> null;
    }

    String validate(List<Pair<String, E>> results);

}
