package com.booleworks.logicng_sdd_bench.experiments.other;

import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.knowledgecompilation.sdd.algorithms.SddUtil;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompiler;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerConfig;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.vtree.DecisionVTreeGenerator;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.vtree.VTree;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.experiments.Experiment;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;
import com.booleworks.logicng_sdd_bench.experiments.results.EmptyResult;

import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

public class VTreePropertyExperiment implements Experiment<ProjectionProblem, EmptyResult> {

    @Override
    public EmptyResult execute(final ProjectionProblem input, final FormulaFactory f, final Logger logger,
                               final Supplier<ComputationHandler> handler) {
        System.out.println("None");
        final var config1 = SddCompilerConfig.builder()
                .prioritizationStrategy(DecisionVTreeGenerator.PrioritizationStrategy.NONE)
                .variables(input.projectedVariables())
                .build();
        final var compiledResult = SddCompiler.compile(input.formula().cnf(f), config1, f, handler.get());
        if (!compiledResult.isSuccess()) {
            return new EmptyResult();
        }
        System.out.println("DOWN");
        final var config2 = SddCompilerConfig.builder()
                .prioritizationStrategy(DecisionVTreeGenerator.PrioritizationStrategy.VAR_DOWN)
                .variables(input.projectedVariables())
                .build();
        final var compiledResult2 = SddCompiler.compile(input.formula().cnf(f), config2, f, handler.get());
        if (!compiledResult2.isSuccess()) {
            return new EmptyResult();
        }
        System.out.println("UP");
        final var config3 = SddCompilerConfig.builder()
                .prioritizationStrategy(DecisionVTreeGenerator.PrioritizationStrategy.VAR_UP)
                .variables(input.projectedVariables())
                .build();
        final var compiledResult3 = SddCompiler.compile(input.formula().cnf(f), config3, f, handler.get());
        if (!compiledResult3.isSuccess()) {
            return new EmptyResult();
        }
        final VTree vtree1 = compiledResult.getResult().getSdd().getVTree().getRoot();
        final VTree vtree2 = compiledResult2.getResult().getSdd().getVTree().getRoot();
        final VTree vtree3 = compiledResult3.getResult().getSdd().getVTree().getRoot();
        final Set<Integer> vars = SddUtil.varsToIndicesOnlyKnown(input.projectedVariables(),
                compiledResult.getResult().getSdd(), new TreeSet<>());
        if (vtree1 == null) {
            return new EmptyResult();
        }
        System.out.println("None Nodes: " + elmShannonNodes(vtree1, vars));
        System.out.println("None Score: " + elmShannonScore(vtree1, vars));
        System.out.println("Down Nodes: " + elmShannonNodes(vtree2, vars));
        System.out.println("Down Score: " + elmShannonScore(vtree2, vars));
        System.out.println("Up Nodes: " + elmShannonNodes(vtree3, vars));
        System.out.println("Up Score: " + elmShannonScore(vtree3, vars));
        return new EmptyResult();
    }

    private static int elmShannonNodes(final VTree vtree, final Set<Integer> variables) {
        if (vtree.isLeaf()) {
            return 0;
        } else {
            if (vtree.asInternal().getLeft().isLeaf()) {
                final int childCount = elmShannonNodes(vtree.asInternal().getRight(), variables);
                if (variables.contains(vtree.asInternal().getLeft().asLeaf().getVariable())) {
                    return childCount;
                } else {
                    return childCount + 1;
                }
            } else {
                return elmShannonNodes(vtree.asInternal().getLeft(), variables)
                        + elmShannonNodes(vtree.asInternal().getRight(), variables);
            }
        }
    }

    private static int elmShannonScore(final VTree vtree, final Set<Integer> variables) {
        if (vtree.isLeaf()) {
            return 0;
        } else {
            if (vtree.asInternal().getLeft().isLeaf()) {
                final int childScore = elmShannonScore(vtree.asInternal().getRight(), variables);
                if (variables.contains(vtree.asInternal().getLeft().asLeaf().getVariable())) {
                    return childScore;
                } else {
                    return childScore + vtreeHeight(vtree);
                }
            } else {
                return elmShannonScore(vtree.asInternal().getLeft(), variables)
                        + elmShannonScore(vtree.asInternal().getRight(), variables);
            }
        }
    }

    private static int vtreeHeight(final VTree vtree) {
        if (vtree.isLeaf()) {
            return 1;
        } else {
            return Math.max(vtreeHeight(vtree.asInternal().getLeft()), vtreeHeight(vtree.asInternal().getRight())) + 1;
        }
    }
}
