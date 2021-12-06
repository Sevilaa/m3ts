package ch.m3ts.connection;

//To be deleted
import static java.nio.charset.StandardCharsets.UTF_8;

import android.os.Build;
import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;

//Keep
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import ch.m3ts.util.Log;

public class udpClient {
    private Integer Port = 2000;
    private InetAddress ipAddress;
    private DatagramSocket socket;
    private DatagramPacket packet;
    private ConcurrentLinkedQueue<byte[]> sendQueue;
    private boolean sendFlag;
    public udpClient(String ip){
        sendFlag = true;
        try {
            ipAddress = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        sendQueue = new ConcurrentLinkedQueue<byte[]>();
        startSendThread();
    }

    private void startSendThread(){
        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new DatagramSocket();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                while(sendFlag){
                    if(!sendQueue.isEmpty()){
                        Log.d("Sending Udp Package in Thread to address "+ipAddress+":"+Port);
                        byte[] sendBuffer = sendQueue.remove();
                        //sendBuffer = "Its Kind of working".getBytes();
                        packet = new DatagramPacket(sendBuffer, sendBuffer.length, ipAddress, Port);
                        for(int i=0; i<5; i++) {
                            try {
                                socket.send(packet);
                                Log.d("Sending from Socket!");
                            } catch (IOException e) {
                                Log.d("Can't Send Data somehow!");
                                Log.d(e.toString());
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        sendThread.start();
    }

    public void sendString(String data){
        Log.d("sendString method");
        //SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        //String content = sdf.format(new Date());
        //content = "[" + Build.MODEL + "]" + content;
        byte[] sendBuffer = data.getBytes();
        sendQueue.add(sendBuffer);
    }

    public void sendTrack(){
        Log.d("Sending Track");
    }

    public void sendData(JSONObject json){
        Log.d("Sending auxilliary data");
        byte[] sendBuffer = json.toString().getBytes(UTF_8);
        packet = new DatagramPacket(sendBuffer, sendBuffer.length, ipAddress, Port);
    }

    public void sendStringData(String string){
        Log.d("Sending auxilliary data");
        byte[] sendBuffer = string.getBytes(StandardCharsets.US_ASCII);
        sendQueue.add(sendBuffer);
    }


}
