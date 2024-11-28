# Example2Program

This project presents three simple program synthesizers implemented in Java, namely Top Down Enumerative Search,
Constraint-based Enumeration, and Divide-and-Conquer Enumeration, to illustra basic ideas of program synthesis.

Overall, the Divide-and-Conquer Enumeration synthesizer has the best performance,
while the Constraint-based Enumeration synthesizer performs the worst.

## Context-Free Grammar (CFG)

In this program, the context-free grammar (CFG) is defined as follows:

```
E ::= Ite(B, E, E) | Add(E, E) | Multiply(E, E) | x | y | z | 1 | 2 | 3
B ::= Lt(E, E) | Eq(E, E) | And(B, B) | Or(B, B) | Not(B)
```

Our synthesizers employs the programming by example (PBE) technique. Thus, a list of input-output examples
is required for the synthesizers to work.

A sample of input-output examples is provided in the `examples.txt` file:

```
x=1, y=2, z=3 -> 6
x=3, y=2, z=2 -> 7
x=2, y=3, z=4 -> 9
```

## Usage

Java 11 and Maven are required. The program is tested on Ubuntu and MacOS.

1. Compile the program
```sh
$ mvn package
```

2. Execute the program with path to the example file (i.e. `examples.txt`) and the synthesizer choice (optional).
```sh
$ java -cp lib:target/synth-1.0.jar synth.Main examples.txt 
```

By default, the Top Down Enumerative Search synthesizer is used, to use other synthesizers, please refer to the below commands

- For Constraint-based Enumeration:
```sh
$ export LD_LIBRARY_PATH=./lib # For MacOS, replace LD_LIBRARY_PATH with DYLD_LIBRARY_PATH
$ java -cp lib/*:target/synth-1.0.jar synth.Main examples.txt constraint-based
```

- For Divide-and-Conquer Enumeration:
```sh
$ java -cp lib:target/synth-1.0.jar synth.Main examples.txt divide-conquer
```
