package com.booleworks.logicng_sdd_bench.trackers;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.events.LngEvent;

public class SimpleTimeTracker implements ComputationHandler {
    long startTime = -1;
    long endTime = -1;

    public SimpleTimeTracker() {
    }

    @Override
    public boolean shouldResume(LngEvent event) {
        if (event == BenchmarkEvent.START_EXPERIMENT) {
            startTime = System.currentTimeMillis();
        } else if (event == BenchmarkEvent.COMPLETED_EXPERIMENT) {
            endTime = System.currentTimeMillis();
        }
        return true;
    }

    public long getTime() {
        if (endTime == -1) {
            return -1;
        } else {
            return endTime - startTime;
        }
    }
}
