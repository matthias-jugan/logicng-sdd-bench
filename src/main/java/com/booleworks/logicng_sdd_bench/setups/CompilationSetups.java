package com.booleworks.logicng_sdd_bench.setups;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.util.Pair;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Settings;
import com.booleworks.logicng_sdd_bench.experiments.CompileBddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.CompileDnnfExperiment;
import com.booleworks.logicng_sdd_bench.experiments.CompileSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentGroup;
import com.booleworks.logicng_sdd_bench.experiments.SddCompProgressExperiment;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProblemFunction;
import com.booleworks.logicng_sdd_bench.experiments.results.ExperimentResult;
import com.booleworks.logicng_sdd_bench.trackers.CompilationTimeTracker;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

public class CompilationSetups {
    public static void compileS(final List<InputFile> inputs, final Settings settings, final Logger logger,
                                final Supplier<ComputationHandler> handler) {
        final var results = new ExperimentGroup<>(List.of(
                new Pair<>("SDD", new CompileSddExperiment())
        )).runExperiments(inputs, logger, ProblemFunction::id, handler);
    }

    public static void compileSd(final List<InputFile> inputs, final Settings settings, final Logger logger,
                                 final Supplier<ComputationHandler> handler) {
        final var results = new ExperimentGroup<>(List.of(
                new Pair<>("SDD", new CompileSddExperiment()),
                new Pair<>("DNNF", new CompileDnnfExperiment())
        )).runExperiments(inputs, logger, ProblemFunction::id, handler);
    }

    public static void compileSdb(final List<InputFile> inputs, final Settings settings, final Logger logger,
                                  final Supplier<ComputationHandler> handler) {
        final var results = new ExperimentGroup<>(List.of(
                new Pair<>("SDD", new CompileSddExperiment()),
                new Pair<>("DNNF", new CompileDnnfExperiment()),
                new Pair<>("BDD", new CompileBddExperiment())
        )).runExperiments(inputs, logger, ProblemFunction::id, handler);
    }

    public static void compilationProgress(final List<InputFile> inputs, final Settings settings, final Logger logger,
                                           final Supplier<ComputationHandler> handler) {
        final LinkedHashMap<InputFile, ExperimentGroup.MergeResult<ExperimentResult>> results
                = new ExperimentGroup<>(List.of(
                new Pair<>("SDD", new SddCompProgressExperiment()),
                new Pair<>("DNNF", new CompileDnnfExperiment())
        )).runExperiments(inputs, logger, ProblemFunction::id, handler);

        int total = 0;
        int compiled = 0;
        int compiledButNotDnnf = 0;
        int notCompiledButDnnf = 0;
        long sddTime = 0;
        long sddTimeWOTimeout = 0;
        long dnnfTime = 0;
        long dnnfTimeWOTimeout = 0;
        long expansion = 0;
        for (final var r : results.entrySet()) {
            final var f = r.getKey();
            final var res = r.getValue();
            final var sddRes = res.results().get(0).getSecond();
            final var dnnfRes = res.results().get(1).getSecond();
            if (sddRes == null || dnnfRes == null) {
                logger.error("Missing value for " + f.name()
                        + ". Will ignore it. Consider this when analysing the results");
                continue;
            }
            total++;
            final SddCompProgressExperiment.Result sr = (SddCompProgressExperiment.Result) sddRes;
            final CompilationTimeTracker dr = (CompilationTimeTracker) dnnfRes;

            expansion += sr.expansions();
            if (sr.time() != -1) {
                compiled++;
                if (dr.getGlobal() == -1) {
                    compiledButNotDnnf++;
                } else {
                    sddTimeWOTimeout += sr.time();
                    dnnfTimeWOTimeout += dr.getGlobal();
                }
                sddTime += sr.time();
            } else {
                if (dr.getGlobal() != -1) {
                    notCompiledButDnnf++;
                }
                sddTime += settings.timeout();
            }
            if (dr.getGlobal() == -1) {
                dnnfTime += settings.timeout();
            } else {
                dnnfTime += dr.getGlobal();
            }
        }

        logger.summary("=== Compilation Progress Summary ===");
        logger.summary("");
        logger.summary(String.format("Input:\t\t\t %d Files", total));
        logger.summary(String.format("Compiled:\t\t %d Files (%.2f%%)", compiled, ((double) compiled / total) * 100));
        logger.summary(String.format("Only compiled sdd:\t %d Files (%.2f%%)", compiledButNotDnnf,
                ((double) compiledButNotDnnf / total) * 100));
        logger.summary(String.format("Only compiled dnnf:\t %d Files (%.2f%%)", notCompiledButDnnf,
                ((double) notCompiledButDnnf / total) * 100));
        if (total > 0) {
            logger.summary("Times:");
            final long avgSddTime = sddTime / total;
            final long avgDnnfTime = dnnfTime / total;
            logger.summary(String.format("Sdd: %dms (%dms)", sddTime, avgSddTime));
            logger.summary(String.format("Dnnf: %dms (%dms)", dnnfTime, avgDnnfTime));
            logger.summary(String.format("Sdd/Dnnf: %.2f", ((double) sddTime) / dnnfTime));
        }
        if (total - compiledButNotDnnf > 0) {
            final long avgSddTimeWOTimeout = sddTimeWOTimeout / (total - compiledButNotDnnf);
            final long avgDnnfTimeWOTimeout = dnnfTimeWOTimeout / (total - compiledButNotDnnf);
            logger.summary(
                    String.format("Sdd (without timeouts): %dms (%dms)", sddTimeWOTimeout, avgSddTimeWOTimeout));
            logger.summary(
                    String.format("Dnnf (without timeouts): %dms (%dms)", dnnfTimeWOTimeout, avgDnnfTimeWOTimeout));
            logger.summary(
                    String.format("Sdd/Dnnf (without timeouts): %.2f",
                            ((double) sddTimeWOTimeout) / dnnfTimeWOTimeout));
        }
        logger.summary(String.format("Shannon Expansions: %d", expansion));
        logger.summary(
                String.format("Shannon Expansions: %.2f per sec", ((double) expansion) / ((double) sddTime / 1000)));
        logger.summary("======");
    }
}
