#!/bin/bash

BENCHMARK_FOLDER="./benchmarks"

for file in "$BENCHMARK_FOLDER"/*.txt; do
    if [[ -f "$file" ]]; then
        echo "---------------------------------"
        echo "Examples in $file:"
        echo "$(<$file)"

        echo -e "\nSyntheszing a program using the top-down enumerative search approach..."
        java -cp lib:target/synth-1.0.jar synth.Main $file

        echo -e "\nSyntheszing a program using the constraint-based enumeration approach..."
        export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:./lib
        java -cp lib/*:target/synth-1.0.jar synth.Main $file constraint-based

        echo -e "\nSyntheszing a program using the divide-and-conquer enumeration approach..."
        java -cp lib:target/synth-1.0.jar synth.Main $file divide-conquer
        echo "---------------------------------"
    fi
done
