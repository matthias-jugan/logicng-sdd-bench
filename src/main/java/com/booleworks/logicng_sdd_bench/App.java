package com.booleworks.logicng_sdd_bench;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.NopHandler;
import com.booleworks.logicng.handlers.TimeoutHandler;
import com.booleworks.logicng_sdd_bench.setups.Setups;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Hello world!
 */
public class App {
    private final static String BENCHMARK_PATH = "benchmarks";

    private static void printUsage() {
        System.err.println("-S <file>: Setup file");
        System.err.println("-E <name>: Experiment");
        System.err.println("-B <path>: Path to Benchmark files");
        System.err.println();
        System.err.println("-t <timout>: Timeout in ms");
        System.err.println("-c (<category> )+: Add files of category");
        System.err.println("-f (<path> )+: Add files at path");
        System.err.println("-n (<file> )+: Add file with name");
        System.err.println();
        System.err.println("-v <\"all\"|\"summary\"|\"silent\"|\"silent-force\">: Print verbosity. default: silent");
        System.err.println("-o <\"auto\"|\"none\">: Generates new log, summary, and time files. default: auto");
        System.err.println("-ol <file>: Pass log file");
        System.err.println("-os <file>: Pass summary file");
        System.err.println("-ot <file>: Pass time file");
        System.err.println();
        System.err.println("List of all experiments:");
        for (final String k : Setups.SETUPS.keySet()) {
            System.err.printf("- %s\n", k);
        }
    }

    public static void main(final String[] args) throws IOException {
        final LocalDateTime startTime = LocalDateTime.now();
        final Map<String, List<String>> arguments = groupArguments(List.of(args));

        if (arguments.containsKey("S") && !arguments.get("S").isEmpty()) {
            final Map<String, List<String>> setupArguments = readSetupFile(new File(arguments.get("S").get(0)));
            setupArguments.forEach((k, v) -> {
                if ((k.equals("c") || k.equals("f") || k.equals("n"))) {
                    if (!arguments.containsKey("c") && !arguments.containsKey("f") && !arguments.containsKey("n")) {
                        arguments.put(k, v);
                    }
                } else if (!arguments.containsKey(k)) {
                    arguments.put(k, v);
                }
            });
        }

        if (!arguments.containsKey("E") || arguments.get("E").isEmpty()) {
            System.err.println("No experiment was specified");
            printUsage();
            return;
        }
        final String experimentName = arguments.get("E").get(0);
        final Setups.RunSetup experiment = Setups.SETUPS.get(experimentName);
        if (experiment == null) {
            System.err.println("Unknown experiment " + experimentName);
            printUsage();
            return;
        }

        String benchmarkPath = BENCHMARK_PATH;
        if (arguments.containsKey("B")) {
            benchmarkPath = arguments.get("B").getFirst();
        }

        File logFile = null;
        File summaryFile = null;
        File timesFile = null;
        if (!arguments.containsKey("o") || arguments.get("o").get(0).equals("auto")) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
            final String prefix = String.format("%s_%s_", startTime.format(formatter), experimentName);
            final File resDir = new File("benchmark_results");
            logFile = new File("benchmark_results/" + prefix + "log.txt");
            summaryFile = new File("benchmark_results/" + prefix + "summary.txt");
            timesFile = new File("benchmark_results/" + prefix + "times.txt");
            resDir.mkdirs();
            logFile.createNewFile();
            timesFile.createNewFile();
            summaryFile.createNewFile();
        }
        if (arguments.containsKey("ol") && !arguments.get("ol").isEmpty()) {
            logFile = new File(arguments.get("ol").getFirst());
        }
        if (arguments.containsKey("os") && !arguments.get("os").isEmpty()) {
            summaryFile = new File(arguments.get("os").getFirst());
        }
        if (arguments.containsKey("ot") && !arguments.get("ot").isEmpty()) {
            timesFile = new File(arguments.get("ot").getFirst());
        }

        Logger.Verbosity verbosity = Logger.Verbosity.SUMMARY;
        if (arguments.containsKey("v") && !arguments.get("v").isEmpty()) {
            verbosity = switch (arguments.get("v").getFirst()) {
                case "all" -> Logger.Verbosity.ALL;
                case "summary" -> Logger.Verbosity.SUMMARY;
                case "silent" -> Logger.Verbosity.SILENT;
                case "silent-force" -> Logger.Verbosity.SILENT_FORCE;
                default -> throw new IllegalStateException("Unexpected verbosity: " + arguments.get("v").getFirst());
            };
        }

        final List<Filter> filters = collectFilters(arguments, benchmarkPath);
        long timeout = -1;
        if (arguments.containsKey("t")) {
            final long t = Long.parseLong(arguments.get("t").getFirst());
            if (t > 0) {
                timeout = t;
            }
        }
        final Settings settings = new Settings(timeout);

        List<InputFile> inputs = scanInputs(benchmarkPath);
        if (!filters.isEmpty()) {
            inputs = filterInputs(filters, inputs);
        }

