package com.booleworks.logicng_sdd_bench.setups;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.util.Pair;
import com.booleworks.logicng_sdd_bench.InputFile;
import com.booleworks.logicng_sdd_bench.Logger;
import com.booleworks.logicng_sdd_bench.PdfExport;

import java.util.List;
import java.util.function.Supplier;

public class Setups {
    public interface RunSetup {
        void run(final List<InputFile> inputs, final List<String> arguments, final Logger logger,
                 final Supplier<ComputationHandler> handler) throws Exception;
    }

    public static final List<Pair<String, RunSetup>> SETUPS = List.of(
            new Pair<>("compile-all", CompilationSetups::compileAll),
            new Pair<>("compile-sd", CompilationSetups::compileSd),
            new Pair<>("compile-s", CompilationSetups::compileS),
            new Pair<>("measure-heap", CompilationSetups::measureHeap),
            new Pair<>("compilation-progress", CompilationSetups::compilationProgress),
            new Pair<>("model-counting", ModelCountingSetups::modelCounting),
            new Pair<>("projected-model-counting", ModelCountingSetups::projectedModelCounting),
            new Pair<>("minimize", MinimizationSetups::minimize),
            new Pair<>("minimize-dt", MinimizationSetups::minimizeDT),
            new Pair<>("min-pmc", MinimizationSetups::minPMC),
            new Pair<>("compile-projected-encoding", ProjectedCompilationSetups::compileProjectedEncoding),
            new Pair<>("pdf-codes", PdfExport::pdfStats),
            new Pair<>("projected-model-enumeration-real", ModelEnumerationSetups::projectedModelEnumerationReal),
            new Pair<>("input-simplification", CompilationSetups::compileSimplification),
            new Pair<>("projected-compile-real", ProjectedCompilationSetups::projectedCompileReal),
            new Pair<>("projected-compile-random", ProjectedCompilationSetups::projectedCompileRandom),
            new Pair<>("recompile", MinimizationSetups::recompile)
    );

}
