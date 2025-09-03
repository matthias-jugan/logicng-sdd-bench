package com.booleworks.logicng_sdd_bench.experiments.compilation;


import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.io.parsers.ParserException;
import com.booleworks.logicng.io.readers.SddReader;
import com.booleworks.logicng.knowledgecompilation.sdd.algorithms.SddSize;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompiler;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerConfig;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.Sdd;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddCompilationResult;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.experiments.Experiment;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentGroup;
import com.booleworks.logicng_sdd_bench.trackers.CompilationTracker;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

public class RecompileExperiment implements Experiment<Formula, CompilationTracker> {
    final File path;

    public RecompileExperiment(final File path) {
        this.path = path;
    }

    @Override
    public CompilationTracker execute(final Formula input, final FormulaFactory f, final Logger logger,
                                      final Supplier<ComputationHandler> handler) {
        final var file = Arrays.stream(Objects.requireNonNull(path.listFiles()))
                .filter(p -> p.getName().startsWith(ExperimentGroup.DIRTY_HACK_CURRENT_FILE.name()))
                .findFirst()
                .get();
        logger.event(String.format("Recompile with %s", file));
        final CompilationTracker tracker = new CompilationTracker(handler.get());
        try {
            final Sdd sdd = SddReader.readVTree(file, f);
            final SddCompilerConfig config = SddCompilerConfig.builder()
                    .compiler(SddCompilerConfig.Compiler.BOTTOM_UP)
                    .sdd(sdd)
                    .build();
            final LngResult<SddCompilationResult> result = SddCompiler.compile(input.cnf(f), config, f, tracker);
            if (result.isSuccess()) {
                tracker.done();
                tracker.setNodeSize(SddSize.size(result.getResult().getNode()));
                tracker.setSddSize(result.getResult().getSdd().getSddNodeCount());
            }
        } catch (final ParserException | IOException e) {
            e.printStackTrace();
        }
        return tracker;
    }
}
