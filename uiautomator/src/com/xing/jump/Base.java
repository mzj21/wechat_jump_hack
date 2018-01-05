package com.xing.jump;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.SystemClock;
import android.view.Display;

import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

public class Base {
    protected final UiDevice device;
    protected final Random random;

    protected Base(UiDevice device) {
        this.device = device;
        random = new Random();
    }

    protected void swipe() {
        int startX = device.getDisplayWidth() / 2;
        int startY = device.getDisplayHeight() / 3;
        int endX = device.getDisplayWidth() / 2;
        int endY = device.getDisplayHeight() / 6 * 5;
        int step = 50;
        device.swipe(startX, startY, endX, endY, step);
    }

    /**
     * 点击
     *
     * @param obj    控件
     * @param millis 休眠时间，单位毫秒
     */
    protected void click(UiObject obj, long millis) throws Exception {
        if (!obj.exists() || obj.getVisibleBounds() == null) {
            return;
        }
        click(obj.getVisibleBounds(), millis);
    }

    /**
     * 点击
     *
     * @param rect   Rect
     * @param millis 休眠时间，单位毫秒
     */
    protected void click(Rect rect, long millis) throws Exception {
        click(rect.centerX(), rect.centerY(), millis);
    }

    /**
     * 点击
     *
     * @param x      X坐标
     * @param y      Y坐标
     * @param millis 休眠时间，单位毫秒
     */
    protected void click(int x, int y, long millis) throws Exception {
        click(x, y, 50, millis);
    }

    /**
     * 点击
     *
     * @param x         X坐标
     * @param y         Y坐标
     * @param touchTime 触碰时间，单位毫秒
     * @param millis    休眠时间，单位毫秒
     */
    protected void click(int x, int y, long touchTime, long millis) throws Exception {
        Class<?> InteractionControllerClass;
        Class<?> UiAutomatorBridgeClass;
        Method getAutomatorBridgeMethod;
        Method getInteractionControllerMethod;
        Method touchDownMethod;
        Method touchUpMethod;
        Method touchMoveMethod;

        getAutomatorBridgeMethod = device.getClass().getDeclaredMethod("getAutomatorBridge");
        getAutomatorBridgeMethod.setAccessible(true);
        Object UiAutomatorBridge = getAutomatorBridgeMethod.invoke(device);

        UiAutomatorBridgeClass = Class.forName("com.android.uiautomator.core.UiAutomatorBridge");
        getInteractionControllerMethod = UiAutomatorBridgeClass.getDeclaredMethod("getInteractionController");
        getInteractionControllerMethod.setAccessible(true);
        Object interactionController = getInteractionControllerMethod.invoke(UiAutomatorBridge);

        InteractionControllerClass = Class.forName("com.android.uiautomator.core.InteractionController");
        touchDownMethod = InteractionControllerClass.getDeclaredMethod("touchDown", int.class, int.class);
        touchDownMethod.setAccessible(true);
        touchMoveMethod = InteractionControllerClass.getDeclaredMethod("touchMove", int.class, int.class);
        touchMoveMethod.setAccessible(true);
        touchUpMethod = InteractionControllerClass.getDeclaredMethod("touchUp", int.class, int.class);
        touchUpMethod.setAccessible(true);

        touchDownMethod.invoke(interactionController, x, y);
        SystemClock.sleep(touchTime);
        touchUpMethod.invoke(interactionController, x, y);
        if (millis > 0) {
            SystemClock.sleep(millis);
        }
    }

    /**
     * 点击
     *
     * @param x         X坐标
     * @param y         Y坐标
     * @param touchTime 触碰时间，单位毫秒
     * @param millis    休眠时间，单位毫秒
     */
    protected void click_random(int x, int y, long touchTime, long millis) throws Exception {
        Class<?> InteractionControllerClass;
        Class<?> UiAutomatorBridgeClass;
        Method getAutomatorBridgeMethod;
        Method getInteractionControllerMethod;
        Method touchDownMethod;
        Method touchUpMethod;
        Method touchMoveMethod;

        getAutomatorBridgeMethod = device.getClass().getDeclaredMethod("getAutomatorBridge");
        getAutomatorBridgeMethod.setAccessible(true);
        Object UiAutomatorBridge = getAutomatorBridgeMethod.invoke(device);

        UiAutomatorBridgeClass = Class.forName("com.android.uiautomator.core.UiAutomatorBridge");
        getInteractionControllerMethod = UiAutomatorBridgeClass.getDeclaredMethod("getInteractionController");
        getInteractionControllerMethod.setAccessible(true);
        Object interactionController = getInteractionControllerMethod.invoke(UiAutomatorBridge);

        InteractionControllerClass = Class.forName("com.android.uiautomator.core.InteractionController");
        touchDownMethod = InteractionControllerClass.getDeclaredMethod("touchDown", int.class, int.class);
        touchDownMethod.setAccessible(true);
        touchMoveMethod = InteractionControllerClass.getDeclaredMethod("touchMove", int.class, int.class);
        touchMoveMethod.setAccessible(true);
        touchUpMethod = InteractionControllerClass.getDeclaredMethod("touchUp", int.class, int.class);
        touchUpMethod.setAccessible(true);

        touchDownMethod.invoke(interactionController, x, y);
        long endtime = System.currentTimeMillis() + touchTime;
        while (endtime >= System.currentTimeMillis()) {
            touchMoveMethod.invoke(interactionController, x + random.nextInt(20), y + random.nextInt(20));
        }
        touchUpMethod.invoke(interactionController, x, y);
        if (millis > 0) {
            SystemClock.sleep(millis);
        }
    }

    /**
     * 获取屏幕精确尺寸
     */
    protected Point getRealSize() throws Exception {
        Class<?> InteractionControllerClass;
        Class<?> UiAutomatorBridgeClass;
        Method getAutomatorBridgeMethod;
        Method getDefaultDisplayMethod;

        getAutomatorBridgeMethod = device.getClass().getDeclaredMethod("getAutomatorBridge");
        getAutomatorBridgeMethod.setAccessible(true);
        Object UiAutomatorBridge = getAutomatorBridgeMethod.invoke(device);

        UiAutomatorBridgeClass = Class.forName("com.android.uiautomator.core.UiAutomatorBridge");
        getDefaultDisplayMethod = UiAutomatorBridgeClass.getDeclaredMethod("getDefaultDisplay");
        getDefaultDisplayMethod.setAccessible(true);
        Display display = (Display) getDefaultDisplayMethod.invoke(UiAutomatorBridge);
        Point p = new Point();
        display.getRealSize(p);
        return p;
    }

    /**
     * 截图
     *
     * @return Bitmap
     */
    protected Bitmap screenshot() throws Exception {
        return screenshot(getRealSize().x, getRealSize().y);
    }

    /**
     * 截图
     *
     * @param width  宽
     * @param height 高
     * @return Bitmap
     */
    protected Bitmap screenshot(int width, int height) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException {
        String className;
        if (Build.VERSION.SDK_INT <= 17) {
            className = "android.view.Surface";
        } else {
            className = "android.view.SurfaceControl";
        }
        return (Bitmap) Class.forName(className).getDeclaredMethod("screenshot", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(null, width, height);
    }

    /**
     * 休眠
     *
     * @param millis 休眠时间(单位毫秒)
     */
    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
