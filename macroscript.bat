@echo off
rem Starts MacroScript IDE on Windows. Build it first with: mvnw.cmd package
rem "compile" and "decompile" run in this console, for example: macroscript.bat compile examples
set JAR=%~dp0target\macroscript-ide-1.0.0.jar
if "%~1"=="compile" goto console
if "%~1"=="decompile" goto console
start "" javaw -jar "%JAR%" %*
goto :eof
:console
java -jar "%JAR%" %*
