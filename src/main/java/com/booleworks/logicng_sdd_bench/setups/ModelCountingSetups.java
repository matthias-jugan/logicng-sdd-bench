package com.booleworks.logicng_sdd_bench.setups;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.ValidationFunction;
import com.booleworks.logicng_sdd_bench.experiments.CountModelsDnnfExperiment;
import com.booleworks.logicng_sdd_bench.experiments.CountModelsSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentEntry;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentGroup;
import com.booleworks.logicng_sdd_bench.experiments.PmcNaiveSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProblemFunction;
import com.booleworks.logicng_sdd_bench.experiments.results.ModelCountingResult;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Supplier;

public class ModelCountingSetups {
    public static final ValidationFunction<ModelCountingResult> COMPARE_MC = (vs) -> {
        if (vs.isEmpty()) {
            return null;
        }
        BigInteger ref = null;
        for (final var v : vs) {
            if (v == null || v.getSecond() == null) {
                continue;
            }
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
    };

    public static void modelCounting(
            final List<InputFile> inputs, final List<String> arguments, final Logger logger,
            final Supplier<ComputationHandler> handler) {
        final var results = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("DNNF Count", new CountModelsDnnfExperiment(), ProblemFunction::id),
                new ExperimentEntry<>("SDD Count", new CountModelsSddExperiment(), ProblemFunction::id)
        )).runExperiments(inputs, logger, handler, COMPARE_MC);
    }

    public static void projectedModelCounting(final List<InputFile> inputs, final List<String> arguments,
                                              final Logger logger,
                                              final Supplier<ComputationHandler> handler) {
        final var results = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("SDD Proj Comp", new PmcNaiveSddExperiment(),
                        ProjectionSetups.exportedPdfToProjectionProblem)
        )).runExperiments(inputs, logger, handler, COMPARE_MC);
    }
}
