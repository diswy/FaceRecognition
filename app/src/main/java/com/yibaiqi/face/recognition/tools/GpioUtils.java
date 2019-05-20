package com.yibaiqi.face.recognition.tools;

import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Administrator on 2018/11/9.
 */

public class GpioUtils {

    private static final String TAG = "GpioUtils";

    /*
    给export申请权限
     */
    public static void upgradeRootPermissionForExport(){
        upgradeRootPermission("/sys/class/gpio/export");
    }

    /*
   配置一个gpio路径
    */
    public static boolean exportGpio(int gpio) {
        return writeNode("/sys/class/gpio/export", ""+gpio);
    }

    /*
    给获取io口的状态的路径申请权限,该方法需要在exportGpio后调用
     */
    public static void upgradeRootPermissionForGpio(int gpio){
        upgradeRootPermission("/sys/class/gpio/gpio" + gpio + "/direction");
        upgradeRootPermission("/sys/class/gpio/gpio" + gpio + "/value");
    }


    /*
    设置io口为输入或输出
     */
    public static boolean setGpioDirection(int gpio, int arg){
        String gpioDirection = "";
        if(arg == 0) gpioDirection = "out";
        else if(arg == 1) gpioDirection = "in";
        else return false;
        return writeNode("/sys/class/gpio/gpio" + gpio + "/direction", gpioDirection);
    }

    /*
    获取io口的状态为输出还是输入
     */
    public static String getGpioDirection(int gpio){
        String gpioDirection ="";
        gpioDirection = readNode("/sys/class/gpio/gpio" + gpio + "/direction");
        return  gpioDirection;
    }

    /*
    给输出io口写值，高电平或低电平
     */
    public static boolean writeGpioValue(int gpio, String arg){
        return writeNode("/sys/class/gpio/gpio" + gpio + "/value", arg);
    }

    //获取当前gpio是高还是低
    public static String getGpioValue(int gpio) {
        return readNode("/sys/class/gpio/gpio" + gpio + "/value");
    }


    private static boolean upgradeRootPermission(String path) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd="chmod 777 " + path;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        try {
            return process.waitFor()==0;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean writeNode(String path, String arg) {
        Log.d(TAG, "Gpio_test set node path: " + path + " arg: " + arg);
        if (path == null || arg == null) {
            Log.e(TAG, "set node error");
            return false;
        }
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileWriter = new FileWriter(path);
            fileWriter.write(arg);
        } catch (Exception e) {
            Log.e(TAG, "Gpio_test write node error!! path" + path + " arg: " + arg);
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private static long mTime = 0;
    private static int mFailTimes = 0;
    private static String readNode(String path) {
        String result = "";

        FileReader fread = null;
        BufferedReader buffer = null;
        try {
            fread = new FileReader(path);
            buffer = new BufferedReader(fread);
            String str = null;
            while ((str = buffer.readLine()) != null) {
                result = str;
                break;
            }
            mFailTimes = 0;
        } catch (IOException e) {
            Log.e(TAG, "IO Exception");
            e.printStackTrace();
            if (mTime == 0 || SystemClock.uptimeMillis() - mTime < 1000) {
                mFailTimes++;
            }
            if (mFailTimes >= 3) {
                Log.d(TAG, "read format node continuous failed three times, exist thread");
            }
        } finally {
            try {
                if (buffer != null) {
                    buffer.close();
                }
                if (fread != null) {
                    fread.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
