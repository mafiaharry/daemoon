package com.qa.daemoon;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.DecimalFormat;

public class SocketThread extends Thread{

    @Override
    public void run() {
        super.run();
        System.out.println("start thread");
        UdpServer();
    }

    public static  void getMsg(){


        try {

            byte data[] = new byte[4*1024];
            //创建一个DatagramPacket对象，并指定DatagramPacket对象的大小
            DatagramPacket packet = new DatagramPacket(data,data.length);

            DatagramSocket socket = new DatagramSocket(33015);


            //读取接收到得数据
            socket.receive(packet);
            //把客户端发送的数据转换为字符串。
            //使用三个参数的String方法。参数一：数据包 参数二：起始位置 参数三：数据包长
            String result = new String(packet.getData(),packet.getOffset() ,packet.getLength());

            result = result.replace("\n", "");


            try {

                int pid = Integer.parseInt(result);
                System.out.println("get :"+pid);

                int pidMemorySize = getPidMemorySize(pid,MyApplication.getContext());

                int allmem  = getMemoryMax();
                DecimalFormat df = new DecimalFormat("0.00");
                String s = df.format((float)pidMemorySize/allmem);


                String s_PidmenSize = String.valueOf(pidMemorySize);
                String s_allmen = String.valueOf(allmem);

                String FanalMsg = s_PidmenSize +" " + s_allmen + " " + s ;

                byte back[] = FanalMsg.getBytes();

                System.out.println(pidMemorySize);




                DatagramPacket packet2 = new DatagramPacket(back, back.length,
                        packet.getAddress(), packet.getPort());
                socket.send(packet2);

            }catch (Exception e){

                DatagramPacket packet2 = new DatagramPacket(data, data.length,
                        packet.getAddress(), packet.getPort());
                socket.send(packet2);
            }



            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static  void UdpServer(){
        while (true) {
            getMsg();
        }
    }




    public static int getPidMemorySize(int pid  , Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int[] myMempid = new int[] { pid };
        Debug.MemoryInfo[] memoryInfo = am.getProcessMemoryInfo(myMempid);
        int memSize = memoryInfo[0].getTotalPss();
        /*     dalvikPrivateDirty： The private dirty pages used by dalvik。
               dalvikPss ：The proportional set size for dalvik.
               dalvikSharedDirty ：The shared dirty pages used by dalvik.
               nativePrivateDirty ：The private dirty pages used by the native heap.
               nativePss ：The proportional set size for the native heap.
               nativeSharedDirty ：The shared dirty pages used by the native heap.
               otherPrivateDirty ：The private dirty pages used by everything else.
               otherPss ：The proportional set size for everything else.
               otherSharedDirty ：The shared dirty pages used by everything else.*/
        return memSize;
    }

    public static int getMemoryMax() {
        return (int) (Runtime.getRuntime().maxMemory()/1024);
    }

}

