public class ClienteMain {
    public static void main(String[] args) {
        String serverIp = "127.0.0.1";
        int serverPort = 1234;

        Cliente fileClient = new Cliente(serverIp, serverPort);
        fileClient.start();
    }
}
//C:/Users/edson/OneDrive/Documentos/a.txt