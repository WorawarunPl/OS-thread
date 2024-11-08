import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileServer {

    private static final int PORT = 8080;
    private static final String DIRECTORY = "./ServerFile/"; 
    private static final ConcurrentHashMap<Socket, Integer> clientThreadCountMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        clientThreadCountMap.merge(socket, 1, Integer::sum);
        System.out.println("Client connected: " + socket.getPort());
        System.out.println("Threads connected to this client: " + clientThreadCountMap.get(socket));

        try (DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            sendFileList(out); 

            String requestedFile = in.readUTF(); 
            sendFile(out, requestedFile); 

        } catch (IOException e) {
            e.printStackTrace();
        } finally { 
            clientThreadCountMap.computeIfPresent(socket, (s, count) -> count > 1 ? count - 1 : null);
            System.out.println("Client disconnected: " + socket.getPort());
        }
    }

    private static void sendFileList(DataOutputStream out) throws IOException { 
        File[] files = new File(DIRECTORY).listFiles(); 
        StringBuilder fileList = new StringBuilder(); 
        for (File file : files) { 
            fileList.append(file.getName()).append("\n"); 
        }
        out.writeUTF(fileList.toString()); 
    }

    private static void sendFile(DataOutputStream out, String requestedFile) throws IOException { 
        File fileToSend = new File(DIRECTORY + requestedFile); 
        if (fileToSend.exists()) { 
            out.writeLong(fileToSend.length());
            try (FileInputStream fis = new FileInputStream(fileToSend)) { //
                byte[] buffer = new byte[4096]; 
                int read;
                while ((read = fis.read(buffer)) != -1) { 
                    out.write(buffer, 0, read); 
                }
            }
        } else {
            out.writeLong(0); 
        }
    }
}