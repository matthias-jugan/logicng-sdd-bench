# Manual

This manual describes the setup for the different experiments of the thesis.  The structure follows the thesis's
structure.  Each section contains a brief explanation of the experiment's intent, refers to the important classes in the
code, and provides a command for reproducing the experiment.

The `/logs` directory contains all logs collected during the executions of the experiments used in the thesis. (Note
that some logs may differ in format as we have changed it at some point after we started to collect the data.)

## 7.2.1 SDD Compilation / Preprocessing

_Description_: Measures the compilation time and size of SDDs with and without preprocessing.

_Related Code_:

- `CompilationSetups.compilePreprocessing`
- `CompileSddExperiment`

_Setup_:

- Dataset: `suite1`
- Experiment: `preprocessing`
- Timeout: 300,000ms (5 minutes)

_Command_:

```
java -Xmx32G -jar logicng-sdd-bench.jar -- -f suite1 -E preprocessing -t 300000 -o auto -v all
```

## 7.2.2 SDD Compilation / Comparison of Knowledge Compilation Formats

_Description_:

- Measures the compilation time and size of SDDs with the bottom-up SDD compiler, the top-down SDD compiler, the
  bottom-up BDD compiler, and the top-down DNNF compiler.
- Measures the maximum heap size of the top-down SDD compiler, and the top-down DNNF compiler.

_Related Code_:

- `CompilationSetups.compileAll`
- `CompilationSetups.measureHeap`
- `CompileSddExperiment`
- `CompileSddBUExperiment`
- `CompileBddExperiment`
- `CompileDnnfExperiment`
- `CompileSddMeasureHeap`
- `CompileDnnfMeasureHeap`

_Setup_:

- Dataset: `suite1`
- Experiments: `compile-all` and `measure-heap`
- Timeout: 300,000ms (5 minutes)

_Commands_:

```
java -Xmx32G -jar logicng-sdd-bench.jar -- -f suite1 -E compile-all -t 300000 -o auto -v all
java -Xmx32G -jar logicng-sdd-bench.jar -- -f suite1 -E measure-heap -t 300000 -o auto -v all
```

## 7.3.1 SDD Functions / SDD Projection

_Description_: Measures the compilation time, projection time, original SDD size, and projected SDD size for real and
random projection problems.

_Related Code_:

- TODO

_Setup_:

- Datasets:
    - Real: `suite1/automotive/auto_c001_dn`, `suite1/automotive/auto_cn_d001`, `suite1/automotive/auto_cn_dn`
    - Random: `suite1/`
- Experiments: `project-real` and `project-random`
- Timeout: 300,000ms (5 minutes)

_Commands_:

```
java -Xmx32G -jar logicng-sdd-bench.jar -- -f suite1/automotive/auto_c001_dn -f suite1/automotive/auto_cn_d001 -f suite1/automotive/auto_cn_dn -E project-real -t 300000 -o auto -v all
java -Xmx32G -jar logicng-sdd-bench.jar -- -f suite1 -E project-random -t 300000 -o auto -v all
```

## 7.3.2 SDD Functions / SDD Model Enumeration

_Description_: Performs projected model enumeration on real problems. Samples the time after every 100 models enumerated
by the two enumeration algorithms. One experiment with additional variables, and one without additional variables.

_Related Code_:

- `ModelEnumerationSetups.projectedModelEnumerationReal`
- `EnumerationSDDExperiment`
- `EnumerationAdvExperiment`

_Setup_:

- Datasets: `suite1/automotive/auto_c001_dn`, `suite1/automotive/auto_cn_d001`, `suite1/automotive/auto_cn_dn`
- Experiments: `projected-model-enumeration-real` and `projected-model-enumeration-random`
- Timeout: 300,000ms (5 minutes)
- `--args with-add-vars` (for the variant with additional variables)

_Commands_:

```
java -Xmx32G -jar logicng-sdd-bench.jar -- -f suite1/automotive/auto_c001_dn -f suite1/automotive/auto_cn_d001 -f suite1/automotive/auto_cn_dn -E projected-model-enumeration-real -t 300000 -o auto -v all
java -Xmx32G -jar logicng-sdd-bench.jar -- -f suite1/automotive/auto_c001_dn -f suite1/automotive/auto_cn_d001 -f suite1/automotive/auto_cn_dn -E projected-model-enumeration-real -t 300000 -o auto -v all --args with-add-vars
```

