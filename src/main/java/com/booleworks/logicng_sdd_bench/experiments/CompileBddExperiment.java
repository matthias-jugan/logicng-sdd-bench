package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Variable;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.knowledgecompilation.bdds.Bdd;
import com.booleworks.logicng.knowledgecompilation.bdds.BddFactory;
import com.booleworks.logicng.knowledgecompilation.bdds.jbuddy.BddKernel;
import com.booleworks.logicng.knowledgecompilation.bdds.orderings.ForceOrdering;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.experiments.results.TimingResult;
import com.booleworks.logicng_sdd_bench.trackers.CompilationTimeTracker;

import java.util.List;
import java.util.function.Supplier;

public class CompileBddExperiment extends Experiment<Formula, CompilationTimeTracker> {
    @Override
    public CompilationTimeTracker execute(final Formula input, final FormulaFactory f, final Logger logger,
                                          final Supplier<ComputationHandler> handler) {
        final CompilationTimeTracker tracker = new CompilationTimeTracker(handler.get());
        tracker.start();
        final Formula formula = input.cnf(f);
        final int varNum = formula.variables(f).size();
        final List<Variable> order = new ForceOrdering().getOrder(f, formula);
        final BddKernel kernel = new BddKernel(f, order, 1_000_000, 1_000_000);
        final LngResult<Bdd> bdd = BddFactory.build(f, formula, kernel, handler.get());

        if (bdd.isSuccess()) {
            tracker.done();
        } else {
            System.err.println("Timeout");
        }
        return tracker;
    }

    @Override
    public List<String> getLabels() {
        return TimingResult.getLabels();
    }
}
