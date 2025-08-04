package com.booleworks.logicng_sdd_bench;

public sealed interface Filter permits Filter.Category, Filter.File, Filter.Name, Filter.Exclude {
    boolean matches(InputFile input);

    record Category(InputFile.Category category) implements Filter {

        @Override
        public boolean matches(final InputFile input) {
            return input.category() == category;
        }
    }

    record File(java.io.File path) implements Filter {

        @Override
        public boolean matches(final InputFile input) {
            return input.file().getAbsolutePath().startsWith(path.getAbsolutePath());
        }
    }

    record Exclude(java.io.File path) implements Filter {

        @Override
        public boolean matches(final InputFile input) {
            return input.file().getAbsolutePath().startsWith(path.getAbsolutePath());
        }
    }

    record Name(String name) implements Filter {

        @Override
        public boolean matches(final InputFile input) {
            return input.name().equals(name);
        }
    }
}
