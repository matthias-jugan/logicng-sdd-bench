package com.booleworks.logicng_sdd_bench.setups;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.knowledgecompilation.sdd.algorithms.SddMinimization;
import com.booleworks.logicng.knowledgecompilation.sdd.algorithms.SddSize;
import com.booleworks.logicng.knowledgecompilation.sdd.algorithms.VTreeUtil;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompiler;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerConfig;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.Sdd;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddMinimizationConfig;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.SddNode;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.TransformationResult;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.vtree.VTree;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentEntry;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentGroup;
import com.booleworks.logicng_sdd_bench.experiments.other.VTreePropertyExperiment;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProblemFunction;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;
import com.booleworks.logicng_sdd_bench.experiments.results.ExperimentResult;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Supplier;

public class OtherSetups {

    public static void shannon(
            final List<InputFile> inputs, final List<String> arguments, final Logger logger,
            final Supplier<ComputationHandler> handler) {
        new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("proj", new VTreePropertyExperiment(), ProjectionProblem.quantifyRandom(0.5, 1))
        )).runExperiments(inputs, logger, handler);
    }

    public static void sizePerVTree(
            final List<InputFile> inputs, final List<String> arguments, final Logger logger,
            final Supplier<ComputationHandler> handler) {
        new ExperimentGroup<>(List.of(new ExperimentEntry<>("ABC",
                (final Formula input, final FormulaFactory f, final Logger l, final Supplier<ComputationHandler> h) -> {
                    final var config = SddCompilerConfig.builder().build();
                    final var result = SddCompiler.compile(input.cnf(f), config, f, h.get());
                    if (!result.isSuccess()) {
                        return new Res();
                    }
                    final var node = result.getResult().getNode();
                    final var sdd = result.getResult().getSdd();
                    printStats(node, result.getResult().getSdd());
                    sdd.pin(node);
                    final LngResult<TransformationResult> minRes =
                            SddMinimization.minimize(SddMinimizationConfig.unlimited(sdd));
                    if (!minRes.isSuccess() && !minRes.isPartial()) {
                        return new Res();
                    }
                    final var min = minRes.getPartialResult();
                    System.out.println("\n---");
                    printStats(min.getTranslations().get(node), sdd);
                    return new Res();
                },
                ProblemFunction::id
        ))).runExperiments(inputs, logger, handler);
    }

    private static void printStats(final SddNode node, final Sdd sdd) {
        final LinkedHashMap<VTree, Set<SddNode>> vtree2nodes = new LinkedHashMap<>();
        final Queue<SddNode> queue = new ArrayDeque<>();
        final HashSet<SddNode> visited = new HashSet<>();
        queue.add(node);
        visited.add(node);
        while (!queue.isEmpty()) {
            final SddNode current = queue.poll();
            final VTree vtree = sdd.vTreeOf(current);
            if (vtree == null) {
                continue;
            }
            final Set<SddNode> nodes = vtree2nodes.computeIfAbsent(vtree, (key) -> new HashSet<>());
            nodes.add(current);
            if (current.isDecomposition()) {
                for (final var element : current.asDecomposition()) {
                    if (visited.add(element.getPrime())) {
                        queue.add(element.getPrime());
                    }
                    if (visited.add(element.getSub())) {
                        queue.add(element.getSub());
                    }
                }
            }
        }
        for (final var e : vtree2nodes.entrySet()) {
            if (e.getKey().isLeaf()) {
                continue;
            }
            final long size1 = VTreeUtil.varCount(e.getKey());
            final long size2 = SddSize.size(e.getValue());
            double balance = -1;
            if (!e.getKey().isLeaf()) {
                final long left =
                        SddSize.size(vtree2nodes.getOrDefault(e.getKey().asInternal().getLeft(), Set.of()));
                final long right = SddSize.size(vtree2nodes.getOrDefault(e.getKey().asInternal().getRight(), Set.of()));
                balance = ((double) Math.abs(left - right)) / (left + right);
            }
            System.out.println(
                    size1 + " -> " + size2 + " (" + ((double) size2) / ((double) size1) + ") balance " + balance);
        }
    }

    private record Res() implements ExperimentResult {
        @Override
        public List<String> getResult() {
            return List.of();
        }

        @Override
        public String getEssentialsAsCsv() {
            return "";
        }
    }
}
