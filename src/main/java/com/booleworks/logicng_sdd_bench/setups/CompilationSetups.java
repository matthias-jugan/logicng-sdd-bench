package com.booleworks.logicng_sdd_bench.setups;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.Variable;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.util.Pair;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Settings;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.CompileBddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.CompileDnnfExperiment;
import com.booleworks.logicng_sdd_bench.experiments.CompileSddBUExperiment;
import com.booleworks.logicng_sdd_bench.experiments.CompileSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentGroup;
import com.booleworks.logicng_sdd_bench.experiments.PcSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.SddCompProgressExperiment;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProblemFunction;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;
import com.booleworks.logicng_sdd_bench.experiments.results.ExperimentResult;
import com.booleworks.logicng_sdd_bench.trackers.CompilationTimeTracker;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
                new Pair<>("SDD (TD)", new CompileSddExperiment()),
                new Pair<>("SDD (BU)", new CompileSddBUExperiment()),
                new Pair<>("DNNF", new CompileDnnfExperiment()),
                new Pair<>("BDD (BU)", new CompileBddExperiment())
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

    public static void projectedCompile(final List<InputFile> inputs, final Settings settings, final Logger logger,
                                        final Supplier<ComputationHandler> handler) {
        //        final var results0 = new ExperimentGroup<>(List.of(
        //                new Pair<>("SDD Naive Count", new PmcNaiveSddExperiment()),
        //                new Pair<>("SDD Proj Comp", new PmcPcSddExperiment())
        //        )).runExperiments(inputs, logger, ProjectionProblem.quantifyRandom(0, 1), handler, COMPARE_MC);

        final var results = new ExperimentGroup<>(List.of(
                new Pair<>("SDD Comp", new CompileSddExperiment())
        )).runExperiments(inputs, logger, ProblemFunction::id, handler);
        final var results20 = new ExperimentGroup<>(List.of(
                new Pair<>("SDD Proj Comp (20%)", new PcSddExperiment())
        )).runExperiments(inputs, logger, ProjectionProblem.quantifyRandom(0.2, 1), handler);
        final var results80 = new ExperimentGroup<>(List.of(
                new Pair<>("SDD Proj Comp (80%)", new PcSddExperiment())
        )).runExperiments(inputs, logger, ProjectionProblem.quantifyRandom(0.8, 1), handler);

        int compiled1 = 0;
        int compiled2 = 0;
        int compiled3 = 0;
        int compiledAll = 0;
        long time1 = 0;
        long time2 = 0;
        long time3 = 0;
        for (final var file : results.keySet()) {
            final var r1 = results.get(file).results().getFirst().getSecond();
            final var r2 = results20.get(file).results().getFirst().getSecond();
            final var r3 = results80.get(file).results().getFirst().getSecond();
            if (r1.getGlobal() != -1) {
                compiled1 += 1;
            }
            if (r2.getGlobal() != -1) {
                compiled2 += 1;
            }
            if (r3.getGlobal() != -1) {
                compiled3 += 1;
            }
            if (r1.getGlobal() != -1 && r2.getGlobal() != -1 && r3.getGlobal() != -1) {
                compiledAll += 1;
                time1 += r1.getGlobal();
                time2 += r2.getGlobal();
                time3 += r3.getGlobal();
            }
        }

        logger.summary(String.format("Compilation: %d Files", compiled1));
        logger.summary(String.format("Proj. Comp. (20%%): %d Files", compiled2));
        logger.summary(String.format("Proj. Comp. (80%%): %d Files", compiled3));
        logger.summary(String.format("All: %d Files", compiledAll));
        if (compiledAll > 0) {
            logger.summary("Times:");
            final double avgTime1 = ((double) time1) / compiledAll;
            final double avgTime2 = ((double) time2) / compiledAll;
            final double avgTime3 = ((double) time3) / compiledAll;
            logger.summary(String.format("Compilation: %.2fms/p", avgTime1));
            logger.summary(String.format("Proj. Comp. (20%%): %.2fms/p", avgTime2));
            logger.summary(String.format("Proj. Comp. (80%%): %.2fms/p", avgTime3));
        }
    }

    public static void compileCNF(final List<InputFile> inputs, final Settings settings, final Logger logger,
                                  final Supplier<ComputationHandler> handler) {
        final ProblemFunction<ProjectionProblem> pureEncoding = (formula, f) -> {
            final Formula cnf = Util.encodeAsPureCnf(f, formula);
            return new ProjectionProblem(cnf, Set.of(), cnf.variables(f));
        };
        final ProblemFunction<Formula> pureEncodingFormula = (formula, f) -> {
            return Util.encodeAsPureCnf(f, formula);
        };
        final ProblemFunction<ProjectionProblem> auxEncoding = (formula, f) -> {
            final Formula cnf = formula.cnf(f);
            final Set<Variable> originalVars = formula.variables(f);
            final Set<Variable> auxVars = new TreeSet<>();
            for (final Variable v : cnf.variables(f)) {
                if (!originalVars.contains(v)) {
                    auxVars.add(v);
                }
            }
            return new ProjectionProblem(cnf, auxVars, originalVars);
        };
        //        final var resultsPure = new ExperimentGroup<>(List.of(
        //                new Pair<>("CNF (Pure Encoding)", new PcSddExperiment())
        //        )).runExperiments(inputs, logger, pureEncoding, handler);
        //        final var resultsAux = new ExperimentGroup<>(List.of(
        //                new Pair<>("CNF (with Aux)", new PcSddExperiment())
        //        )).runExperiments(inputs, logger, auxEncoding, handler);
        final var resultsDnnf = new ExperimentGroup<>(List.of(
                new Pair<>("CNF (DNNF Pure)", new CompileDnnfExperiment())
        )).runExperiments(inputs, logger, pureEncodingFormula, handler);

        //        int completedPure = 0;
        //        int completedAux = 0;
        //        long timePure = 0;
        //        long timeAux = 0;
        //        final ArrayList<Long> diffs = new ArrayList<>();
        //        for (final var input : inputs) {
        //            final var pr = resultsPure.get(input).results().getFirst().getSecond();
        //            final var ar = resultsAux.get(input).results().getFirst().getSecond();
        //            if (pr.getGlobal() != -1 && ar.getGlobal() != -1) {
        //                timePure += pr.getGlobal();
        //                timeAux += ar.getGlobal();
        //                diffs.add(pr.getGlobal() - ar.getGlobal());
        //            }
        //            if (pr.getGlobal() != -1) {
        //                completedPure += 1;
        //            }
        //            if (ar.getGlobal() != -1) {
        //                completedAux += 1;
        //            }
        //        }
        //        logger.summary(String.format("Completed pure: %d", completedPure));
        //        logger.summary(String.format("Completed with aux: %d", completedAux));
        //        if (completedAux > 0 && completedPure > 0) {
        //            logger.summary(String.format("Avg time pure: %.2f", ((double) timePure) / completedPure));
        //            logger.summary(String.format("Avg time with aux: %.2f", ((double) timeAux) / completedAux));
        //            logger.summary(String.format("Diffs: %s", diffs));
        //        }
    }
}
