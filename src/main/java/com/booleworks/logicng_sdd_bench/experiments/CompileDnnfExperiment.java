package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.FType;
import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.knowledgecompilation.dnnf.DnnfCompiler;
import com.booleworks.logicng.knowledgecompilation.dnnf.datastructures.Dnnf;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.trackers.CompilationTracker;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.function.Supplier;

public class CompileDnnfExperiment implements Experiment<Formula, CompilationTracker> {

    @Override
    public CompilationTracker execute(final Formula input, final FormulaFactory f, final Logger logger,
                                      final Supplier<ComputationHandler> handler) {
        final CompilationTracker tracker = new CompilationTracker(handler.get());
        tracker.setFormulaVariableCount(input.variables(f).size());
        final LngResult<Dnnf> dnnf = DnnfCompiler.compile(f, input, tracker);
        if (dnnf.isSuccess()) {
            tracker.done();
            tracker.setNodeSize(dnnfSize(dnnf.getResult().getFormula()));
        }
        return tracker;
    }

    private int dnnfSize(final Formula formula) {
        final Set<Formula> visited = new HashSet<>();
        final Stack<Formula> stack = new Stack<>();
        stack.push(formula);
        int size = 0;
        while (!stack.isEmpty()) {
            final Formula current = stack.pop();
            size += 1;
            if (current.getType() == FType.OR) {
                for (final Formula op : current) {
                    if (!(op.getType() == FType.AND || op.isAtomicFormula())) {
                        throw new IllegalArgumentException("Formula is not in DNNF");
                    }
                    for (final Formula n : op) {
                        if (visited.add(n)) {
                            stack.push(n);
                        }
                    }
                }
            } else {
                if (!(current.getType() == FType.AND || current.isAtomicFormula())) {
                    throw new IllegalArgumentException("Formula is not in DNNF");
                }
                for (final Formula n : current) {
                    if (visited.add(n)) {
                        stack.push(n);
                    }
                }
            }
        }
        return size;
    }
}
