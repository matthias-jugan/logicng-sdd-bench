package com.booleworks.logicng_sdd_bench.experiments;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerBottomUp;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.Sdd;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddNode;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.vtree.DecisionVTreeGenerator;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.vtree.VTree;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.vtree.VTreeRoot;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.results.TimingResult;
import com.booleworks.logicng_sdd_bench.trackers.CompilationTimeTracker;

import java.util.List;
import java.util.function.Supplier;

public class CompileSddBUExperiment extends Experiment<Formula, CompilationTimeTracker> {

    @Override
    public CompilationTimeTracker execute(final Formula input, final FormulaFactory f, final Logger logger,
                                          final Supplier<ComputationHandler> handler) {
        final CompilationTimeTracker tracker = new CompilationTimeTracker(handler.get());
        final Formula cnf = Util.encodeAsPureCnf(f, input);
        final Sdd sdd = Sdd.independent(f);
        final LngResult<VTree> vTree = new DecisionVTreeGenerator(cnf).generate(sdd, tracker);
        if (!vTree.isSuccess()) {
            return tracker;
        }
        final VTreeRoot root = sdd.constructRoot(vTree.getResult());
        final LngResult<SddNode> result = SddCompilerBottomUp.cnfToSdd(cnf, sdd, tracker);
        if (result.isSuccess()) {
            tracker.done();
        }
        return tracker;
    }

    @Override
    public List<String> getLabels() {
        return TimingResult.getLabels();
    }
}
