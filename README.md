# Example2Program

This project presents three simple program synthesizers implemented in Java, namely Top Down Enumerative Search,
Constraint-based Enumeration, and Divide-and-Conquer Enumeration, to illustrate basic ideas of program synthesis.

Overall, the Divide-and-Conquer Enumeration synthesizer has the best performance,
while the Constraint-based Enumeration synthesizer performs the worst.

## Context-Free Grammar (CFG)

In this program, the context-free grammar (CFG) is defined as follows:

```
E ::= Ite(B, E, E) | Add(E, E) | Multiply(E, E) | x | y | z | 1 | 2 | 3
B ::= Lt(E, E) | Eq(E, E) | And(B, B) | Or(B, B) | Not(B)
```

Our synthesizers employ the programming by example (PBE) technique. Thus, a list of input-output examples
is required for the synthesizers to work.

A sample of input-output examples is provided in the `examples.txt` file:

```
x=1, y=2, z=3 -> 6
x=3, y=2, z=2 -> 7
x=2, y=3, z=4 -> 9
```

## Usage

The program has been tested on Ubuntu 20.04 and MacOS and requires Java 11 and Maven to function.

### Step-by-step execution

You can run the program using the following commands:

1. Compile the program
```sh
$ mvn package
```

2. Execute the program with the path to the example file (e.g. `examples.txt`) and the synthesizer choice (optional).
```sh
$ java -cp lib:target/synth-1.0.jar synth.Main examples.txt 
```

By default, the Top Down Enumerative Search synthesizer is used, to use other synthesizers, please refer to the below commands

- To use the Constraint-based Enumeration synthesizer:
```sh
$ export LD_LIBRARY_PATH=./lib # For MacOS, replace LD_LIBRARY_PATH with DYLD_LIBRARY_PATH
$ java -cp lib/*:target/synth-1.0.jar synth.Main examples.txt constraint-based
```

- To use the Divide-and-Conquer Enumeration synthesizer:
```sh
$ java -cp lib:target/synth-1.0.jar synth.Main examples.txt divide-conquer
```

### Automated script

To streamline the process, there is an automated script named `synth.sh`, located in the main directory, to
handle both program compilation and execution. Instead of manually performing multiple steps, you can
run the program with a single command:

```sh
$ ./synth.sh <your_synthesizer_choice>
```

## Evaluation

To evaluate the performance of the synthesizers, 15 benchmarks are used with various degrees of complexity.
The details of each benchmark can be found under the `benchmarks` folder.

To run an end-to-end evaluation of three synthesizers on all 15 benchmarks, use the following command:
```sh
$ ./eval.sh
```
