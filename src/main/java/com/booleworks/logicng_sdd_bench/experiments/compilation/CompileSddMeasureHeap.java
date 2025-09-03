package com.booleworks.logicng_sdd_bench.experiments.compilation;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.events.SimpleEvent;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompiler;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerConfig;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.Experiment;
import com.booleworks.logicng_sdd_bench.experiments.results.SizeResult;
import com.booleworks.logicng_sdd_bench.trackers.HeapSizeTracker;
import com.booleworks.logicng_sdd_bench.trackers.TrackerGroup;

import java.util.List;
import java.util.function.Supplier;

public class CompileSddMeasureHeap implements Experiment<Formula, SizeResult> {
    final Supplier<SddCompilerConfig.Builder> config;

    public CompileSddMeasureHeap(final Supplier<SddCompilerConfig.Builder> config) {
        this.config = config;
    }

    public CompileSddMeasureHeap() {
        this(Util.DEFAULT_COMPILER_CONFIG);
    }

    @Override
    public SizeResult execute(final Formula input, final FormulaFactory f, final Logger logger,
                              final Supplier<ComputationHandler> handler) {
        final var sizeTracker = new HeapSizeTracker(SimpleEvent.SDD_SHANNON_EXPANSION, 0);
        final var h = new TrackerGroup(List.of(sizeTracker), handler.get());
        final var c = config.get().build();
        final var result = SddCompiler.compile(input.cnf(f), c, f, h);
        return new SizeResult(sizeTracker.getMaxSize());
    }
}
