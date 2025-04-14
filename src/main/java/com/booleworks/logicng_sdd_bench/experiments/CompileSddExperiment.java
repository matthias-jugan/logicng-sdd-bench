package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerTopDown;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddCompilationResult;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddFactory;
import com.booleworks.logicng_sdd_bench.experiments.results.TimingResult;

import java.util.List;
import java.util.function.Supplier;

public class CompileSddExperiment extends Experiment<TimingResult> {

    @Override
    public TimingResult execute(final Formula input, final FormulaFactory f,
                                final Supplier<ComputationHandler> handler) {
        final SddFactory sf = new SddFactory(f);
        final long startTime = System.currentTimeMillis();
        final LngResult<SddCompilationResult> result = SddCompilerTopDown.compile(input, sf, handler.get());
        final long endTime = System.currentTimeMillis();
        if (result.isSuccess()) {
            return new TimingResult(endTime - startTime);
        } else {
            System.out.println("Timeout");
            return TimingResult.invalid();
        }
    }

    @Override
    public List<String> getLabels() {
        return TimingResult.getLabels();
    }
}
