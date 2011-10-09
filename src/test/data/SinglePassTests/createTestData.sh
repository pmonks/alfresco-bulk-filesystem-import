#!/bin/sh
cd DirectoryStructureTests
./createTestData.sh

cd ../FileNameTests
./createTestData.sh

cd ../PermissionTests
./createTestData.sh

cd ../FileVolumeTests
./createTestData.sh

cd ../FileSizeTests
./createTestData.sh

cd ..