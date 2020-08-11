package com.qa.daemoon;


import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


public class Main {

    private static final String PROCESS_NAME = "Main.cli";
    private static final String VERSION = "1.0";

    public  static  void main(String[] args) {

        setArgV0(PROCESS_NAME);

        Options options = new Options();
        options.addOption("v", "version", false, "show current version");
        options.addOption("h", "help", false, "show this message");
        options.addOption("x", "exec", true, "show Men" );
        options.addOption("u", "udp", false, "udp server" );


        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            formatter.printHelp(PROCESS_NAME, options);
            System.exit(1);
            return;
        }

        if (cmd.hasOption("help")) {
            formatter.printHelp(PROCESS_NAME, options);
            return;
        }
        if (cmd.hasOption("version")) {
            System.out.println(VERSION);
            return;
        }
        if (cmd.hasOption("udp")) {
            System.out.println("udp server");


            UdpServer();
            return;
        }



        if (cmd.hasOption("exec")){

            String countryCode = cmd.getOptionValue("x");
            if(countryCode == null) {
                System.out.println("no arg ");
            }
            else {
                System.out.println(countryCode);
                double a = getProcessCpuUsage(countryCode);

//                int b =  getPidMemorySize(Integer.parseInt(countryCode),MyApplication.getContext());


                System.out.println(a);



                System.out.println("****************");
//                System.out.println(b);



            }

            return;
        }

    }




    public static double getProcessCpuUsage(String pid) {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();
            String[] toks = load.split(" ");

            double totalCpuTime1 = 0.0;
            int len = toks.length;
            for (int i = 2; i < len; i ++) {
                totalCpuTime1 += Double.parseDouble(toks[i]);
            }

            RandomAccessFile reader2 = new RandomAccessFile("/proc/"+ pid +"/stat", "r");
            String load2 = reader2.readLine();
            String[] toks2 = load2.split(" ");

            double processCpuTime1 = 0.0;
            double utime = Double.parseDouble(toks2[13]);
            double stime = Double.parseDouble(toks2[14]);
            double cutime = Double.parseDouble(toks2[15]);
            double cstime = Double.parseDouble(toks2[16]);

            processCpuTime1 = utime + stime + cutime + cstime;

            try {
                Thread.sleep(360);
            } catch (Exception e) {
                e.printStackTrace();
            }
            reader.seek(0);
            load = reader.readLine();
            reader.close();
            toks = load.split(" ");
            double totalCpuTime2 = 0.0;
            len = toks.length;
            for (int i = 2; i < len; i ++) {
                totalCpuTime2 += Double.parseDouble(toks[i]);
            }
            reader2.seek(0);
            load2 = reader2.readLine();
            String []toks3 = load2.split(" ");

            double processCpuTime2 = 0.0;
            utime = Double.parseDouble(toks3[13]);
            stime = Double.parseDouble(toks3[14]);
            cutime = Double.parseDouble(toks3[15]);
            cstime = Double.parseDouble(toks3[16]);

            processCpuTime2 = utime + stime + cutime + cstime;
            double usage = (processCpuTime2 - processCpuTime1) * 100.00
                    / ( totalCpuTime2 - totalCpuTime1);
            BigDecimal b = new BigDecimal(usage);
            double res = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            return res;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return 0.0;
    }



    /*进程内存上限*/
    public static int getMemoryMax() {
        return (int) (Runtime.getRuntime().maxMemory()/1024);
    }

    //进程总内存
    public static int getPidMemorySize(int pid, Context context) {
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


    public static  void UdpServer(){
        while (true) {
            getMsg();
        }
    }

    public static  void getMsg(){


        try {
            System.out.println("Start UDP server");
            byte data[] = new byte[4*1024];
            //创建一个DatagramPacket对象，并指定DatagramPacket对象的大小
            DatagramPacket packet = new DatagramPacket(data,data.length);

            DatagramSocket socket = new DatagramSocket(33015);


            //读取接收到得数据
            socket.receive(packet);
            //把客户端发送的数据转换为字符串。
            //使用三个参数的String方法。参数一：数据包 参数二：起始位置 参数三：数据包长
            String result = new String(packet.getData(),packet.getOffset() ,packet.getLength());

            DatagramPacket packet2 = new DatagramPacket(data, data.length,
                    packet.getAddress(), packet.getPort());

            socket.send(packet2);
            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void setArgV0(String text) {
        try {
            Method setter = android.os.Process.class.getMethod("setArgV0", String.class);
            setter.invoke(android.os.Process.class, text);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
