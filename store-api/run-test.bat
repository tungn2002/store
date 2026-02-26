@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-21
cd /d %~dp0
call mvnw.cmd test -Dtest=AuthControllerUnitTest
