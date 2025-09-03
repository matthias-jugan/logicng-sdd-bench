package com.booleworks.logicng_sdd_bench.experiments.enumeration;

import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Variable;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompiler;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerConfig;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.Sdd;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddNode;
import com.booleworks.logicng.knowledgecompilation.sdd.functions.SddModelEnumerationFunction;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.Experiment;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;
import com.booleworks.logicng_sdd_bench.experiments.results.ModelEnumerationResult;
import com.booleworks.logicng_sdd_bench.trackers.HandlerGroup;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class EnumerationSddExperiment implements Experiment<ProjectionProblem, ModelEnumerationResult> {
    final Supplier<SddCompilerConfig.Builder> config;
    final int limit;
    boolean withAddVars = false;
    boolean encodePure = true;

    public EnumerationSddExperiment(final int limit, final Supplier<SddCompilerConfig.Builder> config) {
        this.config = config;
        this.limit = limit;
    }

    public EnumerationSddExperiment(final int limit) {
        this(limit, Util.DEFAULT_COMPILER_CONFIG);
    }

    public EnumerationSddExperiment(final int limit, final boolean withAddVars) {
        this(limit, Util.DEFAULT_COMPILER_CONFIG);
        this.withAddVars = withAddVars;
    }

    public EnumerationSddExperiment withPureEncoding(final boolean pureEncoding) {
        this.encodePure = pureEncoding;
        return this;
    }

    @Override
    public ModelEnumerationResult execute(final ProjectionProblem input, final FormulaFactory f, final Logger logger,
                                          final Supplier<ComputationHandler> handler) {
        final int numberVars = input.projectedVariables().size();
        final Set<Variable> additionalVars = withAddVars ? input.quantifiedVariables() : Set.of();
        final var h = handler.get();
        final var countTracker = new ModelEnumerationResult(true, limit, numberVars, additionalVars.size());
        final var handlers = new HandlerGroup(List.of(countTracker, h));
        final var cnf = encodePure ? Util.encodeAsPureCnf(f, input.formula()) : input.formula().cnf(f);
        final var c = config.get().build();
        final var compiled = SddCompiler.compile(cnf, c, f, h);
        if (!compiled.isSuccess()) {
            return countTracker;
        }
        countTracker.addCount(0);
        final Sdd sdd = compiled.getResult().getSdd();
        final SddNode node = compiled.getResult().getNode();

        var meFuncB = SddModelEnumerationFunction.builder(input.projectedVariables(), sdd);
        if (withAddVars) {
            meFuncB = meFuncB.additionalVariables(additionalVars);
        }
        final var meFunc = meFuncB.build();

        final var models = node.execute(meFunc, handlers);
        if (models.isSuccess() || models.isPartial()) {
            countTracker.addCount(models.getPartialResult().size());
        }
        return countTracker;
    }
}
