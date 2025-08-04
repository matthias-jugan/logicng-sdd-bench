package com.booleworks.logicng_sdd_bench;

import com.booleworks.logicng.datastructures.Substitution;
import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Literal;
import com.booleworks.logicng.formulas.Variable;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.io.parsers.ParserException;
import com.booleworks.logicng.io.writers.FormulaWriter;
import com.booleworks.logicng.transformations.Anonymizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PdfExport {
    public static void export(final File dst, final List<InputFile> inputs, final List<String> arguments)
            throws Exception {
        final Optional<String> nameSuffix = arguments.stream().filter(arg -> arg.startsWith("n:")).findFirst();
        if (nameSuffix.isEmpty()) {
            System.err.println("Missing name");
            return;
        }
        dst.mkdirs();
        for (int i = 0; i < inputs.size(); i++) {
            final var input = inputs.get(i);
            final var name = String.format("auto%d_%s.txt", i, nameSuffix.get().substring(2));
            final var path = Path.of(dst.getPath(), name);
            exportFile(input, arguments, path);
        }
    }

    private static void exportFile(final InputFile input, final List<String> arguments, final Path output)
            throws ParserException, IOException {
        System.out.printf("Start export for %s\n", input.name());
        final var f = FormulaFactory.caching();
        assert input.format() == InputFile.InputFormat.PDF;
        final Pdf pdf = ((Input.Pdf) Reader.load(input, f)).pdf();
        final var restricted = applyRestrictions(pdf, arguments, f);
        if (!restricted.isSatisfiable(f)) {
            return;
        }
        final var anonymized = anonymizeFormula(restricted, pdf.getCode(), arguments, f);
        final var outputFile = output.toFile();
        if (outputFile.createNewFile()) {
            System.out.printf("Exported %s\n", output);
            FormulaWriter.write(outputFile, anonymized, true);
        } else {
            System.err.printf("Could not export %s as it already exists\n", output);
        }
    }

    public static Formula anonymizeFormula(final Formula formula, final String pdfCode, final List<String> arguments,
                                           final FormulaFactory f) {
        final var anonymizer = new Anonymizer(f, "v");
        final var anonymized = anonymizer.apply(formula);
        final Set<Variable> eliminationVariables = new TreeSet<>();
        for (final String arg : arguments) {
            if (arg.startsWith("pf:")) {
                for (final String featureCode : splitArgString(arg)) {
                    eliminationVariables.add(f.variable(featureCode));
                }
            }
        }
        final Substitution eliminationSubstitutions = new Substitution();
        for (final Variable v : eliminationVariables) {
            final Variable anonVar = (Variable) anonymizer.getSubstitution().getSubstitution(v);
            if (anonVar != null) {
                eliminationSubstitutions.addMapping(anonVar, f.variable(String.format("%s_p", anonVar.getName())));
            }
        }
        for (final var v : formula.variables(f)) {
            if (v.getName().startsWith(Pdf.DATE_PREFIX)) {
                final Variable anonVar = (Variable) anonymizer.getSubstitution().getSubstitution(v);
                eliminationSubstitutions.addMapping(anonVar, f.variable(String.format("%s_d", anonVar.getName())));
            }
            if (v.getName().startsWith(Pdf.COUNTRY_PREFIX)) {
                final Variable anonVar = (Variable) anonymizer.getSubstitution().getSubstitution(v);
                eliminationSubstitutions.addMapping(anonVar, f.variable(String.format("%s_c", anonVar.getName())));
            }
        }
        return anonymized.substitute(f, eliminationSubstitutions);
    }

    public static Formula applyRestrictions(final Pdf pdf, final List<String> arguments, final FormulaFactory f) {
        final Set<Literal> features = new TreeSet<>();
        final Set<Variable> dates = new TreeSet<>();
        final Set<Variable> countries = new TreeSet<>();
        for (final String arg : arguments) {
            if (arg.startsWith("rf:")) {
                for (final String featureCode : splitArgString(arg)) {
                    if (featureCode.startsWith("~")) {
                        features.add(f.literal(featureCode.substring(1), false));
                    } else {
                        features.add(f.variable(featureCode));
                    }
                }
            }
            if (arg.startsWith("rc:")) {
                for (final String country : splitArgString(arg)) {
                    countries.add(f.variable(String.format("%s_%s", Pdf.COUNTRY_PREFIX, country)));
                }
            }
            if (arg.startsWith("rd:")) {
                for (final String date : splitArgString(arg)) {
                    dates.add(f.variable(String.format("%s_%s_%s", Pdf.DATE_PREFIX, pdf.getCode(), date)));
                }
            }
        }
        if (!dates.isEmpty()) {
            pdf.projectDates(dates);
        }
        if (!countries.isEmpty()) {
            pdf.projectCounties(countries);
        }
        return pdf.projectFeatures(features).project();
    }

    private static String[] splitArgString(final String arg) {
        return arg.substring(3).split(",");
    }


    public static void pdfStats(final List<InputFile> inputs, final List<String> arguments, final Logger logger,
                                final Supplier<ComputationHandler> handler) throws ParserException, IOException {
        final FormulaFactory f = FormulaFactory.caching();
        final Set<Variable> countriesUnion = new TreeSet<>();
        final Set<Variable> datesUnion = new TreeSet<>();
        Set<Variable> countriesInter = null;
        Set<Variable> datesInter = null;
        for (final InputFile inputFile : inputs.stream().filter(i -> i.format() == InputFile.InputFormat.PDF)
                .toList()) {
            final var pdf = ((Input.Pdf) Reader.load(inputFile, f)).pdf();
            countriesUnion.addAll(pdf.getCountryCodes());
            datesUnion.addAll(pdf.getDateCodes());
            if (countriesInter == null) {
                countriesInter = new TreeSet<>(pdf.getCountryCodes());
                datesInter = new TreeSet<>(pdf.getDateCodes());
            } else {
                countriesInter = countriesInter.stream()
                        .filter(v -> !pdf.getCountryCodes().contains(v))
                        .collect(Collectors.toSet());
                datesInter = datesInter.stream()
                        .filter(v -> !pdf.getDateCodes().contains(v))
                        .collect(Collectors.toSet());
            }
        }
        System.out.println("Country codes: " + countriesUnion.size());
        System.out.println("Date codes: " + datesUnion.size());
        System.out.println("Date inter codes: " + datesInter.size());
        System.out.println(countriesUnion);
        System.out.println(datesUnion);
        System.out.println(countriesInter);
        System.out.println(datesInter);

        System.out.println("\n// 1 Both");
        int seed = 0;
        for (int variant = 0; variant < 10; ++variant) {
            final var dates = pickRandom(datesInter, 1, ++seed);
            final var countries = pickRandom(countriesUnion, 1, ++seed);
            final var name = String.format("c001_d001_v%03d", variant);
            System.out.printf("n:%s rc:%s rd:%s\n", name, countryCodes2String(countries), dateCodes2String(dates));
        }

        System.out.println("\n// Scale Dates");
        for (int dateCount = 10; dateCount <= 64; dateCount += 10) {
            for (int variant = 0; variant < 1; ++variant) {
                final List<Variable> dates;
                if (dateCount <= datesInter.size()) {
                    dates = pickRandom(datesInter, dateCount, ++seed);
                } else {
                    dates = pickRandom(datesUnion, dateCount, ++seed);
                }
                final var countries = pickRandom(countriesUnion, 1, ++seed);
                final var name = String.format("c001_d%03d_v%03d", dateCount, variant);
                System.out.printf("n:%s rc:%s rd:%s\n", name, countryCodes2String(countries), dateCodes2String(dates));
            }
        }

        System.out.println("\n// Scale Countries");
        for (int countryCount = 10; countryCount <= 64; countryCount += 10) {
            for (int variant = 0; variant < 1; ++variant) {
                final var dates = pickRandom(datesInter, 1, ++seed);
                final var countries = pickRandom(countriesUnion, countryCount, ++seed);
                final var name = String.format("c%03d_d001_v%03d", countryCount, variant);
                System.out.printf("n:%s rc:%s rd:%s\n", name, countryCodes2String(countries), dateCodes2String(dates));
            }
        }

        System.out.println("\n// Scale Both");
        for (int size = 5; size <= 64; size += 5) {
            for (int variant = 0; variant < 1; ++variant) {
                final List<Variable> dates;
                if (size <= datesInter.size()) {
                    dates = pickRandom(datesInter, size, ++seed);
                } else {
                    dates = pickRandom(datesUnion, size, ++seed);
                }
                final var countries = pickRandom(countriesUnion, size, ++seed);
                final var name = String.format("c%03d_d%03d_v%03d", size, size, variant);
                System.out.printf("n:%s rc:%s rd:%s\n", name, countryCodes2String(countries), dateCodes2String(dates));
            }
        }
    }

    private static List<Variable> pickRandom(final Collection<Variable> vars, final int count, final int seed) {
        final var varList = new ArrayList<>(vars);
        final var random = new Random(seed);
        final List<Variable> res = new ArrayList<>();
        for (int i = 0; i < count && !varList.isEmpty(); ++i) {
            final int idx = random.nextInt(varList.size());
            res.add(varList.get(idx));
            varList.remove(idx);
        }
        return res;
    }

    private static String countryCodes2String(final Collection<Variable> vars) {
        return vars.stream()
                .map(Literal::getName)
                .map(n -> n.substring(Pdf.COUNTRY_PREFIX.length() + 1))
                .collect(Collectors.joining(","));
    }

    private static String dateCodes2String(final Collection<Variable> vars) {
        return vars.stream()
                .map(Literal::getName)
                .map(n -> n.substring(Pdf.DATE_PREFIX.length() + 6))
                .collect(Collectors.joining(","));
    }

}
