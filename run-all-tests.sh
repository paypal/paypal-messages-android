#!/bin/bash

# Enhanced test runner script that optimizes test execution

echo "Running optimized test suite..."

# First run the regular tests (excluding memory-intensive tests)
echo -e "\n\n===== Running standard tests ====="
./gradlew :library:testDebugUnitTest

# Then run the memory-intensive tests separately
echo -e "\n\n===== Running memory-intensive tests ====="
./gradlew :library:testMemoryIntensiveClasses

echo -e "\n\n===== All tests completed ====="