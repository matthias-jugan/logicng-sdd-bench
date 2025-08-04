package com.booleworks.logicng_sdd_bench.experiments.problems;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Variable;
import com.booleworks.logicng_sdd_bench.Input;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public record ProjectionProblem(Formula formula, Set<Variable> quantifiedVariables, Set<Variable> projectedVariables) {
    public static ProblemFunction<ProjectionProblem> quantifyRandom(final double ratio, final int seed) {
        return (final Input input, final FormulaFactory f) -> {
            final Random random = new Random(seed);
            final List<Variable> variables = new ArrayList<>(input.asFormula().variables(f));
            final SortedSet<Variable> quantifiedVariables = new TreeSet<>();
            final int samples = (int) (ratio * (double) variables.size());
            for (int i = 0; i < samples; ++i) {
                final int index = random.nextInt(variables.size());
                quantifiedVariables.add(variables.remove(index));
            }
            return new ProjectionProblem(input.asFormula(), quantifiedVariables, new TreeSet<>(variables));
        };
    }
}
