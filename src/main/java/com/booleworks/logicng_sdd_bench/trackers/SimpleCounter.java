package com.booleworks.logicng_sdd_bench.trackers;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.events.LngEvent;

public class SimpleCounter implements ComputationHandler {
    private final LngEvent event;
    private int counter = 0;

    public SimpleCounter(final LngEvent event) {
        this.event = event;
    }

    @Override
    public boolean shouldResume(final LngEvent event) {
        if (this.event.equals(event)) {
            ++counter;
        }
        return true;
    }

    public LngEvent getEvent() {
        return event;
    }

    public int getCounter() {
        return counter;
    }
}
