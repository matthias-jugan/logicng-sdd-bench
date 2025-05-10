package com.booleworks.logicng_sdd_bench.trackers;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.NopHandler;
import com.booleworks.logicng.handlers.events.LngEvent;

import java.util.List;

public class TrackerGroup implements ComputationHandler {
    private final List<ComputationHandler> trackers;

    public TrackerGroup(List<ComputationHandler> trackers, ComputationHandler handler) {
        this.trackers = trackers;
        this.handler = handler;
    }

    public TrackerGroup(List<ComputationHandler> trackers) {
        this.trackers = trackers;
        this.handler = NopHandler.get();
    }

    private final ComputationHandler handler;

    @Override
    public boolean shouldResume(LngEvent event) {
        for (var tracker : trackers) {
            tracker.shouldResume(event);
        }
        return handler.shouldResume(event);
    }

}
