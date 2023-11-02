package Servidores;

import java.net.SocketException;

import middleware.Servidor;


public class Main {

    public static void main(String[] args) throws SocketException {
        
        Servidor servidor = new Servidor();
        servidor.run();

    }
}
