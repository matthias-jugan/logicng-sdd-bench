package com.booleworks.logicng_sdd_bench.trackers;

import com.booleworks.logicng.handlers.events.LngEvent;
import com.booleworks.logicng.handlers.events.SddMinimizationEvent;
import com.booleworks.logicng.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class SddSizeTracker extends SimpleTimeTracker {
    final List<Pair<Long, Long>> allSizes;
    long lastSize = Long.MAX_VALUE;

    public SddSizeTracker() {
        allSizes = new ArrayList<>();
    }

    @Override
    public boolean shouldResume(final LngEvent event) {
        if (event instanceof SddMinimizationEvent) {
            final long size = ((SddMinimizationEvent) event).getNewSize();
            if (size < lastSize) {
                allSizes.add(new Pair<>(size, getCurrentTime()));
                lastSize = size;
            }
        }
        return super.shouldResume(event);
    }

    public List<Pair<Long, Long>> getAllSizes() {
        return allSizes;
    }
}
