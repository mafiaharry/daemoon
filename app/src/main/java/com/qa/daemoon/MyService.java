package com.qa.daemoon;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class MyService extends Service {
    private Socket socket;
    InputStream is;
    OutputStream outputStream;



    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("zzzzzzyonBind");
        return null;
    }

    @Override
    public void onCreate() {
        System.out.println("\n++++++Create");
        ClassLoader loader = MyService.class.getClassLoader();
        System.out.println(loader);

//        显示当前目录
//        File dir = getFilesDir();
//        System.out.println(dir.getPath());

//        UdpServer();
//        UdpServer();
        new SocketThread().start();

        System.out.println("****************");






//        try {
//            socket = new Socket("192.168.50.107", 8989);
//            System.out.println("socket:"+socket.isConnected());
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//        outputStream = socket.getOutputStream();
//        outputStream.write(("test\n").getBytes("utf-8"));
//        System.out.println("send");
//        outputStream.flush();
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
//        try {
//            outputStream.close();
//            socket.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        super.onCreate();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("zzzzzzyonStartCommand");


        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        System.out.println("onDestroy");
        super.onDestroy();
    }





    private void flash_work(int pid)  {

        int a = 0;

        while( true ){


            try {
                a= getPidMemorySize(pid , MyApplication.getContext());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            writeData(String.valueOf(a));
        }
    }

    private  void  LoopPidMem(){


        File file = new File("/sdcard/wasu/pidtmem.txt");
        String pid = getFileContent(file);
        System.out.println(pid);
        int pid_int = Integer.parseInt(pid);
        flash_work(pid_int);
        System.out.println(pid_int);

    }




    private String getFileContent(File file) {
        String content = "";
        if (!file.isDirectory()) {  //检查此路径名的文件是否是一个目录(文件夹)

                try {
                    InputStream instream = new FileInputStream(file);
                    if (instream != null) {
                        InputStreamReader inputreader
                                = new InputStreamReader(instream, "UTF-8");
                        BufferedReader buffreader = new BufferedReader(inputreader);
                        String line = "";
                        //分行读取
                        while ((line = buffreader.readLine()) != null) {
                            content += line;
                        }
                        instream.close();//关闭输入流
                    }
                } catch (java.io.FileNotFoundException e) {
                    Log.d("TestFile", "The File doesn't not exist.");
                } catch (IOException e) {
                    Log.d("TestFile", e.getMessage());
                }

        }
        return content;
    }




    //进程总内存
    public  int getPidMemorySize(int pid  , Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int[] myMempid = new int[] { pid };
        Debug.MemoryInfo[] memoryInfo = am.getProcessMemoryInfo(myMempid);
        int memSize = memoryInfo[0].getTotalPss();
        //        dalvikPrivateDirty： The private dirty pages used by dalvik。
        //        dalvikPss ：The proportional set size for dalvik.
        //        dalvikSharedDirty ：The shared dirty pages used by dalvik.
        //        nativePrivateDirty ：The private dirty pages used by the native heap.
        //        nativePss ：The proportional set size for the native heap.
        //        nativeSharedDirty ：The shared dirty pages used by the native heap.
        //        otherPrivateDirty ：The private dirty pages used by everything else.
        //        otherPss ：The proportional set size for everything else.
        //        otherSharedDirty ：The shared dirty pages used by everything else.
        return memSize;
    }

    public   void  say(){
        System.out.println("IIIIIIIIIII say");
    }

    private void writeData(String text) {
        String filePath = "/sdcard/wasu/";
        String fileName = "men";
        writeTxtToFile(text, filePath, fileName);
    }

    // 将字符串写入到文本文件中
    private void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileOutputStream raf = new FileOutputStream(file);

            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

//生成文件

    private File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

//生成文件夹

    private static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }



}