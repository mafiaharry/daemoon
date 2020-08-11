package com.qa.daemoon;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Looper;
import android.util.Log;


import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.Multimap;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;




import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class testshot {


    static Looper looper;


    public static void main(String[] args) {
        AsyncHttpServer httpServer = new AsyncHttpServer() {
            protected boolean onRequest(AsyncHttpServerRequest request,
                                        AsyncHttpServerResponse response) {
                return super.onRequest(request, response);
            }
        };
        System.out.println("Packet Start!");

        Looper.prepare();
        looper = Looper.myLooper();
        System.out.println("Started Looper!");
        AsyncServer server = new AsyncServer();
        httpServer.get("/screenshot.jpg", new screenshothttpserver());
        httpServer.get("/meminfo", new MeminfoHttpServer());
        httpServer.get("/cpuinfo", new CpuinfoHttpServer());
        httpServer.listen(server, 9967);

        Looper.loop();

    }


    static class screenshothttpserver implements HttpServerRequestCallback {

        public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
            try {
                Bitmap bitmap = testshot.screenshot(1920, 1080);
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bout);
                bout.flush();
                response.send("image/jpeg", bout.toByteArray());
                return;
            } catch (Exception e) {
                response.code(500);
                response.send(e.toString());
                return;
            }
        }
    }


    static class MeminfoHttpServer implements HttpServerRequestCallback {
        private static final String TAG = "MemGet***:";

        @Override
        public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {

            String uri = request.getPath();
            Log.d(TAG, "onRequest " + uri);
            Object params;
            params = request.getQuery();
            if (params != null) {

                List<String> pid_list = ((Multimap) params).get("pid");
                String pid_s = pid_list.get(0);

                String resp_men = sendMensocket(pid_s);

                response.send(resp_men);
                Log.d(TAG, "params = " + params.toString());


            } else {
                response.send("ppid not found ");

            }


        }


    }

    static class CpuinfoHttpServer implements HttpServerRequestCallback {
        private static final String TAG = "CPUGet***:";

        @Override
        public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {

            String uri = request.getPath();
            Log.d(TAG, "onRequest " + uri);
            Object params;
            params = request.getQuery();
            if (params != null) {

                List<String> pid_list = ((Multimap) params).get("pid");
                String pid_s = pid_list.get(0);

                double cpu_rate = getProcessCpuUsage(pid_s);
                String resp_cpu_rate = String.valueOf(cpu_rate);
                response.send(resp_cpu_rate);
                Log.d(TAG, "params = " + params.toString());


            } else {
                response.send("ppid not found ");

            }


        }
    }

        public static String sendMensocket(String pid_s) {

            try {
                byte[] pid_bytes = pid_s.getBytes();
                byte data[] = new byte[512];
//        SocketAddress localAddr = new InetSocketAddress("127.0.0.1",33015);
                InetAddress address = InetAddress.getByName("127.0.0.1");
                int port = 33015;
                DatagramPacket packet = new DatagramPacket(pid_bytes, pid_bytes.length, address, port);

                DatagramSocket ds = new DatagramSocket();
                ds.setSoTimeout(1000);
                ds.setBroadcast(true);
                ds.send(packet);
                DatagramPacket packet2 = new DatagramPacket(data, data.length);
                ds.receive(packet2);

                String ends = new String(packet2.getData(), 0, packet2.getLength());
                ds.close();
                return ends;

            } catch (Exception e) {
                e.printStackTrace();

                return "nill";
            }


        }

        public static double getProcessCpuUsage(String pid) {
            try {
                RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
                String load = reader.readLine();
                String[] toks = load.split(" ");

                double totalCpuTime1 = 0.0;
                int len = toks.length;
                for (int i = 2; i < len; i++) {
                    totalCpuTime1 += Double.parseDouble(toks[i]);
                }

                RandomAccessFile reader2 = new RandomAccessFile("/proc/" + pid + "/stat", "r");
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
                for (int i = 2; i < len; i++) {
                    totalCpuTime2 += Double.parseDouble(toks[i]);
                }
                reader2.seek(0);
                load2 = reader2.readLine();
                String[] toks3 = load2.split(" ");

                double processCpuTime2 = 0.0;
                utime = Double.parseDouble(toks3[13]);
                stime = Double.parseDouble(toks3[14]);
                cutime = Double.parseDouble(toks3[15]);
                cstime = Double.parseDouble(toks3[16]);

                processCpuTime2 = utime + stime + cutime + cstime;
                double usage = (processCpuTime2 - processCpuTime1) * 100.00
                        / (totalCpuTime2 - totalCpuTime1);
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


        public static Bitmap screenshot(int width, int height) throws Exception {
            String surfaceClassName;
            if (Build.VERSION.SDK_INT <= 17) {

                surfaceClassName = "android.view.Surface";
            } else {
                surfaceClassName = "android.view.SurfaceControl";
            }
            Bitmap b = (Bitmap) Class.forName(surfaceClassName).getDeclaredMethod("screenshot",
                    new Class[]{Integer.TYPE, Integer.TYPE}).invoke(null,
                    new Object[]{Integer.valueOf(width),
                            Integer.valueOf(height)});
            return b;
        }

    }
