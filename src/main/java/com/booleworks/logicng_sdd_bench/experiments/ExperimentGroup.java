package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.util.Pair;
import com.booleworks.logicng_sdd_bench.ConsoleColors;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Reader;
import com.booleworks.logicng_sdd_bench.ValidationFunction;
import com.booleworks.logicng_sdd_bench.experiments.results.ExperimentResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class ExperimentGroup<E extends ExperimentResult> {
    List<Pair<String, Experiment<? extends E>>> experiments;


    public ExperimentGroup(final List<Pair<String, Experiment<? extends E>>> experiments) {
        this.experiments = experiments;
    }

    public final HashMap<InputFile, MergeResult<E>> runExperiments(final List<InputFile> inputFiles,
                                                                   final Supplier<ComputationHandler> handler) {
        return runExperiments(inputFiles, handler, ValidationFunction.valid());
    }

    public final HashMap<InputFile, MergeResult<E>> runExperiments(final List<InputFile> inputFiles,
                                                                   final Supplier<ComputationHandler> handler,
                                                                   final ValidationFunction<E> validationFunction) {
        final HashMap<InputFile, MergeResult<E>> results = new HashMap<>();
        for (final InputFile inputFile : inputFiles) {
            final List<Pair<String, E>> innerResults = new ArrayList<>();
            for (final var experiment : experiments) {
                System.out.println("Run " + experiment.getFirst() + " for " + inputFile.file());
                try {
                    Runtime.getRuntime().gc();
                    final FormulaFactory f = FormulaFactory.caching();
                    final Formula formula = Reader.load(inputFile, f);
                    final E result = experiment.getSecond().execute(formula, f, handler);
                    innerResults.add(new Pair<>(experiment.getFirst(), result));
                    Runtime.getRuntime().gc();
                } catch (final Exception exception) {
                    System.err.println(
                            "Skipped " + experiment.getFirst() + " with input " + inputFile.name()
                                    + " because of an exception:");
                    exception.printStackTrace();
                    innerResults.add(new Pair<>(experiment.getFirst(), null));
                }
            }
            final String validation = validationFunction.validate(innerResults);
            if (validation != null) {
                System.err.println(ConsoleColors.RED + inputFile.name() + " produced an inconsistent result:\n"
                        + ConsoleColors.RESET + validation);
            }
            results.put(inputFile, new MergeResult<>(innerResults));
        }
        return results;
    }

    public record MergeResult<E extends ExperimentResult>(List<Pair<String, E>> results) implements ExperimentResult {

        @Override
        public List<String> getResult() {
            return results
                    .stream()
                    .flatMap(r -> r.getSecond().getResult().stream())
                    .toList();
        }
    }

}
