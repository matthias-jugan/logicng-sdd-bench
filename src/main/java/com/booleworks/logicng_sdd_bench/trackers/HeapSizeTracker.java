package com.booleworks.logicng_sdd_bench.trackers;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.events.LngEvent;

public class HeapSizeTracker implements ComputationHandler {
    final LngEvent triggerEvent;
    final int limit;
    int counter = 0;
    long maxSize;

    public HeapSizeTracker(final LngEvent triggerEvent, final int limit) {
        this.triggerEvent = triggerEvent;
        this.limit = limit;
        maxSize = Runtime.getRuntime().totalMemory();
    }

    @Override
    public boolean shouldResume(final LngEvent event) {
        if (triggerEvent == null || event == triggerEvent) {
            counter++;
            if (counter >= limit) {
                counter = 0;
                maxSize = Runtime.getRuntime().totalMemory();
            }
        }
        return false;
    }
}
