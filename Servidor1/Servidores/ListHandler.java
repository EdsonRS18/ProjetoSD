package Servidores;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListHandler {
    private final DataOutputStream dataOutputStream;

    public ListHandler(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    public void handleListFiles() {
        try {
            List<String> fileList = getFileList();

            sendFileList(fileList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getFileList() {
        List<String> fileList = new ArrayList<>();
        File directory = new File(System.getProperty("user.dir"));

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        fileList.add(file.getName());
                    }
                }
            }
        }

        return fileList;
    }

    private void sendFileList(List<String> fileList) throws IOException {
        dataOutputStream.writeUTF("FILE_LIST");
        dataOutputStream.writeInt(fileList.size());

        for (String fileName : fileList) {
            dataOutputStream.writeUTF(fileName);
        }
    }
}
