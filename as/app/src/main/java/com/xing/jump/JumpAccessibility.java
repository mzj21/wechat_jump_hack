package com.xing.jump;

import android.accessibilityservice.AccessibilityService;
import android.app.Service;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JumpAccessibility extends AccessibilityService {
    private static final String TAG = "JumpAccessibility";
    private Random random;
    private List<AccessibilityNodeInfo> list;
    private Point point;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        point = getRealSize();
        random = new Random();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        //根据事件回调类型进行处理
        switch (eventType) {
            //当窗口的状态发生改变时
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (existsByText("跳一跳")) {
                    sleep(1000);
                    getAccessibilityNodeInfoByText("跳一跳").getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                    sleep(5000);
                    CmdUtil.execCmd("input tap " + getRealSize().x / 2 + " " + getRealSize().y * 1920 / getRealSize().y * 0.796, false);
                }
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }

    /**
     * 获取屏幕精确尺寸
     */
    protected Point getRealSize() {
        Display display = ((WindowManager) getApplicationContext().getSystemService(Service.WINDOW_SERVICE)).getDefaultDisplay();
        Point p = new Point();
        display.getRealSize(p);
        return p;
    }

    /**
     * 休眠
     *
     * @param millis 休眠时间(单位毫秒)
     */
    public void sleep(long millis) {
        SystemClock.sleep(millis);
    }

    protected boolean existsByText(String... strings) {
        for (String str : strings) {
            if (getAccessibilityNodeInfoByText(str) != null) {
                return true;
            }
        }
        return false;
    }

    protected boolean existsByID(String... IDs) {
        for (String id : IDs) {
            if (getAccessibilityNodeInfoByID(id) != null) {
                return true;
            }
        }
        return false;
    }

    protected Rect getRectByID(String id) {
        Rect rect = new Rect();
        getAccessibilityNodeInfo(id, 0, true).getBoundsInScreen(rect);
        return rect;
    }

    protected Rect getRectByText(String text) {
        Rect rect = new Rect();
        getAccessibilityNodeInfo(text, 0, false).getBoundsInScreen(rect);
        return rect;
    }

    protected AccessibilityNodeInfo getAccessibilityNodeInfoByID(String id) {
        return getAccessibilityNodeInfo(id, 0, true);
    }

    protected AccessibilityNodeInfo getAccessibilityNodeInfoByID(String id, int instance) {
        return getAccessibilityNodeInfo(id, instance, true);
    }

    protected AccessibilityNodeInfo getAccessibilityNodeInfoByText(String text) {
        return getAccessibilityNodeInfo(text, 0, false);
    }

    protected AccessibilityNodeInfo getAccessibilityNodeInfoByText(String text, int instance) {
        return getAccessibilityNodeInfo(text, instance, false);
    }

    protected AccessibilityNodeInfo getAccessibilityNodeInfo(String str, int instance, boolean isID) {
        List<AccessibilityNodeInfo> list = getAccessibilityNodeInfo(str, isID);
        if (list != null && list.size() > 0 && list.size() > instance) {
            return list.get(instance);
        }
        return null;
    }

    protected List<AccessibilityNodeInfo> getAccessibilityNodeInfo(String str, boolean isID) {
        AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
        if (rootNodeInfo != null) {
            if (isID) {
                list = rootNodeInfo.findAccessibilityNodeInfosByViewId(str);
                for (int i = 0; i < list.size(); i++) {
                    if (!list.get(i).getViewIdResourceName().equals(str)) {
                        list.remove(i);
                    }
                }
            } else {
                list = rootNodeInfo.findAccessibilityNodeInfosByText(str);
                for (int i = 0; i < list.size(); i++) {
                    if (!String.valueOf(list.get(i).getText()).equals(str)) {
                        list.remove(i);
                    }
                }
            }
            if (list != null && list.size() > 0) {
                return list;
            }
            rootNodeInfo.recycle();
        }
        return null;
    }

    protected boolean existsByClassName(String... args) {
        for (String className : args) {
            AccessibilityNodeInfo info = getAccessibilityNodeInfoByClassName(className);
            if (info != null && String.valueOf(info.getClassName()).contains(className)) {
                return true;
            }
        }
        return false;
    }

    protected Rect getRectByClassName(String className) {
        Rect rect = new Rect();
        getAccessibilityNodeInfoByClassName(className).getBoundsInScreen(rect);
        return rect;
    }

    protected AccessibilityNodeInfo getAccessibilityNodeInfoByClassName(String className) {
        List<AccessibilityNodeInfo> list = getRootNodeInfoList();
        for (AccessibilityNodeInfo info : list) {
            if (String.valueOf(info.getClassName()).equals(className)) {
                return info;
            }
        }
        return null;
    }

    protected AccessibilityNodeInfo getAccessibilityNodeInfoByClassNameAndInstance(String className, int instance) {
        List<AccessibilityNodeInfo> list = getRootNodeInfoList();
        List<AccessibilityNodeInfo> l = new ArrayList<>();
        for (AccessibilityNodeInfo info : list) {
            if (String.valueOf(info.getClassName()).equals(className)) {
                l.add(info);
            }
        }
        if (l.size() > 0 && l.size() > instance) {
            return l.get(instance);
        }
        return null;
    }

    protected List<AccessibilityNodeInfo> getRootNodeInfoList() {
        AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
        list = new ArrayList<>();
        dumpNodeRec(rootNodeInfo);
        if (rootNodeInfo != null) {
            rootNodeInfo.recycle();
        }
        return list;
    }

    /**
     * 遍历添加所有可见元素
     *
     * @param rootNodeInfo 根元素
     */
    private void dumpNodeRec(AccessibilityNodeInfo rootNodeInfo) {
        if (rootNodeInfo != null) {
            int count = rootNodeInfo.getChildCount();
            for (int i = 0; i < count; i++) {
                AccessibilityNodeInfo child = rootNodeInfo.getChild(i);
                if (child != null) {
                    if (child.isVisibleToUser()) {
                        list.add(child);
                        dumpNodeRec(child);
                    }
                }
            }
        }
    }
}
