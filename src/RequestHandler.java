import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

class RequestHandler implements Runnable {
    private Socket clientSocket;

    public RequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleRequest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void handleRequest() throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {
    
            // Read the request (for simplicity, assuming a single-line request)
            String request = in.readLine();
            System.out.println("Request: " + request);
    
            // Read content from the index.html file
            String filePath = "../default/index.html";
            File file = new File(filePath);
            String content = readFile(file);
    
            // Send the HTTP response
            String response = "HTTP/1.1 200 OK\r\n" +
                              "Content-Type: text/html\r\n" +
                              "Content-Length: " + content.length() + "\r\n\r\n" +
                              content;
            out.write(response.getBytes());
    
            // Close the connection
            clientSocket.close();
            System.out.println("Socket closed.");
            System.out.println("----------------------------------------------------------------------");
        }
    }
    
    private String readFile(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file.getPath());
        }
    
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                contentBuilder.append(sCurrentLine).append("\n");
            }
        }

        return contentBuilder.toString();
    }
}
