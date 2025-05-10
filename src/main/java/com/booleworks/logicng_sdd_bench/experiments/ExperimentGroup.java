package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.util.Pair;
import com.booleworks.logicng_sdd_bench.ConsoleColors;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Reader;
import com.booleworks.logicng_sdd_bench.ValidationFunction;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProblemFunction;
import com.booleworks.logicng_sdd_bench.experiments.results.ExperimentResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ExperimentGroup<I, E extends ExperimentResult> {
    List<Pair<String, Experiment<I, ? extends E>>> experiments;


    public ExperimentGroup(final List<Pair<String, Experiment<I, ? extends E>>> experiments) {
        this.experiments = experiments;
    }

    public final LinkedHashMap<InputFile, MergeResult<E>> runExperiments(final List<InputFile> inputFiles,
                                                                         final Logger logger,
                                                                         final ProblemFunction<I> problemFunction,
                                                                         final Supplier<ComputationHandler> handler) {
        return runExperiments(inputFiles, logger, problemFunction, handler, ValidationFunction.valid());
    }

    public final LinkedHashMap<InputFile, MergeResult<E>> runExperiments(final List<InputFile> inputFiles,
                                                                         final Logger logger,
                                                                         final ProblemFunction<I> problemFunction,
                                                                         final Supplier<ComputationHandler> handler,
                                                                         final ValidationFunction<E> validationFunction) {
        final LinkedHashMap<InputFile, MergeResult<E>> results = new LinkedHashMap<>();
        for (final InputFile inputFile : inputFiles) {
            final List<Pair<String, E>> innerResults = new ArrayList<>();
            for (final var experiment : experiments) {
                logger.event("Run " + experiment.getFirst() + " for " + inputFile.file());
                try {
                    Runtime.getRuntime().gc();
                    final FormulaFactory f = FormulaFactory.caching();
                    final Formula formula = Reader.load(inputFile, f);
                    final I problem = problemFunction.generate(formula, f);
                    final E result = experiment.getSecond().execute(problem, f, logger, handler);
                    innerResults.add(new Pair<>(experiment.getFirst(), result));
                    Runtime.getRuntime().gc();
                } catch (final Exception exception) {
                    logger.error("Skipped " + experiment.getFirst() + " with input " + inputFile.name()
                            + " because of an exception:");
                    logger.error(exception.toString());
                    innerResults.add(new Pair<>(experiment.getFirst(), null));
                }
            }
            final String validation = validationFunction.validate(innerResults);
            if (validation != null) {
                logger.error(ConsoleColors.RED + inputFile.name() + " produced an inconsistent result:\n"
                        + ConsoleColors.RESET + validation);
            }
            final var result = new MergeResult<>(innerResults);
            logger.result(inputFile, result);
            results.put(inputFile, result);
        }
        return results;
    }

    public record MergeResult<E extends ExperimentResult>(List<Pair<String, E>> results) implements ExperimentResult {
        @Override
        public List<String> getResult() {
            return results
                    .stream()
                    .map(r -> r.getFirst() + ": (" + String.join(", ", r.getSecond().getResult() + ")"))
                    .toList();
        }

        @Override
        public String getEssentialsAsCsv() {
            return results.stream().map(p -> p.getSecond().getEssentialsAsCsv()).collect(Collectors.joining(","));
        }
    }

}