        final long ft = timeout;
        final Supplier<ComputationHandler> handler =
                ft == -1 ? NopHandler::get : () -> new TimeoutHandler(ft);
        try (final Logger logger = Logger.of(logFile, timesFile, summaryFile, verbosity)) {
            log(logger, "=== Setup ===");
            log(logger, String.format("Time: %s", startTime.format(DateTimeFormatter.ISO_DATE_TIME)));
            log(logger, String.format("Experiment: %s", experimentName));
            log(logger, "");
            log(logger, String.format("Input source: %s", benchmarkPath));
            log(logger, String.format("Files: %d", inputs.size()));
            log(logger, String.format("Filters: %s", filters));
            log(logger, "");
            log(logger, String.format("Timeout: %dms", settings.timeout()));
            log(logger, String.format("Heap Size: %dMB", Runtime.getRuntime().maxMemory() / (1024 * 1024)));
            log(logger, "======");
            experiment.run(inputs, settings, logger, handler);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void log(final Logger logger, final String msg) {
        logger.summary(msg);
        logger.event(msg);
    }

    public static ArrayList<InputFile> scanInputs(final String path) {
        final ArrayList<InputFile> inputs = new ArrayList<>();
        final File dir = new File(path);
        assert dir.isDirectory();
        for (final File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                scanSubDirectory(file, categoryFromName(file.getName()), inputs);
            } else {
                if (!file.getName().startsWith("ignore")) {
                    addInput(file, InputFile.Category.OTHER, inputs);
                }
            }
        }
        return inputs;
    }

    private static void scanSubDirectory(final File dir, final InputFile.Category category,
                                         final ArrayList<InputFile> inputs) {
        for (final File file : Objects.requireNonNull(dir.listFiles())) {
            if (!file.getName().startsWith("ignore")) {
                if (file.isDirectory()) {
                    scanSubDirectory(file, category, inputs);
                } else {
                    addInput(file, category, inputs);
                }
            }
        }
    }

    private static void addInput(final File file, final InputFile.Category category, final List<InputFile> inputs) {
        if (file.getName().startsWith("ignore") || file.getName().startsWith(".")) {
            return;
        }
        final InputFile.InputFormat format = formatFromName(file.getName());
        inputs.add(new InputFile(file.getName(), file, category, format));
    }

    private static List<InputFile> filterInputs(final Collection<Filter> filters, final Collection<InputFile> inputs) {
        return inputs.stream().filter(i -> filters.stream().anyMatch(f -> f.matches(i))).collect(Collectors.toList());
    }


    public static InputFile.Category categoryFromName(String name) {
        name = name.toLowerCase().trim();
        if (name.startsWith("handmade")) {
            return InputFile.Category.HANDMADE;
        } else if (name.startsWith("circuit")) {
            return InputFile.Category.CIRCUIT;
        } else if (name.startsWith("config")) {
            return InputFile.Category.CONFIGURATION;
        } else if (name.startsWith("random")) {
            return InputFile.Category.RANDOM;
        } else if (name.startsWith("plan")) {
            return InputFile.Category.PLANING;
        } else if (name.startsWith("other")) {
            return InputFile.Category.OTHER;
        } else {
            return null;
        }
    }

    public static InputFile.InputFormat formatFromName(String name) {
        name = name.toLowerCase().trim();
        if (name.endsWith(".cnf") || name.endsWith(".dimacs")) {
            return InputFile.InputFormat.DIMACS;
        } else {
            return InputFile.InputFormat.ARBITRARY;
        }
    }

    public static List<Filter> collectFilters(final Map<String, List<String>> args, final String benchmarkPath) {
        final List<Filter> filters = new ArrayList<>();
        if (args.containsKey("c")) {
            for (final String c : args.get("c")) {
                final InputFile.Category cp = categoryFromName(c);
                if (cp != null) {
                    filters.add(new Filter.Category(cp));
                } else {
                    System.err.println("Ignoring unknown Category: " + c);
                }
            }
        }
        if (args.containsKey("f")) {
            for (final String f : args.get("f")) {
                filters.add(new Filter.File(new File(benchmarkPath + "/" + f)));
            }
        }
        if (args.containsKey("n")) {
            for (final String n : args.get("n")) {
                filters.add(new Filter.Name(n));
            }
        }
        return filters;
    }

    public static Map<String, List<String>> groupArguments(final Collection<String> args) {
        final Map<String, List<String>> res = new HashMap<>();
        List<String> ps = new ArrayList<>();
        String c = null;
        for (final String arg : args) {
            if (arg.startsWith("-")) {
                put(res, c, ps);
                ps = new ArrayList<>();
                c = arg.substring(1);
            } else {
                ps.add(arg);
            }
        }
        put(res, c, ps);
        return res;
    }

    public static Map<String, List<String>> readSetupFile(final File file) {
        final List<String> args = new ArrayList<>();
        try (final BufferedReader br = new BufferedReader(new FileReader(file))) {
            while (br.ready()) {
                final String line = br.readLine().trim();
                args.addAll(List.of(line.split(" ")));
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return groupArguments(args);
    }

    private static void put(final Map<String, List<String>> res, final String c, final List<String> ps) {
        if (c != null) {
            if (res.containsKey(c)) {
                res.get(c).addAll(ps);
            } else {
                res.put(c, ps);
            }
        }
    }
}
