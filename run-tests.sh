#!/bin/bash

set -e

echo "Starting Java tests build..."

cd "$(dirname "$0")"

if [ ! -x gradlew ]; then
    echo "Making gradlew executable..."
    chmod +x gradlew
fi

echo "Cleaning previous builds..."
./gradlew clean

echo "Running tests..."
./gradlew test

echo "Build completed successfully!"
echo "Allure results saved to: allure-results/"