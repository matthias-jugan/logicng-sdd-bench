package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.knowledgecompilation.bdds.Bdd;
import com.booleworks.logicng.knowledgecompilation.bdds.BddFactory;
import com.booleworks.logicng.knowledgecompilation.bdds.jbuddy.BddKernel;
import com.booleworks.logicng_sdd_bench.experiments.results.TimingResult;

import java.util.List;
import java.util.function.Supplier;

public class CompileBddExperiment extends Experiment<TimingResult> {
    @Override
    public TimingResult execute(final Formula input, final FormulaFactory f,
                                final Supplier<ComputationHandler> handler) {
        final long startTime = System.currentTimeMillis();
        final Formula formula = input.nnf(f);
        final int varNum = formula.variables(f).size();
        final BddKernel kernel = new BddKernel(f, varNum, varNum * 30, varNum * 20);
        final LngResult<Bdd> bdd = BddFactory.build(f, formula, kernel, handler.get());
        final long endTime = System.currentTimeMillis();

        if (bdd.isSuccess()) {
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
