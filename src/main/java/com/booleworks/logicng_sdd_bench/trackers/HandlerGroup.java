package com.booleworks.logicng_sdd_bench.trackers;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.events.LngEvent;

import java.util.List;

public class HandlerGroup implements ComputationHandler {
    List<ComputationHandler> handlers;

    public HandlerGroup(final List<ComputationHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public boolean shouldResume(final LngEvent event) {
        for (final var handler : handlers) {
            if (!handler.shouldResume(event)) {
                return false;
            }
        }
        return true;
    }
}
