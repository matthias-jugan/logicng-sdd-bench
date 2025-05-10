package com.booleworks.logicng_sdd_bench.trackers;

import com.booleworks.logicng.handlers.events.LngEvent;

public record BenchmarkEvent(String desc) implements LngEvent {
    public static final BenchmarkEvent START_EXPERIMENT = new BenchmarkEvent("Start benchmark");
    public static final BenchmarkEvent COMPLETED_EXPERIMENT = new BenchmarkEvent("End benchmark");
    public static final BenchmarkEvent ABORTED_EXPERIMENT = new BenchmarkEvent("Aborted benchmark");
}
