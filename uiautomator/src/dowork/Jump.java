package dowork;

import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;
import com.xing.jump.JumpTest;

public class Jump extends UiAutomatorTestCase {
    public void test() throws UiObjectNotFoundException {
        new JumpTest(getUiDevice()).doMission();
    }
}
