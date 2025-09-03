package com.booleworks.logicng_sdd_bench.experiments.compilation;

import com.booleworks.logicng.formulas.FType;
import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Variable;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.knowledgecompilation.bdds.Bdd;
import com.booleworks.logicng.knowledgecompilation.bdds.BddFactory;
import com.booleworks.logicng.knowledgecompilation.bdds.jbuddy.BddKernel;
import com.booleworks.logicng.knowledgecompilation.bdds.orderings.ForceOrdering;
import com.booleworks.logicng.predicates.satisfiability.SatPredicate;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.Experiment;
import com.booleworks.logicng_sdd_bench.trackers.CompilationTracker;

import java.util.List;
import java.util.function.Supplier;

public class CompileBddExperiment implements Experiment<Formula, CompilationTracker> {
    @Override
    public CompilationTracker execute(final Formula input, final FormulaFactory f, final Logger logger,
                                      final Supplier<ComputationHandler> handler) {
        final var tracker = new CompilationTracker(handler.get());
        final Formula cnf = input.cnf(f);
        tracker.setFormulaVariableCount(input.variables(f).size());
        tracker.start();
        final LngResult<Formula> simplified = Util.optimizeFormulaForCompilation(f, cnf, tracker);
        if (!simplified.isSuccess()) {
            return tracker;
        }
        final Formula simplifiedFormula = simplified.getResult();
        if (simplifiedFormula.getType() == FType.TRUE) {
            return tracker;
        }
        if (!simplifiedFormula.holds(new SatPredicate(f))) {
            return tracker;
        }
        final int varNum = simplifiedFormula.variables(f).size();
        final List<Variable> order = new ForceOrdering().getOrder(f, simplifiedFormula);
        final BddKernel kernel = new BddKernel(f, order, varNum * 30, varNum * 20);
        final LngResult<Bdd> bdd = BddFactory.build(f, simplifiedFormula, kernel, tracker);

        if (bdd.isSuccess()) {
            tracker.done();
            tracker.setNodeSize(bdd.getResult().nodeCount());
        }
        return tracker;
    }
}
