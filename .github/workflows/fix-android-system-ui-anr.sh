#!/usr/bin/env bash

# From https://gist.github.com/d4vidi/7862d60375b38f8970f824c4ce0ad2a9

echo ""
echo "[Waiting for launcher to start]"
LAUNCHER_READY=
while [[ -z ${LAUNCHER_READY} ]]; do
    UI_FOCUS=`adb shell dumpsys window windows 2>/dev/null | grep -i mCurrentFocus`
    echo "(DEBUG) Current focus: ${UI_FOCUS}"

    case ${UI_FOCUS} in
    *"Launcher"* | *"launcher"*)
        LAUNCHER_READY=true
    ;;
    "")
        echo "Waiting for window service..."
        sleep 3
    ;;
    *"ConversationListActivity"*)
        echo "Detected ConversationListActivity, Dismissing..."
        adb shell input keyevent KEYCODE_HOME
    ;;
    *"Not Responding"*)
        echo "Detected an ANR! Dismissing..."
        adb shell input keyevent KEYCODE_DPAD_DOWN
        adb shell input keyevent KEYCODE_DPAD_DOWN
        adb shell input keyevent KEYCODE_ENTER
    ;;
    *)
        echo "Waiting for launcher..."
        sleep 3
    ;;
    esac
done

echo "Launcher is ready :-)"