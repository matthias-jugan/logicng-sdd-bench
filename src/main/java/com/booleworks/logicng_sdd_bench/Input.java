package com.booleworks.logicng_sdd_bench;

public sealed interface Input permits Input.Formula, Input.Pdf {
    default com.booleworks.logicng.formulas.Formula asFormula() {
        return switch (this) {
            case final Formula formula -> formula.formula();
            case final Pdf pdf -> pdf.pdf.getBaseFormula();
        };
    }

    record Formula(com.booleworks.logicng.formulas.Formula formula) implements Input {
    }

    record Pdf(com.booleworks.logicng_sdd_bench.Pdf pdf) implements Input {
    }
}
