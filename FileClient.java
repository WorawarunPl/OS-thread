import java.io.*;
import java.net.*;

public class FileClient {
    private static final String SERVER_ADDRESS = "172.27.108.37"; // ใส่ IP ของเครื่องที่รัน Server
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) { 
        new FileClient().start();
    }

    private void start() {
        try (Socket socket = createSocket(); 
             DataInputStream in = new DataInputStream(socket.getInputStream()); 
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            displayAvailableFiles(in); 
            String requestedFile = getUserInput(); 
            requestFile(out, requestedFile); 
            receiveFile(in, requestedFile); 

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Socket createSocket() throws IOException { 
        return new Socket(SERVER_ADDRESS, SERVER_PORT); 
    }

    private void displayAvailableFiles(DataInputStream in) throws IOException { 
        String fileList = in.readUTF();
        System.out.println("Available files:\n" + fileList);
    }

    private String getUserInput() throws IOException {
        System.out.println("Enter the name of the file you want to download:");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)); 
        return reader.readLine(); 
    }

    private void requestFile(DataOutputStream out, String requestedFile) throws IOException {
        out.writeUTF(requestedFile); 
    }

    private void receiveFile(DataInputStream in, String requestedFile) throws IOException {
        long fileSize = in.readLong(); 
        if (fileSize > 0) { 
            saveFile(in, requestedFile, fileSize); 
            System.out.println("File downloaded successfully!");
        } else {
            System.out.println("File not found on server.");
        }
    }

    private void saveFile(DataInputStream in, String requestedFile, long fileSize) throws IOException {
        File directory = new File("C:\\Users\\maturyn\\Desktop\\khem\\os_1"); 
        if (!directory.exists()) {
            directory.mkdirs(); 
        }

        try (FileOutputStream fos = new FileOutputStream(directory + "/" + requestedFile)) {
            byte[] buffer = new byte[4096];
            long remaining = fileSize;
            int read;
            while ((read = in.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
                fos.write(buffer, 0, read); 
                remaining -= read;
            }
        }
    }
}