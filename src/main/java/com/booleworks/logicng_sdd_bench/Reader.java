package com.booleworks.logicng_sdd_bench;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.io.parsers.ParserException;
import com.booleworks.logicng.io.readers.DimacsReader;
import com.booleworks.logicng.io.readers.FormulaReader;

import java.io.IOException;

public class Reader {
    private Reader() {
    }

    public static Formula load(final InputFile file, final FormulaFactory f) throws ParserException, IOException {
        return switch (file.format()) {
            case DIMACS -> f.and(DimacsReader.readCNF(f, file.file()));
            case ARBITRARY -> f.and(FormulaReader.readFormulas(f, file.file()));
        };
    }
}
