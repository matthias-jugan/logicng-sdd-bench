package com.booleworks.logicng_sdd_bench;

import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.io.parsers.ParserException;
import com.booleworks.logicng.io.readers.DimacsReader;
import com.booleworks.logicng.io.readers.FormulaReader;

import java.io.IOException;

public class Reader {
    private Reader() {
    }

    public static Input load(final InputFile file, final FormulaFactory f) throws ParserException, IOException {
        return switch (file.format()) {
            case DIMACS -> new Input.Formula(f.and(DimacsReader.readCNF(f, file.file())));
            case ARBITRARY -> new Input.Formula(f.and(FormulaReader.readFormulas(f, file.file())));
            case PDF -> new Input.Pdf(new Pdf(file.name(), f, FormulaReader.readFormula(f, file.file())));
            case EXPORT -> throw new IllegalArgumentException("Cannot load an export file as formula");
        };
    }
}
