@echo off
rem This script hasn't been tested, as I no longer have ready access to a
rem Windows machine.

setlocal enableextensions

cd DirectoryStructure
call createTestData.cmd

cd ..\FileNameTests
call createTestData.cmd

rem TODO: implement this
rem cd ..\PermissionTests
rem call createTestData.cmd

cd ..\FileSizeTests
call createTestData.cmd

cd ..\FileVolumeTests
call createTestData.cmd

cd ..