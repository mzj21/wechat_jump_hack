package com.xing.jump;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        copyAssets(this, "Jump.jar", "/data/local/tmp");
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CmdUtil.canRootPermission()) {
                    new UiautomatorThread().start();
                } else {
                    CmdUtil.hasRootPermission();
                }
            }
        });
    }

    class UiautomatorThread extends Thread {
        @Override
        public void run() {
            super.run();
            CmdUtil.execCmd("uiautomator runtest Jump.jar -c com.xing.jump.Jump -e defult true -e jumpTime 500");
//            CmdUtil.execCmd("am instrument --user 0 -w -r -e debug false -e class com.xing.jump.ExampleInstrumentedTest com.xing.jump.test/android.support.test.runner.AndroidJUnitRunner");
        }
    }

    /**
     * 复制Assets到指定路径
     *
     * @param context    上下文
     * @param assetsName 指定Assets的文件名全称
     * @param savePath   保存路径
     */
    public static void copyAssets(Context context, String assetsName, String savePath) {
        String filename = savePath + "/" + assetsName;
        File dir = new File(savePath);
        try {
            if (dir.exists()) {
                dir.delete();
            }
            dir.createNewFile();
            if (!(new File(filename)).exists()) {
                InputStream is = context.getResources().getAssets().open(assetsName);
                FileOutputStream fos = new FileOutputStream(filename);
                byte[] buffer = new byte[7168];
                int count;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
