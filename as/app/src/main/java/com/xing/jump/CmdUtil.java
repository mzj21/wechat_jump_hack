package com.xing.jump;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CmdUtil {
    public static final String TAG = "CmdUtil";
    public static final String COMMAND_SU = "su";
    public static final String COMMAND_SH = "shell";
    public static final String COMMAND_EXIT = "exit\n";
    public static final String COMMAND_LINE_END = "\n";

    /**
     * check whether can root permission
     */
    public static boolean canRootPermission() {
        return execCmd("echo root").result == 0;
    }

    /**
     * check whether has root permission
     */
    public static boolean hasRootPermission() {
        if (!TextUtils.isEmpty(execCmd("echo root").responseMsg)) {
            return execCmd("echo root").responseMsg.trim().equals("root");
        } else {
            return false;
        }
    }

    public static CommandResult execCmd(String cmd) {
        return execCmd(cmd, true);
    }

    public static CommandResult execCmd(String cmd, boolean root) {
        Log.d(TAG, "execCmd: " + cmd);
        int result = -1;
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(root ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            os.write(cmd.getBytes());
            os.writeBytes(COMMAND_LINE_END);
            os.flush();
            os.writeBytes(COMMAND_EXIT);
            os.flush();
            successMsg = new StringBuilder();
            errorMsg = new StringBuilder();
            result = process.waitFor();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s).append(COMMAND_LINE_END);
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s).append(COMMAND_LINE_END);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (errorResult != null) {
                    errorResult.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (process != null) {
                    process.destroy();
                }
            }

        }
        return new CommandResult(result, successMsg == null ? null : successMsg.toString(), errorMsg == null ? null
                : errorMsg.toString());
    }

    public static class CommandResult {
        final int result;
        String responseMsg;
        String errorMsg;

        public CommandResult(int result) {
            this.result = result;
        }

        public CommandResult(int result, String responseMsg, String errorMsg) {
            this.result = result;
            this.responseMsg = responseMsg;
            this.errorMsg = errorMsg;
        }
    }
}