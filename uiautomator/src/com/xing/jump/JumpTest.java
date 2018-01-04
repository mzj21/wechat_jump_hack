package com.xing.jump;

import android.graphics.Bitmap;
import android.widget.TextView;

import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiSelector;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import util.CmdUtil;

public class JumpTest extends Base {
    static final float JUMP_RATIO = 1.35f;

    public JumpTest(UiDevice device) {
        super(device);
    }

    public void doMission() {
        try {
            CmdUtil.execCmd("am force-stop com.tencent.mm");
            sleep(1000);
            CmdUtil.execCmd("am start com.tencent.mm/com.tencent.mm.ui.LauncherUI");
            UiObject ui_1 = new UiObject(new UiSelector().className(TextView.class).text("微信").packageName("com.tencent.mm"));
            UiObject ui_2 = new UiObject(new UiSelector().className(TextView.class).text("通讯录").packageName("com.tencent.mm"));
            UiObject ui_3 = new UiObject(new UiSelector().className(TextView.class).text("发现").packageName("com.tencent.mm"));
            UiObject ui_4 = new UiObject(new UiSelector().className(TextView.class).text("我").packageName("com.tencent.mm"));
            long endtime = System.currentTimeMillis() + 30 * 1000;
            while (endtime > System.currentTimeMillis() && (!ui_1.exists() || !ui_2.exists() || !ui_3.exists() || !ui_4.exists())) {
            }
            if (!ui_1.exists() || !ui_2.exists() || !ui_3.exists() || !ui_4.exists()) {
                return;
            }
            swipe();
            sleep(1000);
            click(new UiObject(new UiSelector().className(TextView.class).text("跳一跳").packageName("com.tencent.mm")), 5000);
            click(getRealSize().x / 2, (int) (getRealSize().y * 1920 / getRealSize().y * 0.796), 1000);
            double jumpRatio = JUMP_RATIO * 1080 / getRealSize().x;

            int total = 0;
            int centerHit = 0;
            for (int i = 0; i < 5000; i++) {
                total++;
                int[] myPos = findMyPos(screenshot());
                if (myPos != null) {
                    int[] nextCenter = findNextCenter(screenshot(), myPos);
                    if (nextCenter == null || nextCenter[0] == 0) {
                        break;
                    } else {
                        int centerX, centerY;
                        int[] whitePoint = findWhitePoint(screenshot(), nextCenter[0] - 120, nextCenter[1], nextCenter[0] + 120, nextCenter[1] + 180);
                        if (whitePoint != null) {
                            centerX = whitePoint[0];
                            centerY = whitePoint[1];
                            centerHit++;
                            System.out.println("find whitePoint, succ, (" + centerX + ", " + centerY + "), centerHit: " + centerHit+ ", total: " + total);
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
                        click(getRealSize().x / 2, getRealSize().y / 2, distance, 2000 + random.nextInt(1000));
                    }
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 瓶子的下一步位置计算
     */
    public int[] findBottle(Bitmap bitmap, int i, int j) {
        if (bitmap == null) {
            return null;
        }

        int[] ret = new int[6];
        ret[0] = i;
        ret[1] = j;
        ret[2] = Integer.MAX_VALUE;
        ret[3] = Integer.MAX_VALUE;
        ret[4] = Integer.MIN_VALUE;
        ret[5] = Integer.MAX_VALUE;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        boolean[][] vMap = new boolean[width][height];
        Queue<int[]> queue = new ArrayDeque<>();
        int[] pos = {i, j};
        queue.add(pos);

        while (!queue.isEmpty()) {
            pos = queue.poll();
            i = pos[0];
            j = pos[1];
            if (i < 0 || i >= width || j < 0 || j > height || vMap[i][j]) {
                continue;
            }
            vMap[i][j] = true;
            int pixel = bitmap.getPixel(i, j);
            int r = (pixel & 0xff0000) >> 16;
            int g = (pixel & 0xff00) >> 8;
            int b = (pixel & 0xff);
            if (r == 255 && g == 255 && b == 255) {
                //System.out.println("("+i+", "+j+")");
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
                if (j < ret[1]) {
                    ret[0] = i;
                    ret[1] = j;
                }
                queue.add(new int[]{i - 1, j});
                queue.add(new int[]{i + 1, j});
                queue.add(new int[]{i, j - 1});
                queue.add(new int[]{i, j + 1});
            }
        }
        return ret;
    }

    /**
     * 白点的下一步位置计算
     */
    public int[] findWhitePoint(Bitmap bitmap, int x1, int y1, int x2, int y2) {
        if (bitmap == null) {
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        x1 = Math.max(x1, 0);
        x2 = Math.min(x2, width - 1);
        y1 = Math.max(y1, 0);
        y2 = Math.min(y2, height - 1);

        for (int i = x1; i <= x2; i++) {
            for (int j = y1; j <= y2; j++) {
                int pixel = bitmap.getPixel(i, j);
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
                        pixel = bitmap.getPixel(x, y);
                        r = (pixel & 0xff0000) >> 16;
                        g = (pixel & 0xff00) >> 8;
                        b = (pixel & 0xff);
                        if (r == 245 && g == 245 && b == 245) {
                            maxX = Math.max(maxX, x);
                            minX = Math.min(minX, x);
                            maxY = Math.max(maxY, y);
                            minY = Math.min(minY, y);
                            queue.add(buildArray(x - 1, y));
                            queue.add(buildArray(x + 1, y));
                            queue.add(buildArray(x, y - 1));
                            queue.add(buildArray(x, y + 1));
                        }
                    }
                    if (maxX - minX <= 45 && maxX - minX >= 35 && maxY - minY <= 30 && maxY - minY >= 20) {
                        int[] ret = {(minX + maxX) / 2, (minY + maxY) / 2};
                        return ret;
                    } else {
                        return null;
                    }

                }
            }
        }
        return null;
    }

    public int[] findNextCenter(Bitmap bitmap, int[] myPos) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int pixel = bitmap.getPixel(0, 200);
        int r1 = (pixel & 0xff0000) >> 16;
        int g1 = (pixel & 0xff00) >> 8;
        int b1 = (pixel & 0xff);
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < width; i++) {
            pixel = bitmap.getPixel(i, height - 1);
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
        for (int j = height / 4; j < myPos[1]; j++) {
            for (int i = 0; i < width; i++) {
                int dx = Math.abs(i - myPos[0]);
                int dy = Math.abs(j - myPos[1]);
                if (dy > dx) {
                    continue;
                }
                pixel = bitmap.getPixel(i, j);
                int r = (pixel & 0xff0000) >> 16;
                int g = (pixel & 0xff00) >> 8;
                int b = (pixel & 0xff);
                if (r < minR || r > maxR || g < minG || g > maxG || b < minB || b > maxB) {
                    ret[0] = i;
                    ret[1] = j;
                    System.out.println("top, x: " + i + ", y: " + j);
                    for (int k = 0; k < 5; k++) {
                        pixel = bitmap.getPixel(i, j + k);
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

        if (targetR == 255 && targetG == 255 && targetB == 255) {
            return findBottle(bitmap, ret[0], ret[1]);
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
            if (j >= myPos[1]) {
                continue;
            }

            if (i < Math.max(ret[0] - 300, 0) || i >= Math.min(ret[0] + 300, width) || j < Math.max(0, ret[1] - 400) || j >= Math.max(height, ret[1] + 400) || vMap[i][j]) {
                continue;
            }
            vMap[i][j] = true;
            pixel = bitmap.getPixel(i, j);
            int r = (pixel & 0xff0000) >> 16;
            int g = (pixel & 0xff00) >> 8;
            int b = (pixel & 0xff);
            matchMap[i][j] = match(r, g, b, targetR, targetG, targetB, 16);
            if (i == ret[0] && j == ret[1]) {
                System.out.println(matchMap[i][j]);
            }
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
                if (j < ret[1]) {
                    ret[0] = i;
                    ret[1] = j;
                }
                queue.add(buildArray(i - 1, j));
                queue.add(buildArray(i + 1, j));
                queue.add(buildArray(i, j - 1));
                queue.add(buildArray(i, j + 1));
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
            for (int j = height / 4; j < height * 3 / 4; j++) {
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
        ret[0] = (maxX + minX) / 2 + 3;
        ret[1] = maxY;
        return ret;
    }

    public static int[] buildArray(int i, int j) {
        int[] ret = {i, j};
        return ret;
    }

    public static boolean match(int r, int g, int b, int rt, int gt, int bt, int t) {
        return r > rt - t && r < rt + t && g > gt - t && g < gt + t && b > bt - t && b < bt + t;
    }
}
