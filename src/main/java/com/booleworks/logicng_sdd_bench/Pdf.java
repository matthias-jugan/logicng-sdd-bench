package com.booleworks.logicng_sdd_bench;

import com.booleworks.logicng.datastructures.Assignment;
import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Literal;
import com.booleworks.logicng.formulas.Variable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Pdf {
    public static final String DATE_PREFIX = System.getenv("PDF_DATE");
    public static final String COUNTRY_PREFIX = System.getenv("PDF_COUNTRY");

    final FormulaFactory f;
    final Formula baseFormula;
    final String name;
    final List<Variable> dateCodes;
    final List<Variable> countryCodes;
    final Assignment projAssignment;

    public Pdf(final String name, final FormulaFactory f, final Formula baseFormula) {
        this.f = f;
        this.baseFormula = baseFormula;
        this.name = name;
        final var variables = baseFormula.variables(f);
        this.dateCodes = variables.stream()
                .filter(v -> v.getName().startsWith(DATE_PREFIX))
                .collect(Collectors.toList());
        this.countryCodes = variables.stream()
                .filter(v -> v.getName().startsWith(COUNTRY_PREFIX))
                .collect(Collectors.toList());
        this.projAssignment = new Assignment();
    }

    public Pdf projectDates(final Set<Variable> dcs) {
        for (final Variable code : dateCodes) {
            if (!dcs.contains(code)) {
                projAssignment.addLiteral(code.negate(f));
            } else if (dcs.size() == 1) {
                projAssignment.addLiteral(code);
            }
        }
        return this;
    }

    public Pdf projectCounties(final Set<Variable> ccs) {
        for (final Variable code : countryCodes) {
            if (!ccs.contains(code)) {
                projAssignment.addLiteral(code.negate(f));
            } else if (ccs.size() == 1) {
                projAssignment.addLiteral(code);
            }
        }
        return this;
    }

    public Pdf projectFeatures(final Set<Literal> features) {
        for (final var feature : features) {
            projAssignment.addLiteral(feature);
        }
        return this;
    }

    public Formula project() {
        return baseFormula.restrict(f, projAssignment);
    }


    public FormulaFactory getF() {
        return f;
    }

    public Formula getBaseFormula() {
        return baseFormula;
    }

    public List<Variable> getDateCodes() {
        return dateCodes;
    }

    public List<Variable> getCountryCodes() {
        return countryCodes;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return getName().substring(4, 8);
    }
}
