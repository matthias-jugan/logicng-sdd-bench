package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.FormulaFactoryConfig;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.TimeoutHandler;
import com.booleworks.logicng.util.Pair;
import com.booleworks.logicng_sdd_bench.ConsoleColors;
import com.booleworks.logicng_sdd_bench.Input;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Reader;
import com.booleworks.logicng_sdd_bench.ValidationFunction;
import com.booleworks.logicng_sdd_bench.experiments.results.ExperimentResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ExperimentGroup<E extends ExperimentResult> {
    public static InputFile DIRTY_HACK_CURRENT_FILE = null;
    final static FormulaFactoryConfig CONFIG = FormulaFactoryConfig.builder().name("BENCH").build();
    List<ExperimentEntry<?, ? extends E>> experiments;

    public ExperimentGroup(final List<ExperimentEntry<?, ? extends E>> experiments) {
        this.experiments = experiments;
    }

    public final LinkedHashMap<InputFile, MergeResult<E>> runExperiments(final List<InputFile> inputFiles,
                                                                         final Logger logger,
                                                                         final Supplier<ComputationHandler> handler) {
        return runExperiments(inputFiles, logger, handler, ValidationFunction.valid());
    }

    public final LinkedHashMap<InputFile, MergeResult<E>> runExperiments(final List<InputFile> inputFiles,
                                                                         final Logger logger,
                                                                         final Supplier<ComputationHandler> handler,
                                                                         final ValidationFunction<E> validationFunction) {
        final LinkedHashMap<InputFile, MergeResult<E>> results = new LinkedHashMap<>();
        for (final InputFile inputFile : inputFiles) {
            DIRTY_HACK_CURRENT_FILE = inputFile;
            final List<Pair<String, E>> innerResults = new ArrayList<>();
            if (inputFile == inputFiles.getFirst()) {
                logger.event("Warmup");
                for (final var experiment : experiments) {
                    try {
                        Runtime.getRuntime().gc();
                        final FormulaFactory f = FormulaFactory.caching(CONFIG);
                        final Input input = Reader.load(inputFile, f);
                        experiment.run(input, f, logger, () -> new TimeoutHandler(5_000));
                        Runtime.getRuntime().gc();
                    } catch (final Exception ignored) {
                    }
                }
            }
            for (final var experiment : experiments) {
                logger.event("Run " + experiment.name() + " for " + inputFile.file());
                try {
                    Runtime.getRuntime().gc();
                    final FormulaFactory f = FormulaFactory.caching(CONFIG);
                    final Input input = Reader.load(inputFile, f);
                    final var result = experiment.run(input, f, logger, handler);
                    if (result == null) {
                        logger.event("Skip " + experiment.name() + " for " + inputFile.file()
                                + " because no problem could be generated");
                        innerResults.add(new Pair<>(experiment.name(), null));
                    } else {
                        innerResults.add(new Pair<>(experiment.name(), result));
                    }
                    Runtime.getRuntime().gc();
                } catch (final Exception exception) {
                    logger.error("Skipped " + experiment.name() + " with input " + inputFile.name()
                            + " because of an exception:");
                    logger.error(exception.toString());
                    innerResults.add(new Pair<>(experiment.name(), null));
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
                    .map(r ->
                            r.getFirst() + ": (" + String.join(", ",
                                    (r.getSecond() == null ? "null" : r.getSecond().getResult()) + ")"))
                    .toList();
        }

        @Override
        public String getEssentialsAsCsv() {
            return results
                    .stream()
                    .filter(p -> p.getSecond() != null)
                    .map(p -> p.getFirst() + "," + p.getSecond().getEssentialsAsCsv())
                    .collect(Collectors.joining(","));
        }
    }
}
