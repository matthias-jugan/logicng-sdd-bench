package com.booleworks.logicng_sdd_bench.setups;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.Variable;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.transformations.cnf.CnfConfig;
import com.booleworks.logicng.transformations.cnf.CnfEncoder;
import com.booleworks.logicng_sdd_bench.Input;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.ExElimSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentEntry;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentGroup;
import com.booleworks.logicng_sdd_bench.experiments.compilation.CompileSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.compilation.PcSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProblemFunction;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ProjectedCompilationSetups {
    public static ProblemFunction<ProjectionProblem> exportedPdfToProjectionProblem = (input, f) -> {
        var formula = input.asFormula();
        var allVariables = formula.variables(f);
        var elimVars = allVariables.stream()
                .filter(v -> v.getName().endsWith("_p") || v.getName().endsWith("_c") || v.getName().endsWith("_d"))
                .collect(Collectors.toSet());
        var projectionVars = allVariables.stream().filter(v -> !elimVars.contains(v)).collect(Collectors.toSet());
        return new ProjectionProblem(formula, elimVars, projectionVars);
    };


    public static void projectedCompileReal(final List<InputFile> inputs, final List<String> arguments,
                                            final Logger logger,
                                            final Supplier<ComputationHandler> handler) {
        final var results = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("Projected Compilation", new PcSddExperiment(Util.DEFAULT_COMPILER_CONFIG),
                        exportedPdfToProjectionProblem),
                new ExperimentEntry<>("Compile + Eliminate", new ExElimSddExperiment(Util.DEFAULT_COMPILER_CONFIG),
                        exportedPdfToProjectionProblem)
        )).runExperiments(inputs, logger, handler);
    }

    public static void projectedCompileRandom(final List<InputFile> inputs, final List<String> arguments,
                                              final Logger logger,
                                              final Supplier<ComputationHandler> handler) {
        final var results = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("Projected Compilation (20%)", new PcSddExperiment(Util.DEFAULT_COMPILER_CONFIG),
                        ProjectionProblem.quantifyRandom(0.2, 1)),
                new ExperimentEntry<>("Compile + Eliminate (20%)", new ExElimSddExperiment(),
                        ProjectionProblem.quantifyRandom(0.2, 1)),
                new ExperimentEntry<>("Projected Compilation (50%)", new PcSddExperiment(Util.DEFAULT_COMPILER_CONFIG),
                        ProjectionProblem.quantifyRandom(0.5, 1)),
                new ExperimentEntry<>("Compile + Eliminate (50%)", new ExElimSddExperiment(),
                        ProjectionProblem.quantifyRandom(0.5, 1)),
                new ExperimentEntry<>("Projected Compilation (80%)", new PcSddExperiment(Util.DEFAULT_COMPILER_CONFIG),
                        ProjectionProblem.quantifyRandom(0.8, 1)),
                new ExperimentEntry<>("Compile + Eliminate (80%)", new ExElimSddExperiment(),
                        ProjectionProblem.quantifyRandom(0.8, 1))
        )).runExperiments(inputs, logger, handler);
    }

    public static void compileProjectedEncoding(final List<InputFile> inputs, final List<String> arguments,
                                                final Logger logger, final Supplier<ComputationHandler> handler) {
        final ProblemFunction<Formula> pureEncoding = (input, f) -> {
            assert input instanceof Input.Formula;
            final var formula = ((Input.Formula) input).formula();
            return Util.encodeAsPureCnf(f, formula);
        };
        final ProblemFunction<ProjectionProblem> pgEncoding = (input, f) -> {
            assert input instanceof Input.Formula;
            final var formula = ((Input.Formula) input).formula();
            final var cnf = CnfEncoder.encode(f, formula,
                    CnfConfig.builder().algorithm(CnfConfig.Algorithm.PLAISTED_GREENBAUM).build());
            final Set<Variable> originalVars = input.asFormula().variables(f);
            final Set<Variable> auxVars = new TreeSet<>();
            for (final Variable v : cnf.variables(f)) {
                if (!originalVars.contains(v)) {
                    auxVars.add(v);
                }
            }
            return new ProjectionProblem(cnf, auxVars, originalVars);
        };
        final var results = new ExperimentGroup<>(List.of(
                new ExperimentEntry<>("CNF (Pure)", new CompileSddExperiment(), pureEncoding),
                new ExperimentEntry<>("CNF (PG + Proj)", new PcSddExperiment(), pgEncoding)
        )).runExperiments(inputs, logger, handler);
    }
}
