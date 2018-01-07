package com.xing.jump;

import android.app.Service;
import android.app.UiAutomation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class Jump {
    private UiDevice device;
    private Context context;

    @Test
    public void test() throws Exception {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        context = InstrumentationRegistry.getInstrumentation().getContext();
        try {
            UiObject ui_1 = device.findObject(new UiSelector().className(TextView.class).text("微信").packageName("com.tencent.mm"));
            UiObject ui_2 = device.findObject(new UiSelector().className(TextView.class).text("通讯录").packageName("com.tencent.mm"));
            UiObject ui_3 = device.findObject(new UiSelector().className(TextView.class).text("发现").packageName("com.tencent.mm"));
            UiObject ui_4 = device.findObject(new UiSelector().className(TextView.class).text("我").packageName("com.tencent.mm"));
            long endtime = System.currentTimeMillis() + 30 * 1000;
            while (endtime > System.currentTimeMillis() && (!ui_1.exists() || !ui_2.exists() || !ui_3.exists() || !ui_4.exists())) {
            }
            if (!ui_1.exists() || !ui_2.exists() || !ui_3.exists() || !ui_4.exists()) {
                return;
            }
            swipe();
            sleep(1000);
            UiObject ui_5 = device.findObject(new UiSelector().className(TextView.class).text("跳一跳").packageName("com.tencent.mm"));
            click(ui_5.getVisibleBounds().centerX(), ui_5.getVisibleBounds().centerY(), 5000);
            click(getRealSize().x / 2, (int) (getRealSize().y * 1920 / getRealSize().y * 0.796), 1000);
            float JUMP_RATIO = 1.35f;
            double jumpRatio = JUMP_RATIO * 1080 / getRealSize().x;
            for (int i = 0; i < 2048; i++) {
                int[] myPos = findMyPos(screenshot());
                if (myPos != null) {
                    int[] excepted = {myPos[0] - 30, myPos[0] + 30};
                    int[] nextCenter = findNextCenter(screenshot(), excepted, myPos[1]);
                    if (nextCenter == null || nextCenter[0] == 0) {
                        break;
                    } else {
                        int centerX, centerY;
                        int[] whitePoint = findWhitePoint(screenshot(), nextCenter[0] - 120, nextCenter[1], nextCenter[0] + 120, nextCenter[1] + 180);
                        if (whitePoint != null) {
                            centerX = whitePoint[0];
                            centerY = whitePoint[1];
                        } else {
                            if (nextCenter[2] != Integer.MAX_VALUE && nextCenter[4] != Integer.MIN_VALUE) {
                                centerX = (nextCenter[2] + nextCenter[4]) / 2;
                                centerY = (nextCenter[3] + nextCenter[5]) / 2;
                            } else {
                                centerX = nextCenter[0];
                                centerY = nextCenter[1] + 48;
                            }
                        }
                        int distance = (int) (Math.sqrt((centerX - myPos[0]) * (centerX - myPos[0]) + (centerY - myPos[1]) * (centerY - myPos[1])) * jumpRatio);
                        click(getRealSize().x / 2, getRealSize().y / 2, distance, 2000);
                    }
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int[] findWhitePoint(Bitmap image, int x1, int y1, int x2, int y2) {
        if (image == null) {
            return null;
        }

        int width = image.getWidth();
        int height = image.getHeight();

        for (int i = x1; i <= x2; i++) {
            for (int j = y1; j <= y2; j++) {
                int pixel = image.getPixel(i, j);
                int r = (pixel & 0xff0000) >> 16;
                int g = (pixel & 0xff00) >> 8;
                int b = (pixel & 0xff);
                if (r == 245 && g == 245 && b == 245) {
                    boolean[][] vMap = new boolean[width][height];
                    Queue<int[]> queue = new ArrayDeque<>();
                    int[] pos = {i, j};
                    queue.add(pos);
                    int maxX = Integer.MIN_VALUE;
                    int minX = Integer.MAX_VALUE;
                    int maxY = Integer.MIN_VALUE;
                    int minY = Integer.MAX_VALUE;
                    while (!queue.isEmpty()) {
                        pos = queue.poll();
                        int x = pos[0];
                        int y = pos[1];
                        if (x < x1 || x > x2 || y < y1 || y > y2 || vMap[x][y]) {
                            continue;
                        }
                        vMap[x][y] = true;
                        pixel = image.getPixel(x, y);
                        r = (pixel & 0xff0000) >> 16;
                        g = (pixel & 0xff00) >> 8;
                        b = (pixel & 0xff);
                        if (r == 245 && g == 245 && b == 245) {
                            maxX = Math.max(maxX, x);
                            minX = Math.min(minX, x);
                            maxY = Math.max(maxY, y);
                            minY = Math.min(minY, y);
                            queue.add(new int[]{x - 1, y});
                            queue.add(new int[]{x + 1, y});
                            queue.add(new int[]{x, y - 1});
                            queue.add(new int[]{x, y + 1});
                        }
                    }
                    if (maxX - minX <= 45 && maxX - minX >= 35 && maxY - minY <= 30 && maxY - minY >= 20) {
                        return new int[]{(minX + maxX) / 2, (minY + maxY) / 2};
                    } else {
                        return null;
                    }

                }
            }
        }
        return null;
    }

    public int[] findNextCenter(Bitmap image, int[] exceptedX, int maxY) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        int pixel = image.getPixel(0, 200);
        int r1 = (pixel & 0xff0000) >> 16;
        int g1 = (pixel & 0xff00) >> 8;
        int b1 = (pixel & 0xff);
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < width; i++) {
            pixel = image.getPixel(i, height - 1);
            if (map.get(pixel) != null || map.containsKey(pixel)) {
                map.put(pixel, map.get(pixel) + 1);
            } else {
                map.put(pixel, 1);
            }
        }
        int max = 0;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getValue() > max) {
                pixel = entry.getKey();
                max = entry.getValue();
            }
        }
        int r2 = (pixel & 0xff0000) >> 16;
        int g2 = (pixel & 0xff00) >> 8;
        int b2 = (pixel & 0xff);

        int t = 16;

        int minR = Math.min(r1, r2) - t;
        int maxR = Math.max(r1, r2) + t;
        int minG = Math.min(g1, g2) - t;
        int maxG = Math.max(g1, g2) + t;
        int minB = Math.min(b1, b2) - t;
        int maxB = Math.max(b1, b2) + t;
        int[] ret = new int[6];
        int targetR = 0, targetG = 0, targetB = 0;
        boolean found = false;
        for (int j = height / 4; j < maxY; j++) {
            for (int i = 0; i < width; i++) {
                if (i >= exceptedX[0] && i <= exceptedX[1]) {
                    continue;
                }
                pixel = image.getPixel(i, j);
                int r = (pixel & 0xff0000) >> 16;
                int g = (pixel & 0xff00) >> 8;
                int b = (pixel & 0xff);
                if (r < minR || r > maxR || g < minG || g > maxG || b < minB || b > maxB) {
                    ret[0] = i;
                    ret[1] = j;
                    for (int k = 0; k < 5; k++) {
                        pixel = image.getPixel(i, j + k);
                        targetR += (pixel & 0xff0000) >> 16;
                        targetG += (pixel & 0xff00) >> 8;
                        targetB += (pixel & 0xff);
                    }
                    targetR /= 5;
                    targetG /= 5;
                    targetB /= 5;
                    found = true;
                    break;
                }
            }
            if (found) {
                break;
            }
        }

        boolean[][] matchMap = new boolean[width][height];
        boolean[][] vMap = new boolean[width][height];
        ret[2] = Integer.MAX_VALUE;
        ret[3] = Integer.MAX_VALUE;
        ret[4] = Integer.MIN_VALUE;
        ret[5] = Integer.MAX_VALUE;

        Queue<int[]> queue = new ArrayDeque<>();
        queue.add(ret);
        while (!queue.isEmpty()) {
            int[] item = queue.poll();
            int i = item[0];
            int j = item[1];
            if (i >= exceptedX[0] && i <= exceptedX[1]) {
                continue;
            }
            if (j >= maxY) {
                continue;
            }

            if (i < Math.max(ret[0] - 200, 0) || i >= Math.min(ret[0] + 200, width) || j < Math.max(0, ret[1] - 300) || j >= Math.max(height, ret[1] + 300) || vMap[i][j]) {
                continue;
            }
            vMap[i][j] = true;
            pixel = image.getPixel(i, j);
            int r = (pixel & 0xff0000) >> 16;
            int g = (pixel & 0xff00) >> 8;
            int b = (pixel & 0xff);
            matchMap[i][j] = match(r, g, b, targetR, targetG, targetB, 16);
            if (matchMap[i][j]) {
                if (i < ret[2]) {
                    ret[2] = i;
                    ret[3] = j;
                } else if (i == ret[2] && j < ret[3]) {
                    ret[2] = i;
                    ret[3] = j;
                }
                if (i > ret[4]) {
                    ret[4] = i;
                    ret[5] = j;
                } else if (i == ret[4] && j < ret[5]) {
                    ret[4] = i;
                    ret[5] = j;
                }
                queue.add(new int[]{i - 1, j});
                queue.add(new int[]{i + 1, j});
                queue.add(new int[]{i, j - 1});
                queue.add(new int[]{i, j + 1});
            }
        }
        return ret;
    }

    public int[] findMyPos(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] ret = {0, 0};
        int maxX = Integer.MIN_VALUE;
        int minX = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height * 3 / 4; j++) {
                int pixel = bitmap.getPixel(i, j);
                int r = (pixel & 0xff0000) >> 16;
                int g = (pixel & 0xff00) >> 8;
                int b = (pixel & 0xff);
                if (match(r, g, b, 40, 43, 86, 16) && j > ret[1]) {
                    maxX = Math.max(maxX, i);
                    minX = Math.min(minX, i);
                    maxY = Math.max(maxY, j);
                    minY = Math.min(minY, j);
                }
            }
        }
        ret[0] = (maxX + minX) / 2;
        ret[1] = maxY;
        return ret;
    }

    public static boolean match(int r, int g, int b, int rt, int gt, int bt, int t) {
        return r > rt - t && r < rt + t && g > gt - t && g < gt + t && b > bt - t && b < bt + t;
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
        Method getInteractionControllerMethod;
        Method touchDownMethod;
        Method touchUpMethod;
        Method touchMoveMethod;

        getInteractionControllerMethod = device.getClass().getDeclaredMethod("getInteractionController");
        getInteractionControllerMethod.setAccessible(true);
        Object interactionController = getInteractionControllerMethod.invoke(device);

        InteractionControllerClass = Class.forName("android.support.test.uiautomator.InteractionController");
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
     * 获取屏幕精确尺寸
     */
    protected Point getRealSize() throws Exception {
        Display display = ((WindowManager) context.getSystemService(Service.WINDOW_SERVICE)).getDefaultDisplay();
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
        Method getUiAutomationMethod;
        getUiAutomationMethod = device.getClass().getDeclaredMethod("getUiAutomation");
        getUiAutomationMethod.setAccessible(true);
        UiAutomation interactionController = (UiAutomation) getUiAutomationMethod.invoke(device);
        return interactionController.takeScreenshot();
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
