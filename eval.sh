#!/bin/bash

echo -e "\n------ RUNNING EVALUATION ------\n"

MVN_OUTPUT=$(mvn clean package 2>&1)
if [ $? -ne 0 ]; then
    echo "$MVN_OUTPUT"
    echo "Compilation failed."
    exit 1  # Exit the script with an error code
fi

# Set library path for Linux or MacOS
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:./lib
elif [[ "$OSTYPE" == "darwin"* ]]; then
    export DYLD_LIBRARY_PATH=$DYLD_LIBRARY_PATH:./lib
else
    echo "Unsupported OS: $OSTYPE"
    exit 1
fi

BENCHMARK_FOLDER="./benchmarks"
for file in "$BENCHMARK_FOLDER"/*.txt; do
    if [[ -f "$file" ]]; then
        echo "---------------------------------"
        echo "Examples in $file:"
        echo "$(<$file)"

        echo -e "\nSynthesizing a program using the top-down enumerative search approach..."
        java -cp lib:target/synth-1.0.jar synth.Main $file

        echo -e "\nSynthesizing a program using the constraint-based enumeration approach..."
        java -cp lib/*:target/synth-1.0.jar synth.Main $file constraint-based

        echo -e "\nSynthesizing a program using the divide-and-conquer enumeration approach..."
        java -cp lib:target/synth-1.0.jar synth.Main $file divide-conquer
        echo "---------------------------------"
    fi
done
