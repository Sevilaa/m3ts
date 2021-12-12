package ch.m3ts.connection;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.graphics.Color;
import android.util.Pair;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

import ch.m3ts.util.Log;
import cz.fmo.Lib;
import cz.fmo.data.Track;

public class UDPClient {
    private Integer Port = 2000;
    private InetAddress ipAddress;
    private DatagramSocket socket;
    private DatagramPacket packet;
    private ConcurrentLinkedQueue<byte[]> sendQueue;
    private boolean sendFlag;
    byte[] trackHeader = {(byte) 0xfd};
    byte[] eventHeader = {(byte) 0xfe};

    public UDPClient(String ip){
        sendFlag = true;
        Log.d("UDP IP Address: "+ ip);
        try {
            ipAddress = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        sendQueue = new ConcurrentLinkedQueue<>();
        startSendThread();
    }

    public static byte[] joinByteArray(byte[] byte1, byte[] byte2) {

        return ByteBuffer.allocate(byte1.length + byte2.length)
                .put(byte1)
                .put(byte2)
                .array();

    }

    private void startSendThread(){
        Thread sendThread = new Thread(() -> {
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
                    try {
                        socket.send(packet);
                        Log.d("Sending from Socket!");
                    } catch (IOException e) {
                        Log.d("Can't Send Data!");
                        Log.d(e.toString());
                        e.printStackTrace();
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
        sendBuffer = joinByteArray(eventHeader, sendBuffer);
        sendQueue.add(sendBuffer);
    }
    public byte[] encodeTrack(Track track){
        Log.d("Enocding Track");
        byte sendBuffer[] = new byte[0];
        byte tmpBuffer[];
        byte lengthBuffer[];
        Lib.Detection latest = track.getLatest();
        JSONObject json = new JSONObject();
        int NumberOfTracks=0;
        while (latest != null){
            NumberOfTracks+=1;
            int color = Color.rgb(Math.round(track.getColor().rgba[0]), Math.round(track.getColor().rgba[1]), Math.round(track.getColor().rgba[2]));
            try {
                Log.d("Logging Track1: ");
                Log.d("Logging Track2: " + latest.centerX);
                json.put("id",NumberOfTracks);
                json.put("positionX",latest.centerX);
                json.put("positionY",latest.centerY);
                json.put("positionZ",latest.centerZ);
                json.put("color",color);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("Failed to put coordinates into JSON File!");
            }
            tmpBuffer = json.toString().getBytes();
            Log.d("Tack Point: " + json.toString());
            lengthBuffer = ByteBuffer.allocate(4).putInt(tmpBuffer.length).array();
            tmpBuffer = joinByteArray(lengthBuffer, tmpBuffer);
            sendBuffer = joinByteArray(sendBuffer, tmpBuffer);
            latest = latest.predecessor;
        }
        lengthBuffer = ByteBuffer.allocate(4).putInt(NumberOfTracks).array();
        sendBuffer = joinByteArray(lengthBuffer, sendBuffer);
        sendBuffer = joinByteArray(trackHeader, sendBuffer);
        return sendBuffer;
    }
    public void sendTrack(Track track){
        Log.d("Sending Track");
        if(track == null){
            Log.d("Track is null!");
            return;
        }
        byte[] sendBuffer = encodeTrack(track);
        sendQueue.add(sendBuffer);
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
