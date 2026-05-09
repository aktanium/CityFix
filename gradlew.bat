@echo off
setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_HOME=%DIRNAME%

set WRAPPER_JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
set WRAPPER_SHARED_JAR=%APP_HOME%gradle\wrapper\gradle-wrapper-shared.jar
set WRAPPER_CLI_JAR=%APP_HOME%gradle\wrapper\gradle-cli.jar

if defined JAVA_HOME (
  set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
  set JAVA_EXE=java.exe
)

"%JAVA_EXE%" -classpath "%WRAPPER_JAR%;%WRAPPER_SHARED_JAR%;%WRAPPER_CLI_JAR%" org.gradle.wrapper.GradleWrapperMain %*

endlocal
