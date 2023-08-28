package Servidores;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public class ExistenceChecker {
    private final DataOutputStream dataOutputStream;

    public ExistenceChecker(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    public void checkFileExistence(String fileName) {
        try {
            sendExistenceStatus(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendExistenceStatus(String fileName) throws IOException {
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            dataOutputStream.writeUTF("FILE_FOUND");
        } else {
            dataOutputStream.writeUTF("FILE_NOT_FOUND");
        }
    }
}
