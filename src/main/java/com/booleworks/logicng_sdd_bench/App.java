package com.booleworks.logicng_sdd_bench;

import com.booleworks.logicng.handlers.ComputationHandler;
import com.booleworks.logicng.handlers.NopHandler;
import com.booleworks.logicng.handlers.TimeoutHandler;

import java.io.File;
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

    public static void main(final String[] args) {
        final Map<String, List<String>> arguments = groupArguments(args);

        String benchmarkPath = BENCHMARK_PATH;
        if (arguments.containsKey("B")) {
            benchmarkPath = arguments.get("B").getFirst();
        }

        final List<Filter> filters = collectFilters(arguments, benchmarkPath);
        long timeout = -1;
        if (arguments.containsKey("t")) {
            final long t = Long.parseLong(arguments.get("t").getFirst());
            if (t > 0) {
                timeout = t;
            }
        }
        List<InputFile> inputs = scanInputs(benchmarkPath);
        if (!filters.isEmpty()) {
            inputs = filterInputs(filters, inputs);
        }

        final long ft = timeout;
        final Supplier<ComputationHandler> handler =
                ft == -1 ? NopHandler::get : () -> new TimeoutHandler(ft);
        final var results = Setups.compilation(inputs, handler);
        inputs.forEach(i -> {
            if (results.containsKey(i)) {
                System.out.println(i.name() + " -> " + results.get(i).getResult());
            }
        });
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
            if (file.isDirectory()) {
                scanSubDirectory(file, category, inputs);
            } else {
                if (!file.getName().startsWith("ignore")) {
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

    public static Map<String, List<String>> groupArguments(final String[] args) {
        final Map<String, List<String>> res = new HashMap<>();
        List<String> ps = new ArrayList<>();
        String c = null;
        for (final String arg : args) {
            if (arg.startsWith("-")) {
                if (c != null) {
                    res.put(c, ps);
                    ps = new ArrayList<>();
                }
                c = arg.substring(1);
            } else {
                ps.add(arg);
            }
        }
        if (c != null) {
            res.put(c, ps);
            ps = new ArrayList<>();
        }
        return res;
    }
}
