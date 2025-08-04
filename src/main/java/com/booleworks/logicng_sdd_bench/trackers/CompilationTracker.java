package com.booleworks.logicng_sdd_bench.trackers;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.events.ComputationStartedEvent;
import com.booleworks.logicng.handlers.events.LngEvent;
import com.booleworks.logicng.handlers.events.SimpleEvent;
import com.booleworks.logicng_sdd_bench.experiments.results.ExperimentResult;

import java.util.List;

public class CompilationTracker implements ComputationHandler, ExperimentResult {

    private long dTreeStart = -1;
    private long dTreeEnd = -1;
    private long vTreeStart = -1;
    private long vTreeEnd = -1;
    private long compilationStart = -1;
    private long compilationEnd = -1;
    private long globalStart = -1;
    private long globalEnd = -1;
    private final ComputationHandler handler;
    private long sddSize = -1;
    private long nodeSize = -1;
    private int formulaVariableCount = -1;
    private int projectedVariableCount = -1;

    public CompilationTracker(final ComputationHandler handler) {
        this.handler = handler;
    }

    @Override
    public boolean shouldResume(final LngEvent event) {
        if (event == SimpleEvent.VTREE_CUTSET_GENERATION && dTreeStart != -1) {
            if (dTreeEnd == -1) {
                dTreeEnd = System.currentTimeMillis();
            }
        }
        if (event instanceof ComputationStartedEvent) {
            if (globalStart == -1) {
                globalStart = System.currentTimeMillis();
            }
            if (event == ComputationStartedEvent.DTREE_GENERATION_STARTED && dTreeStart == -1) {
                dTreeStart = System.currentTimeMillis();
            } else if (event == ComputationStartedEvent.VTREE_GENERATION_STARTED && vTreeStart == -1) {
                vTreeStart = System.currentTimeMillis();
            } else if (event == ComputationStartedEvent.DNNF_COMPUTATION_STARTED && compilationStart == -1) {
                final long t = System.currentTimeMillis();
                compilationStart = t;
                if (dTreeEnd == -1) {
                    dTreeEnd = t;
                }
            } else if (event == ComputationStartedEvent.SDD_COMPUTATION_STARTED && compilationStart == -1) {
                final long t = System.currentTimeMillis();
                compilationStart = t;
                if (dTreeEnd == -1) {
                    dTreeEnd = t;
                }
                if (vTreeStart != -1) {
                    vTreeEnd = t;
                }
            }
        }
        return handler == null || handler.shouldResume(event);
    }

    public void start() {
        if (globalStart == -1) {
            globalStart = System.currentTimeMillis();
        }
    }

    public void done() {
        final long t = System.currentTimeMillis();
        compilationEnd = t;
        globalEnd = t;
    }

    public int getFormulaVariableCount() {
        return formulaVariableCount;
    }

    public void setFormulaVariableCount(final int formulaVariableCount) {
        this.formulaVariableCount = formulaVariableCount;
    }

    public int getProjectedVariableCount() {
        return projectedVariableCount;
    }

    public void setProjectedVariableCount(final int projectedVariableCount) {
        this.projectedVariableCount = projectedVariableCount;
    }

    public long getSddSize() {
        return sddSize;
    }

    public void setSddSize(final long sddSize) {
        this.sddSize = sddSize;
    }

    public long getNodeSize() {
        return nodeSize;
    }

    public void setNodeSize(final long nodeSize) {
        this.nodeSize = nodeSize;
    }

    public long getGlobal() {
        if (globalEnd == -1) {
            return -1;
        } else {
            return globalEnd - globalStart;
        }
    }

    public long getCompilation() {
        if (compilationStart == -1) {
            return -2;
        } else if (compilationEnd == -1) {
            return -1;
        } else {
            return compilationEnd - compilationStart;
        }
    }

    public long getVTree() {
        if (vTreeStart == -1) {
            return -2;
        } else if (vTreeEnd == -1) {
            return -1;
        } else {
            return vTreeEnd - vTreeStart;
        }
    }

    public long getDTree() {
        if (dTreeStart == -1) {
            return -2;
        } else if (dTreeEnd == -1) {
            return -1;
        } else {
            return dTreeEnd - dTreeStart;
        }
    }

    @Override
    public List<String> getResult() {
        return List.of(
                String.valueOf(getGlobal()),
                String.valueOf(getDTree()),
                String.valueOf(getVTree()),
                String.valueOf(getCompilation()),
                String.valueOf(getNodeSize()),
                String.valueOf(getSddSize()),
                String.valueOf(getFormulaVariableCount()),
                String.valueOf(getProjectedVariableCount())
        );
    }

    @Override
    public String getEssentialsAsCsv() {
        return getGlobal() + "," + getNodeSize() + "," + getSddSize() + "," + getFormulaVariableCount() + ","
                + getProjectedVariableCount();
    }
}
