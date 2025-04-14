package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.knowledgecompilation.dnnf.DnnfCompiler;
import com.booleworks.logicng.knowledgecompilation.dnnf.datastructures.Dnnf;
import com.booleworks.logicng_sdd_bench.experiments.results.TimingResult;

import java.util.List;
import java.util.function.Supplier;

public class CompileDnnfExperiment extends Experiment<TimingResult> {

    @Override
    public TimingResult execute(final Formula input, final FormulaFactory f,
                                final Supplier<ComputationHandler> handler) {
        final long startTime = System.currentTimeMillis();
        final LngResult<Dnnf> dnnf = DnnfCompiler.compile(f, input, handler.get());
        final long endTime = System.currentTimeMillis();
        if (dnnf.isSuccess()) {
            return new TimingResult(endTime - startTime);
        } else {
            System.err.println("Timeout");
            return TimingResult.invalid();
        }
    }

    @Override
    public List<String> getLabels() {
        return TimingResult.getLabels();
    }
}
