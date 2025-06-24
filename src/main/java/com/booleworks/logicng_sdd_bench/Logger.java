package com.booleworks.logicng_sdd_bench;

import com.booleworks.logicng_sdd_bench.experiments.results.ExperimentResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger implements AutoCloseable {
    private final BufferedWriter logW;
    private final BufferedWriter timesW;
    private final BufferedWriter summaryW;
    private final Verbosity consoleVerbosity;

    private Logger(final BufferedWriter logW, final BufferedWriter timesW, final BufferedWriter summaryW,
                   final Verbosity consoleVerbosity) {
        this.logW = logW;
        this.timesW = timesW;
        this.summaryW = summaryW;
        this.consoleVerbosity = consoleVerbosity;
    }

    public static Logger of(final File log, final File times, final File summary, final Verbosity verbosity)
            throws IOException {
        final BufferedWriter logW;
        if (log != null) {
            logW = new BufferedWriter(new FileWriter(log));
        } else {
            logW = null;
        }
        final BufferedWriter timesW;
        if (log != null) {
            timesW = new BufferedWriter(new FileWriter(times));
        } else {
            timesW = null;
        }
        final BufferedWriter summaryW;
        if (log != null) {
            summaryW = new BufferedWriter(new FileWriter(summary));
        } else {
            summaryW = null;
        }
        return new Logger(logW, timesW, summaryW, verbosity);
    }

    public void event(final String msg) {
        if (logW != null) {
            try {
                logW.write(msg);
                logW.newLine();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (consoleVerbosity.level >= Verbosity.ALL.level) {
            System.out.println(msg);
        }
    }

    public void error(final String msg) {
        if (logW != null) {
            try {
                logW.write(msg);
                logW.newLine();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (consoleVerbosity.level > Verbosity.SILENT_FORCE.level) {
            System.err.println(ConsoleColors.RED + msg + ConsoleColors.RESET);
        }
    }

    public void result(final InputFile file, final ExperimentResult result) {
        try {
            if (timesW != null) {
                timesW.write(String.format("%s,%s", file.name(), result.getEssentialsAsCsv()));
                timesW.newLine();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        final String msg = String.format("Obtained result: %s", result.getResult());
        try {
            if (logW != null) {
                logW.write(msg);
                logW.newLine();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        if (consoleVerbosity.level >= Verbosity.ALL.level) {
            System.out.println(msg);
        }
    }

    public void summary(final String msg) {
        try {
            if (summaryW != null) {
                summaryW.write(msg);
                summaryW.newLine();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        if (consoleVerbosity.level >= Verbosity.SUMMARY.level) {
            System.out.println(msg);
        }
    }

    @Override
    public void close() throws Exception {
        if (logW != null) {
            logW.flush();
            logW.close();
        }
        if (timesW != null) {
            timesW.flush();
            timesW.close();
        }
        if (summaryW != null) {
            summaryW.flush();
            summaryW.close();
        }
    }

    public enum Verbosity {
        SILENT_FORCE(0),
        SILENT(1),
        SUMMARY(2),
        ALL(4);

        private final int level;

        Verbosity(final int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }
}
