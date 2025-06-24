package com.booleworks.logicng_sdd_bench.trackers;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.NopHandler;
import com.booleworks.logicng.handlers.events.LngEvent;

import java.util.List;

public class TrackerGroup implements ComputationHandler {
    private final List<ComputationHandler> trackers;
    private final ComputationHandler handler;

    public TrackerGroup(final List<ComputationHandler> trackers, final ComputationHandler handler) {
        this.trackers = trackers;
        this.handler = handler;
    }

    public TrackerGroup(final List<ComputationHandler> trackers) {
        this.trackers = trackers;
        this.handler = NopHandler.get();
    }

    @Override
    public boolean shouldResume(final LngEvent event) {
        for (final var tracker : trackers) {
            tracker.shouldResume(event);
        }
        return handler.shouldResume(event);
    }

}
