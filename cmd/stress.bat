@echo off
rem locate where the batch is running
for /f %%i in ("%0") do set curpath=%%~dpi
cd /d %curpath%
java %JAVA_OPTIONS% -cp "%CLASSPATH%;%curpath%sparql-query-bm-cli.jar" net.sf.sparql.benchmarking.commands.StressCommand %*
