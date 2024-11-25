#!/bin/bash

MVN_OUTPUT=$(mvn clean package 2>&1)
if [ $? -ne 0 ]; then
    echo "$MVN_OUTPUT"
    echo "Compilation failed."
    exit 1  # Exit the script with an error code
fi

# Set library path for Linux or MacOS
if [[ $1 == "constraint-based" ]]; then
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:./lib
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        export DYLD_LIBRARY_PATH=$DYLD_LIBRARY_PATH:./lib
    else
        echo "Unsupported OS: $OSTYPE"
        exit 1
    fi
fi

java -cp lib/*:target/synth-1.0.jar synth.Main examples.txt $1
