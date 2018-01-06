adb push ./Jump.jar /data/local/tmp
adb shell uiautomator runtest Jump.jar -c com.xing.jump.Jump -e defult true -e jumpTime 500