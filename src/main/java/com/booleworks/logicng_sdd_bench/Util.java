package com.booleworks.logicng_sdd_bench;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.transformations.PureExpansionTransformation;
import com.booleworks.logicng.transformations.cnf.CnfConfig;
import com.booleworks.logicng.transformations.cnf.CnfEncoder;

public class Util {
    public static Formula encodeAsPureCnf(final FormulaFactory f, final Formula formula) {
        final PureExpansionTransformation expander = new PureExpansionTransformation(f);
        final Formula expandedFormula = formula.transform(expander);

        final CnfConfig cnfConfig = CnfConfig.builder()
                .algorithm(CnfConfig.Algorithm.ADVANCED)
                .fallbackAlgorithmForAdvancedEncoding(CnfConfig.Algorithm.TSEITIN).build();

        return CnfEncoder.encode(f, expandedFormula, cnfConfig);
    }

}
