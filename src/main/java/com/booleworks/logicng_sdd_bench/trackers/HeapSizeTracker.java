package com.booleworks.logicng_sdd_bench.trackers;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.events.LngEvent;
import com.booleworks.logicng_sdd_bench.Util;

public class HeapSizeTracker implements ComputationHandler {
    final private LngEvent triggerEvent;
    final private int limit;
    private int counter = 0;
    private int calls = 0;
    private long maxSize;

    public HeapSizeTracker(final LngEvent triggerEvent, final int limit) {
        this.triggerEvent = triggerEvent;
        this.limit = limit;
        maxSize = Util.getHeapSize();
    }

    @Override
    public boolean shouldResume(final LngEvent event) {
        if (triggerEvent == null || event == triggerEvent) {
            calls++;
            counter++;
            if (counter >= limit) {
                counter = 0;
                maxSize = Math.max(maxSize, Util.getHeapSize());
            }
        }
        return false;
    }

    public int getCalls() {
        return calls;
    }

    public long getMaxSize() {
        return maxSize;
    }
}
