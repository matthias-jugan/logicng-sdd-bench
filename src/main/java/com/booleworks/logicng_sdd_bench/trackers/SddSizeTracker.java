package com.booleworks.logicng_sdd_bench.trackers;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.events.LngEvent;
import com.booleworks.logicng.handlers.events.SddMinimizationStepEvent;

import java.util.ArrayList;
import java.util.List;

public class SddSizeTracker implements ComputationHandler {
    final List<Integer> allSizes;

    public SddSizeTracker() {
        allSizes = new ArrayList<>();
    }

    @Override
    public boolean shouldResume(final LngEvent event) {
        if (event instanceof SddMinimizationStepEvent) {
            allSizes.add(((SddMinimizationStepEvent) event).getNewSize());
        }
        return true;
    }

    public List<Integer> getAllSizes() {
        return allSizes;
    }
}
