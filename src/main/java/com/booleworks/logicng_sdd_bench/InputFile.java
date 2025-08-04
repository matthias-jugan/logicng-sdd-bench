package com.booleworks.logicng_sdd_bench;

import java.io.File;

public record InputFile(String name, File file, Category category, InputFormat format) {

    public enum Category {
        HANDMADE,
        CIRCUIT,
        CONFIGURATION,
        RANDOM,
        PLANING,
        OTHER
    }

    public enum InputFormat {
        PDF,
        DIMACS,
        ARBITRARY,
        EXPORT
    }

    @Override
    public String toString() {
        return "Input{" +
                "name='" + name + '\'' +
                ", file='" + file + '\'' +
                ", category=" + category +
                ", format=" + format +
                '}';
    }
}
