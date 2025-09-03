package com.booleworks.logicng_sdd_bench.setups;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerConfig;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentEntry;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentGroup;
import com.booleworks.logicng_sdd_bench.experiments.compilation.CompileBddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.compilation.CompileDnnfExperiment;
import com.booleworks.logicng_sdd_bench.experiments.compilation.CompileDnnfMeasureHeap;
import com.booleworks.logicng_sdd_bench.experiments.compilation.CompileSddBUExperiment;
import com.booleworks.logicng_sdd_bench.experiments.compilation.CompileSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.compilation.CompileSddMeasureHeap;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProblemFunction;

import java.util.List;
import java.util.function.Supplier;

public class CompilationSetups {
    public static void compileAll(final List<InputFile> inputs, final List<String> arguments, final Logger logger,
                                  final Supplier<ComputationHandler> handler) {
        final var results = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("SDD (TD)", new CompileSddExperiment(), ProblemFunction::id),
                new ExperimentEntry<>("SDD (BU)", new CompileSddBUExperiment(), ProblemFunction::id),
                new ExperimentEntry<>("BDD (BU)", new CompileBddExperiment(), ProblemFunction::id),
                new ExperimentEntry<>("DNNF", new CompileDnnfExperiment(), ProblemFunction::id)
        )).runExperiments(inputs, logger, handler);
    }

    public static void measureHeap(final List<InputFile> inputs, final List<String> arguments, final Logger logger,
                                   final Supplier<ComputationHandler> handler) {
        final var results = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("SDD", new CompileSddMeasureHeap(), ProblemFunction::id),
                new ExperimentEntry<>("DNNF", new CompileDnnfMeasureHeap(), ProblemFunction::id)
        )).runExperiments(inputs, logger, handler);
    }

    public static void compilePreprocessing(final List<InputFile> inputs, final List<String> arguments,
                                            final Logger logger, final Supplier<ComputationHandler> handler) {
        new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("Simplified",
                        new CompileSddExperiment(() -> SddCompilerConfig.builder().preprocessing(true)),
                        ProblemFunction::id),
                new ExperimentEntry<>("Not Simplified",
                        new CompileSddExperiment(() -> SddCompilerConfig.builder().preprocessing(false)),
                        ProblemFunction::id)
        )).runExperiments(inputs, logger, handler);
    }
}
