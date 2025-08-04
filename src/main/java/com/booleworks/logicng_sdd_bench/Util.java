package com.booleworks.logicng_sdd_bench;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.LngResult;
import com.booleworks.logicng.knowledgecompilation.sdd.compilers.SddCompilerConfig;
import com.booleworks.logicng.knowledgecompilation.sdd.datastructures.vtree.DecisionVTreeGenerator;
import com.booleworks.logicng.transformations.PureExpansionTransformation;
import com.booleworks.logicng.transformations.cnf.CnfConfig;
import com.booleworks.logicng.transformations.cnf.CnfEncoder;
import com.booleworks.logicng.transformations.cnf.CnfSubsumption;
import com.booleworks.logicng.transformations.simplification.BackboneSimplifier;

import java.util.function.Supplier;

public class Util {
    public final static Supplier<SddCompilerConfig.Builder> DEFAULT_COMPILER_CONFIG = () -> SddCompilerConfig.builder()
            .compiler(SddCompilerConfig.Compiler.TOP_DOWN)
            .inputSimplification(true)
            .prioritizationStrategy(DecisionVTreeGenerator.PrioritizationStrategy.NONE);

    public static Formula encodeAsPureCnf(final FormulaFactory f, final Formula formula) {
        final PureExpansionTransformation expander = new PureExpansionTransformation(f);
        final Formula expandedFormula = formula.transform(expander);

        final CnfConfig cnfConfig = CnfConfig.builder()
                .algorithm(CnfConfig.Algorithm.ADVANCED)
                .fallbackAlgorithmForAdvancedEncoding(CnfConfig.Algorithm.TSEITIN).build();

        return CnfEncoder.encode(f, expandedFormula, cnfConfig);
    }

    public static LngResult<Formula> optimizeFormulaForCompilation(final FormulaFactory f, final Formula formula,
                                                                   final ComputationHandler handler) {
        final LngResult<Formula> backboneSimplified = formula.transform(new BackboneSimplifier(f), handler);
        if (!backboneSimplified.isSuccess()) {
            return LngResult.canceled(backboneSimplified.getCancelCause());
        }
        return backboneSimplified.getResult().transform(new CnfSubsumption(f), handler);
    }

}
