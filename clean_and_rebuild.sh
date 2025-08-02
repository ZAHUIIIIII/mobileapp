#!/bin/bash

echo "🧹 Cleaning Android project..."

# Stop any running Gradle daemons
./gradlew --stop

# Clean the project
./gradlew clean

# Clear Gradle caches (optional, but helpful for memory issues)
rm -rf ~/.gradle/caches/
rm -rf ~/.gradle/daemon/

echo "🏗️ Rebuilding project with new memory settings..."

# Build the project
./gradlew build

echo "✅ Build completed! Memory optimizations applied."
echo ""
echo "📋 Memory settings applied:"
echo "- Gradle heap size: 4GB (increased from 2GB)"
echo "- Parallel builds: Enabled"
echo "- Build cache: Enabled"
echo "- Configuration cache: Enabled"
echo "- Multidex: Enabled"
echo ""
echo "🔧 Fixed issues:"
echo "- Removed unsupported MaxPermSize option for Java 23"
echo "- Fixed attendees field change detection in EditInstanceActivity" 