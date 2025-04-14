package com.booleworks.logicng_sdd_bench;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.util.Pair;
import com.booleworks.logicng_sdd_bench.experiments.CompileDnnfExperiment;
import com.booleworks.logicng_sdd_bench.experiments.CompileSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.CountModelsDnnfExperiment;
import com.booleworks.logicng_sdd_bench.experiments.CountModelsSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentGroup;
import com.booleworks.logicng_sdd_bench.experiments.results.ModelCountingResult;
import com.booleworks.logicng_sdd_bench.experiments.results.TimingResult;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class Setups {
    public static HashMap<InputFile, ExperimentGroup.MergeResult<ModelCountingResult>> modelCounting(
            final List<InputFile> inputs, final Supplier<ComputationHandler> handler) {

        final var results
                = new ExperimentGroup<>(List.of(
                new Pair<>("DNNF Count", new CountModelsDnnfExperiment()),
                new Pair<>("SDD Count", new CountModelsSddExperiment())
        )).runExperiments(inputs, handler, (vs) -> {
            if (vs.isEmpty()) {
                return null;
            }
            BigInteger ref = null;
            for (final var v : vs) {
                if (ref == null) {
                    ref = v.getSecond().count();
                } else {
                    if (v.getSecond() != null && !ref.equals(v.getSecond().count())) {
                        return "Got model count " + v.getSecond().count() + " in " + v.getFirst()
                                + " but there is also a result with another model count " + ref;
                    }
                }
            }
            return null;
        });
        return results;
    }

    public static HashMap<InputFile, ExperimentGroup.MergeResult<TimingResult>> compilation(
            final List<InputFile> inputs, final Supplier<ComputationHandler> handler) {

        final var results = new ExperimentGroup<>(List.of(
                new Pair<>("DNNF", new CompileDnnfExperiment()),
                //new Pair<>("BDD", new CompileBddExperiment()),
                new Pair<>("SDD", new CompileSddExperiment())
        )).runExperiments(inputs, handler);
        return results;
    }
}
