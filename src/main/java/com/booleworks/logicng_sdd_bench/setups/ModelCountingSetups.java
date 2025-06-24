package com.booleworks.logicng_sdd_bench.setups;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.util.Pair;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Settings;
import com.booleworks.logicng_sdd_bench.ValidationFunction;
import com.booleworks.logicng_sdd_bench.experiments.CountModelsDnnfExperiment;
import com.booleworks.logicng_sdd_bench.experiments.CountModelsSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentGroup;
import com.booleworks.logicng_sdd_bench.experiments.PmcPcSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProblemFunction;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;
import com.booleworks.logicng_sdd_bench.experiments.results.ModelCountingResult;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Supplier;

public class ModelCountingSetups {
    private static final ValidationFunction<ModelCountingResult> COMPARE_MC = (vs) -> {
        if (vs.isEmpty()) {
            return null;
        }
        BigInteger ref = null;
        for (final var v : vs) {
            System.out.println(v.getSecond().count());
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
            final List<InputFile> inputs, final Settings settings, final Logger logger,
            final Supplier<ComputationHandler> handler) {
        final var results
                = new ExperimentGroup<>(List.of(
                new Pair<>("DNNF Count", new CountModelsDnnfExperiment()),
                new Pair<>("SDD Count", new CountModelsSddExperiment())
        )).runExperiments(inputs, logger, ProblemFunction::id, handler, COMPARE_MC);

        long total = 0;
        long sddTime = 0;
        long dnnfTime = 0;
        long completed = 0;
        long completedBoth = 0;
        long timeouts = 0;
        for (final var result : results.values()) {
            final ModelCountingResult dnnfR = result.results().get(0).getSecond();
            final ModelCountingResult sddR = result.results().get(1).getSecond();
            total++;
            if (sddR.times().getGlobal() == -1) {
                timeouts += 1;
            } else {
                completed += 1;
                if (dnnfR.times().getGlobal() != -1) {
                    completedBoth++;
                    sddTime += sddR.times().getGlobal();
                    dnnfTime += dnnfR.times().getGlobal();
                }
            }
        }

        logger.summary("=== Model Count Summary ===");
        logger.summary("");
        logger.summary(String.format("Total Problems: %d", total));
        logger.summary(String.format("Completed: %d", completed));
        logger.summary(String.format("Timeouts: %d", timeouts));
        if (completedBoth > 0) {
            final double avgSddTime = ((double) sddTime) / completedBoth;
            final double avgDnnfTime = ((double) dnnfTime) / completedBoth;
            final double factor = avgSddTime / avgDnnfTime;
            logger.summary(String.format("Sdd: %dms (%fms)", sddTime, avgSddTime));
            logger.summary(String.format("Dnnf: %dms (%fms)", dnnfTime, avgDnnfTime));
            logger.summary(String.format("Sdd/Dnnf: %.2f", factor));
        }
    }

    public static void pmc(final List<InputFile> inputs, final Settings settings, final Logger logger,
                           final Supplier<ComputationHandler> handler) {
        //        final var results0 = new ExperimentGroup<>(List.of(
        //                new Pair<>("SDD Naive Count", new PmcNaiveSddExperiment()),
        //                new Pair<>("SDD Proj Comp", new PmcPcSddExperiment())
        //        )).runExperiments(inputs, logger, ProjectionProblem.quantifyRandom(0, 1), handler, COMPARE_MC);

        final var results20 = new ExperimentGroup<>(List.of(
                //new Pair<>("SDD Naive Count", new PmcNaiveSddExperiment()),
                new Pair<>("SDD Proj Comp", new PmcPcSddExperiment())
        )).runExperiments(inputs, logger, ProjectionProblem.quantifyRandom(0.2, 1), handler, COMPARE_MC);

        final var results80 = new ExperimentGroup<>(List.of(
                //new Pair<>("SDD Naive Count", new PmcNaiveSddExperiment()),
                new Pair<>("SDD Proj Comp", new PmcPcSddExperiment())
        )).runExperiments(inputs, logger, ProjectionProblem.quantifyRandom(0.8, 1), handler, COMPARE_MC);
    }
}
