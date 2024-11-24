#!/bin/bash

MVN_OUTPUT=$(mvn clean package 2>&1)
if [ $? -ne 0 ]; then
    echo "$MVN_OUTPUT"
    echo "Compilation failed."
    exit 1  # Exit the script with an error code
fi

export DYLD_LIBRARY_PATH=$DYLD_LIBRARY_PATH:./lib
java -cp lib/*:target/synth-1.0.jar synth.Main examples.txt $1
