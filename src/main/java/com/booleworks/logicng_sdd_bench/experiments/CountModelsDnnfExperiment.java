package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.knowledgecompilation.dnnf.DnnfCompiler;
import com.booleworks.logicng.knowledgecompilation.dnnf.datastructures.Dnnf;
import com.booleworks.logicng.knowledgecompilation.dnnf.functions.DnnfModelCountFunction;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.results.ModelCountingResult;
import com.booleworks.logicng_sdd_bench.experiments.results.TimingResult;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Supplier;

public class CountModelsDnnfExperiment extends Experiment<ModelCountingResult> {
    @Override
    public ModelCountingResult execute(final Formula input, final FormulaFactory f,
                                       final Supplier<ComputationHandler> handler) {
        final Formula cnf = Util.encodeAsPureCnf(f, input);
        final LngResult<Dnnf> dnnf = DnnfCompiler.compile(f, cnf, handler.get());
        if (!dnnf.isSuccess()) {
            System.err.println("Timeout");
            return ModelCountingResult.invalid();
        }
        final long startTime = System.currentTimeMillis();
        final BigInteger count = dnnf.getResult().execute(new DnnfModelCountFunction(f));
        final long endTime = System.currentTimeMillis();
        return new ModelCountingResult(endTime - startTime, count);
    }

    @Override
    public List<String> getLabels() {
        return TimingResult.getLabels();
    }
}
