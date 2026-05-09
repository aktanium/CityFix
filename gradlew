#!/usr/bin/env sh

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_SHARED_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper-shared.jar"
WRAPPER_CLI_JAR="$APP_HOME/gradle/wrapper/gradle-cli.jar"

if [ -n "$JAVA_HOME" ]; then
  JAVA_EXE="$JAVA_HOME/bin/java"
else
  JAVA_EXE="java"
fi

exec "$JAVA_EXE" -classpath "$WRAPPER_JAR:$WRAPPER_SHARED_JAR:$WRAPPER_CLI_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
