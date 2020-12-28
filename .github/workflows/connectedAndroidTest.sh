#!/usr/bin/env bash

for i in 1; do
    adb shell am force-stop org.opendatakit.tables
    adb shell am force-stop org.opendatakit.survey
    adb shell am force-stop org.opendatakit.services
    adb shell rm -rf /sdcard/opendatakit
    ../app-designer/node_modules/.bin/grunt --gruntfile ../app-designer/Gruntfile.js adbpush

    ./gradlew --stacktrace --rerun-tasks clean connectedSnapshotUitestDebugAndroidTest && TEST_STATUS=0 && break
    TEST_STATUS=$?

    sleep 5
done

adb pull /sdcard/opendatakit/default/output/logging default-output-logging
adb pull /sdcard/tables-screenshot-* .
exit $TEST_STATUS
