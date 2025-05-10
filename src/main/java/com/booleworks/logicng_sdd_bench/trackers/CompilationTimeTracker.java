package com.booleworks.logicng_sdd_bench.trackers;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.events.ComputationStartedEvent;
import com.booleworks.logicng.handlers.events.LngEvent;
import com.booleworks.logicng.handlers.events.SimpleEvent;
import com.booleworks.logicng_sdd_bench.experiments.results.ExperimentResult;

import java.util.List;

public class CompilationTimeTracker implements ComputationHandler, ExperimentResult {

    private long dTreeStart = -1;
    private long dTreeEnd = -1;
    private long vTreeStart = -1;
    private long vTreeEnd = -1;
    private long compilationStart = -1;
    private long compilationEnd = -1;
    private long globalStart = -1;
    private long globalEnd = -1;
    private final ComputationHandler handler;
    public long exps;

    public CompilationTimeTracker(final ComputationHandler handler) {
        this.handler = handler;
    }

    @Override
    public boolean shouldResume(final LngEvent event) {
        if (event == SimpleEvent.SDD_SHANNON_EXPANSION) {
            exps++;
        }
        if (event instanceof ComputationStartedEvent) {
            if (globalStart == -1) {
                globalStart = System.currentTimeMillis();
            }
            if (event == ComputationStartedEvent.DTREE_GENERATION_STARTED && dTreeStart == -1) {
                dTreeStart = System.currentTimeMillis();
            } else if (event == ComputationStartedEvent.VTREE_GENERATION_STARTED && vTreeStart == -1) {
                final long t = System.currentTimeMillis();
                dTreeEnd = t;
                vTreeStart = t;
            } else if (event == ComputationStartedEvent.DNNF_COMPUTATION_STARTED && compilationStart == -1) {
                final long t = System.currentTimeMillis();
                compilationStart = t;
                if (dTreeEnd == -1) {
                    dTreeEnd = t;
                }
            } else if (event == ComputationStartedEvent.SDD_COMPUTATION_STARTED && compilationStart == -1) {
                final long t = System.currentTimeMillis();
                compilationStart = t;
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
                String.valueOf(getCompilation())
        );
    }

    @Override
    public String getEssentialsAsCsv() {
        return String.valueOf(getGlobal());
    }
}
