import java.io.BufferedReader;
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

            // Send a simple HTTP response
            String response = "HTTP/1.1 200 OK\r\n\r\n" +
                              "<html><body><h1>Welcome to Ido and Adir's server.</h1></body></html>";
            out.write(response.getBytes());

            // Close the connection
            clientSocket.close();
            System.out.println("Socket closed.");
            System.out.println("----------------------------------------------------------------------");
        }
    }
}
