public class ServidorMain {
    public static void main(String[] args) {
        String serverDirectory = "C:\\Users\\edson\\OneDrive\\Documentos\\vs-code\\facul\\sd\\projetoSD"; // Replace with the desired server directory
        Servidor servidor = new Servidor(serverDirectory);
        servidor.start();
    }
}
