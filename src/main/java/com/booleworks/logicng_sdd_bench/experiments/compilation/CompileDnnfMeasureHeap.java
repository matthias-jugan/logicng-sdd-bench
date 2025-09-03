package com.booleworks.logicng_sdd_bench.experiments.compilation;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.handlers.events.SimpleEvent;
import com.booleworks.logicng.knowledgecompilation.dnnf.DnnfCompiler;
import com.booleworks.logicng.knowledgecompilation.dnnf.datastructures.Dnnf;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.experiments.Experiment;
import com.booleworks.logicng_sdd_bench.experiments.results.SizeResult;
import com.booleworks.logicng_sdd_bench.trackers.HeapSizeTracker;
import com.booleworks.logicng_sdd_bench.trackers.TrackerGroup;

import java.util.List;
import java.util.function.Supplier;

public class CompileDnnfMeasureHeap implements Experiment<Formula, SizeResult> {

    @Override
    public SizeResult execute(final Formula input, final FormulaFactory f, final Logger logger,
                              final Supplier<ComputationHandler> handler) {
        final var sizeTracker = new HeapSizeTracker(SimpleEvent.DNNF_SHANNON_EXPANSION, 0);
        final var h = new TrackerGroup(List.of(sizeTracker), handler.get());
        final LngResult<Dnnf> dnnf = DnnfCompiler.compile(f, input, h);
        return new SizeResult(sizeTracker.getMaxSize());
    }
}
