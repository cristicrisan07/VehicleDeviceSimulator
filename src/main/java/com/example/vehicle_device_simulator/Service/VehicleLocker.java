package com.example.vehicle_device_simulator.Service;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import javax.bluetooth.*;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;
import java.io.*;
import java.util.Base64;

@Service
public class VehicleLocker {
    private static Object lock=new Object();
    private static LocalDevice localDevice;
    private static DiscoveryAgent agent;
    private static StreamConnection connection;
    private static StreamConnectionNotifier server;
    private static boolean waitForConnection = true;
    private static final UUID uuid = new UUID(                              //the uid of the service, it has to be unique,
            "27012f0c68af4fbf8dbe6bbaf7aa432a", false);
    public static void Main(String[] args){

        try {

            var token = args[0];
            while (waitForConnection) {
                System.out.println("Waiting for client to connect");
                connection = server.acceptAndOpen(); // Wait until client connects
                ////=== At this point, two devices should be connected ===//

                DataOutputStream dos = connection.openDataOutputStream();
                var messageToBeSent = "CONNECTED\n";
                System.out.println("Phone connected");
                dos.writeChars(messageToBeSent);

                DataInputStream dis = connection.openDataInputStream();

                StringBuilder cmd = new StringBuilder();
                try {
                    while (!cmd.toString().equals("terminate")) {

                        cmd.delete(0, cmd.length());
                        char c;
                        while (((c = dis.readChar()) > 0) && (c != '\n')) {
                            cmd.append(c);
                        }
                        var readString = cmd.toString();
                        if (!readString.equals("terminate")) {
                            try {
                                JSONObject jsonObject = (JSONObject) new JSONParser().parse(readString);

                                String remoteToken = (String) jsonObject.get("token");
                                if (remoteToken.equals(token)) {
                                    String operation = (String) jsonObject.get("operation");
                                    if (operation.equals("OPEN")) {
                                        System.out.println("Opening the doors");
                                        dos.writeChars("DOORS OPENED\n");
                                        dos.writeChars("terminate\n");

                                    } else {
                                        if (operation.equals("CLOSE")) {
                                            System.out.println("Closing the doors");
                                            dos.writeChars("DOORS CLOSED\n");
                                            dos.writeChars("terminate\n");
                                        }
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                } catch (EOFException e) {
                    System.out.println("Remote socket closed.");
                }

                connection.close();
//            try {
//                synchronized(lock){
//                    lock.wait();
//                }
//
//            }
//            catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//                System.out.println("Device Inquiry Completed. ");

            }
        }
        catch (InterruptedIOException e){
            System.out.println("Connection closed");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Main exited");
    }

    public static String closeConnection() throws IOException {
        if(connection == null){
            return "NO CONNECTION TO END";
        }else{
            connection.close();
            server.close();
            waitForConnection = false;
            return "SUCCESS";
        }
    }

    public static String openService(String token) throws IOException{
        localDevice = LocalDevice.getLocalDevice();
        agent = localDevice.getDiscoveryAgent();
        // 3
        //agent.startInquiry(DiscoveryAgent.GIAC, new MyDiscoveryListener());
        var remoteDevices = agent.retrieveDevices(DiscoveryAgent.PREKNOWN);
        for (RemoteDevice rm : remoteDevices) {
            System.out.println(rm.getFriendlyName(false));
           // inquireDeviceForAService(rm);

        }
        if(localDevice.getDiscoverable() != DiscoveryAgent.GIAC) {
            localDevice.setDiscoverable(DiscoveryAgent.GIAC); // Advertising the service
        }

        waitForConnection = true;
        String url = "btspp://localhost:" + uuid + ";name=BlueToothServer";
        server = (StreamConnectionNotifier) Connector.open(url);
        new Thread(()->Main(new String[]{token})).start();
        return "SUCCESS";
    }

    public static class MyDiscoveryListener implements DiscoveryListener {
        @Override
        public void deviceDiscovered(RemoteDevice btDevice, DeviceClass arg1) {
            String name;
            try {
                name = btDevice.getFriendlyName(false);
            } catch (Exception e) {
                name = btDevice.getBluetoothAddress();
            }

            System.out.println("device found: " + name);
            try {
                inquireDeviceForAService(btDevice);
            } catch (BluetoothStateException e) {
                throw new RuntimeException(e);
            }

        }

        @Override
        public void inquiryCompleted(int arg0) {
            synchronized(lock){
                lock.notify();
            }
        }

        @Override
        public void serviceSearchCompleted(int arg0, int arg1) {
            synchronized (lock) {
                lock.notify();
            }
        }

        @Override
        public void servicesDiscovered(int arg0, ServiceRecord[] services) {
            System.out.println("yes");
            for (int i = 0; i < services.length; i++) {
                String url = services[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                if (url == null) {
                    continue;
                }

                DataElement serviceName = services[i].getAttributeValue(0);
//                if (serviceName != null) {
//                    System.out.println("service " + serviceName.getValue() + " found " + url);
//                } else {
//                    System.out.println("service found " + url);
//                }
//
//                if(serviceName.getValue().equals("OBEX Object Push")){
     //               sendMessageToDevice(url);
//                }
            }

        }

    }

    public static void inquireDeviceForAService(RemoteDevice device) throws BluetoothStateException {
        UUID[] uuidSet = new UUID[1];
        uuidSet[0]=new UUID(0x1105); //OBEX Object Push service

        int[] attrIDs =  new int[] {
                0x0100 // Service name
        };

        LocalDevice localDevice = LocalDevice.getLocalDevice();
        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
        agent.searchServices(null,uuidSet,device, new MyDiscoveryListener());


        try {
            synchronized(lock){
                lock.wait();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private static void sendMessageToDevice(String serverURL){
        try{
            System.out.println("Connecting to " + serverURL);

            ClientSession clientSession = (ClientSession) Connector.open(serverURL);
            HeaderSet hsConnectReply = clientSession.connect(null);
            if (hsConnectReply.getResponseCode() != ResponseCodes.OBEX_HTTP_OK) {
                System.out.println("Failed to connect");
                return;
            }

            HeaderSet hsOperation = clientSession.createHeaderSet();
            hsOperation.setHeader(HeaderSet.NAME, "Hello.txt");
            hsOperation.setHeader(HeaderSet.TYPE, "text");

            //Create PUT Operation
            Operation putOperation = clientSession.put(hsOperation);

            // Sending the message
            byte data[] = "Hello World !!!".getBytes("iso-8859-1");
            OutputStream os = putOperation.openOutputStream();
            os.write(data);
            os.close();

            putOperation.close();
            clientSession.disconnect(null);
            clientSession.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
