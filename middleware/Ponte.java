package middleware;

import Servidores.funcoes.DownloadHandler;
import Servidores.funcoes.FileReplicator;
import Servidores.funcoes.ListHandler;
import Servidores.funcoes.UploadHandler;
import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;


class Ponte extends Thread {
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private HashSet<String> serverAddresses; // Referência à estrutura de armazenamento dos endereços IP

    public Ponte(Socket socket, HashSet<String> serverAddresses) {
        this.socket = socket;
        this.serverAddresses = serverAddresses;

        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            boolean running = true;
            while (running) {
                try {
                    String request = dataInputStream.readUTF();
                    System.out.println("Received request: " + request);

                    if (request.equals("UPLOAD")) {
                        String fileName = dataInputStream.readUTF();
                        long fileSize = dataInputStream.readLong();
                        System.out.println("Received UPLOAD request for file: " + fileName);

                        UploadHandler uploadHandler = new UploadHandler(dataInputStream);
                        uploadHandler.saveFile(fileName, fileSize);

                        // Replicate the file to other servers
                        replicateFileToOtherServers(fileName);

                    } else if (request.equals("LIST")) {
                        System.out.println("Received LIST request");
                        ListHandler listHandler = new ListHandler(dataOutputStream);
                        listHandler.sendFileList();
                    } else if (request.equals("DOWNLOAD")) {
                        String fileName = dataInputStream.readUTF();
                        System.out.println("Received DOWNLOAD request for file: " + fileName);

                        DownloadHandler downloadHandler = new DownloadHandler(dataOutputStream);
                        downloadHandler.sendFile(fileName);
                    } else if (request.equals("EXIT")) {
                        System.out.println("Received EXIT request");
                        running = false;
                      } else if (request.equals("REPLICATE")) {
                        String fileName = dataInputStream.readUTF();
                        String hash = dataInputStream.readUTF();
                        long fileSize = dataInputStream.readLong();
                        
                        UploadHandler uploadHandler = new UploadHandler(dataInputStream);
                        uploadHandler.saveFile(fileName, fileSize);

                        System.out.println("File replicated from another server: " + fileName);
                    }
                } catch (EOFException e) {
                    System.err.println("Client disconnected unexpectedly.");
                    running = false;
                }
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void replicateFileToOtherServers(String fileName) {
      String localIP = getLocalIPAddress();
   //   System.out.println("Local IP: " + localIP); print do ip local
  
      for (String serverAddress : serverAddresses) {
        //  System.out.println("Processing server address: " + serverAddress); print informando o outro ip
  
          // Example: serverAddress is in the format "IP:PORT"
          String[] parts = serverAddress.split(":");
          String ipAddress = parts[0];
          int port = Integer.parseInt(parts[1]);
  
          if (!ipAddress.equals(localIP)) {
              //System.out.println("Replicating file to server: " + ipAddress + ":" + port); print informando que foi replicado para o ip e porta
              FileReplicator.replicateFileToServer(fileName, ipAddress, port);
          } else {
             // System.out.println("Skipping local server: " + ipAddress + ":" + port);
              System.out.println("Skipping local server");
          }
      }
  }
  

    private String getLocalIPAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }
}