## 7.3.3 SDD Functions / SDD Model Counting

_Description_:

1. Model Counting with SDDs and DNNFs on all problems
2. Projected Model Counting on real projection problems

_Related Code_:

- `ModelCountingSetups.projectedModelCounting`
- `ModelCountingSetups.modelCounting`
- `CountModelsDnnfExperiment`
- `CountModelsSddExperiment`
- `PmcNaiveSddExperiment`

_Setup_:

- Datasets:
    - Model Counting:`suite1/`, excludes `suite1/mercedes/C220_FW` and `suite1/mercedes/C168_FW`
    - Projected Model Counting: `suite1/automotive/auto_c001_dn`, `suite1/automotive/auto_cn_d001`,
      `suite1/automotive/auto_cn_dn`
- Experiments: `model-counting` and `projected-model-counting`
- Timeout: 300,000ms (5 minutes)

_Commands_:

```
java -Xmx32G -jar logicng-sdd-bench.jar -- -f suite1 -e suite1/mercedes/C220_FW -e suite1/mercedes/C168_FW -E model-counting -t 300000 -o auto -v all
java -Xmx32G -jar logicng-sdd-bench.jar -- -f suite1/automotive/auto_c001_dn -f suite1/automotive/auto_cn_d001 -f suite1/automotive/auto_cn_dn -E projected-model-counting -t 300000 -o auto -v all
```

## 7.4 Projected SDD Compilation

_Description_: Runs projected compilation and normal projection on real and random projection problems.

_Related Code_:

- `ProjectedCompilationSetups.projectedCompileReal`
- `ProjectedCompilationSetups.projectedCompileRandom`
- `PcSddExperiment`
- `ExElimSddExperiment`

_Setup_:

- Datasets:
    - Real: `suite1/automotive/auto_c001_dn`, `suite1/automotive/auto_cn_d001`, `suite1/automotive/auto_cn_dn`
    - Random: `suite1/`
- Experiments: `projected-compile-real` and `projected-compile-random`
- Timeout: 300,000ms (5 minutes)

_Commands_:

```
java -Xmx32G -jar logicng-sdd-bench.jar -- -f suite1/automotive/auto_c001_dn -f suite1/automotive/auto_cn_d001 -f suite1/automotive/auto_cn_dn -E projected-compile-real -t 300000 -o auto -v all
java -Xmx32G -jar logicng-sdd-bench.jar -- -f suite1 -E projected-compile-random -t 300000 -o auto -v all
```

## 7.5 SDD Minimization

_Description_: Runs SDD minimization with three different search strategies and tracks every improvement to the SDD.

_Related Code_:

- `MinimizationSetups.minimize`
- `MinimizeSddExperiment`

_Setup_:

- Datasets: `setups/minimization_selection.files`:
    - `suite1/automotive/auto_c001_d001/auto0_s0_c01_d01_v08.txt`
    - `suite1/automotive/auto_cn_d001/auto0_s2_c40_d01.txt`
    - `suite1/automotive/auto_c001_dn/auto1_s1_c01_d10.txt`
    - `suite1/automotive/auto_cn_dn/auto1_s2_c05_d05.txt`
    - `suite1/automotive/auto_cn_d001/auto1_s2_c60_d01.txt`
    - `suite1/automotive/auto_c001_dn/auto5_s1_c01_d40.txt`
    - `suite1/automotive/auto_cn_dn/auto5_s2_c25_d25.txt`
    - `suite1/automotive/auto_cn_dn/auto6_s2_c15_d15.txt`
    - `suite1/mercedes/C171_FR.cnf`
    - `suite1/mercedes/C203_FW.cnf`
- Experiments: `minimize`
- Timeout: 1,800,000ms (30 minutes)

_Commands_:

```
java -Xmx32G -jar logicng-sdd-bench.jar -- -S setups/minimization_selection.files -E minimize -t 1800000 -o auto -v all
```
