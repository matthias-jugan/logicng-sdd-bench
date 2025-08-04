package com.booleworks.logicng_sdd_bench.setups;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.Variable;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.transformations.cnf.CnfConfig;
import com.booleworks.logicng.transformations.cnf.CnfEncoder;
import com.booleworks.logicng_sdd_bench.Input;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.Pdf;
import com.booleworks.logicng_sdd_bench.PdfExport;
import com.booleworks.logicng_sdd_bench.Util;
import com.booleworks.logicng_sdd_bench.experiments.CompileSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentEntry;
import com.booleworks.logicng_sdd_bench.experiments.ExperimentGroup;
import com.booleworks.logicng_sdd_bench.experiments.PcSddExperiment;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProblemFunction;
import com.booleworks.logicng_sdd_bench.experiments.problems.ProjectionProblem;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PdfSetups {
    public static void compileCnfEncoding(final List<InputFile> inputs, final List<String> arguments,
                                          final Logger logger,
                                          final Supplier<ComputationHandler> handler) {
        final ProblemFunction<Formula> pureEncoding = (input, f) -> {
            final var formula = input instanceof Input.Pdf
                                ? PdfExport.applyRestrictions(((Input.Pdf) input).pdf(), arguments, f)
                                : input.asFormula();
            return Util.encodeAsPureCnf(f, formula);
        };
        final ProblemFunction<ProjectionProblem> pureEncodingAndProject = (input, f) -> {
            final var formula = input instanceof Input.Pdf
                                ? PdfExport.applyRestrictions(((Input.Pdf) input).pdf(), arguments, f)
                                : input.asFormula();
            final var encoded = Util.encodeAsPureCnf(f, formula);
            final var originalVars = formula.variables(f);
            final var auxVars =
                    encoded.variables(f).stream().filter(v -> !originalVars.contains(v)).collect(Collectors.toSet());
            return new ProjectionProblem(encoded, auxVars, originalVars);
        };
        final ProblemFunction<ProjectionProblem> pgEncoding = (input, f) -> {
            final var formula = input instanceof Input.Pdf
                                ? PdfExport.applyRestrictions(((Input.Pdf) input).pdf(), arguments, f)
                                : input.asFormula();
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
                //new ExperimentEntry<>("CNF (Pure + Proj)", new PcSddExperiment(), pureEncodingAndProject),
                new ExperimentEntry<>("CNF (PG + Proj)", new PcSddExperiment(), pgEncoding)
                //new ExperimentEntry<>("CNF (DNNF Pure)", new CompileDnnfExperiment(), pureEncoding)
        )).runExperiments(inputs, logger, handler);
    }

    private static ProblemFunction<Formula> getProjectionFunction(final Set<String> countries,
                                                                  final Set<String> dates) {
        return (input, f) -> {
            if (input instanceof Input.Formula) {
                return null;
            }
            final var pdf = ((Input.Pdf) input).pdf();
            final Set<Variable> dateCodes = dates
                    .stream()
                    .map(d -> f.variable(String.format("%s_%s_%s", Pdf.DATE_PREFIX, pdf.getCode(), d)))
                    .collect(Collectors.toSet());
            final Set<Variable> countryCodes = countries
                    .stream()
                    .map(d -> f.variable(String.format("%s_%s", Pdf.COUNTRY_PREFIX, d)))
                    .collect(Collectors.toSet());
            pdf.projectDates(dateCodes);
            pdf.projectCounties(countryCodes);
            return pdf.project();
        };
    }


}
