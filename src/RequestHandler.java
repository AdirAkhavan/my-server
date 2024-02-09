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
    
            HTTPRequest httpRequest = new HTTPRequest(request);
            System.out.println("---------------------");
            System.out.println(httpRequest);
            System.out.println("---------------------");
            String reqType = httpRequest.requestType;
            String response = "";
            
            if (reqType.equals("GET")) {
                System.out.println("---------------------");
                System.out.println("CALLING handleGetRequest");
                response = handleGetRequest(httpRequest, out);
                System.out.println("Response:");
                System.out.println(response);
                System.out.println("---------------------");
            }
            else if (reqType.equals("POST")) {
                // handlePostRequest(httpRequest);
                System.out.println("---------------------");
                System.out.println("handlePostRequest");
                System.out.println("---------------------");
            }
            else if (reqType.equals("HEAD")) {
                System.out.println("---------------------");
                System.out.println("CALLING handleHeadRequest");
                response = handleHeadRequest(httpRequest, out);
                System.out.println("Response:");
                System.out.println(response);
                System.out.println("---------------------");
            }
            else if (reqType.equals("TRACE")) {
                System.out.println("---------------------");
                System.out.println("CALLING handleTraceRequest");
                response = handleTraceRequest(httpRequest, out);
                System.out.println("Response:");
                System.out.println(response);
                System.out.println("---------------------");
            }
    
            // Close the connection
            clientSocket.close();
            System.out.println("Socket closed.");
            System.out.println("----------------------------------------------------------------------");
        }
    }
    
    private String handleGetRequest(HTTPRequest httpRequest, OutputStream out) throws IOException {
        // Determine the requested resource
        String requestedResource = httpRequest.requestedPage;
        String responseToPrint = "";
        String response = "No response";

        // Handle request for the HTML file
        if (requestedResource.equals("/") || requestedResource.equals("/index.html")) {
            // Adjusted path for 'default' folder
            String filePath = "../default/index.html";
            File file = new File(filePath);
            String content = readFile(file);
            response = "HTTP/1.1 200 OK\r\n" +
                              "Content-Type: text/html\r\n" +
                              "Content-Length: " + content.length() + "\r\n\r\n";
                              
            responseToPrint = response;
            response += content;
            out.write(response.getBytes());
        }
        // Handle request for images
        else if (requestedResource.matches(".*\\.(jpg|png|gif|bmp)")) {
            // Adjusted path for 'default' folder
            String filePath = "../default" + requestedResource;
            File file = new File(filePath);
            if (file.exists() && !file.isDirectory()) {
                byte[] content = readFileAsBytes(file);
                response = "HTTP/1.1 200 OK\r\n" +
                                  "Content-Type: " + getContentType(requestedResource) + "\r\n" +
                                  "Content-Length: " + content.length + "\r\n\r\n";
                out.write(response.getBytes());
                out.write(content);
            } else {
                // Send 404 Not Found if the image does not exist
                response = "HTTP/1.1 404 Not Found\r\n\r\n";
                out.write(response.getBytes());
            }

            responseToPrint = response;
        }
        else if (requestedResource.equals("/favicon.ico")) {
            String filePath = "../default/favicon.ico";
            File file = new File(filePath);
            if (file.exists() && !file.isDirectory()) {
                byte[] content = readFileAsBytes(file);
                response = "HTTP/1.1 200 OK\r\n" +
                                  "Content-Type: icon\r\n" +
                                  "Content-Length: " + content.length + "\r\n\r\n";
                out.write(response.getBytes());
                out.write(content);
            } else {
                // Send 404 Not Found if the favicon.ico does not exist
                response = "HTTP/1.1 404 Not Found\r\n\r\n";
                out.write(response.getBytes());
            }

            responseToPrint = response;
        }
        
        return responseToPrint;
    }

    // TODO: Implement
    private String handlePostRequest(HTTPRequest httpRequest) throws IOException {
        return "";
    }

    // TODO: Implement, current implementation is not good
    private String handleHeadRequest(HTTPRequest httpRequest, OutputStream out) throws IOException {
        // Handle HEAD request similar to GET, but do not send the body
        // You need to implement the logic to set filePath based on the requested resource
        String requestedResource = httpRequest.requestedPage;
        
        String response = "No response";

        // TODO: change filePath
        String filePath = "../default/index.html";
        File file = new File(filePath);
        if (file.exists() && !file.isDirectory()) {
            response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + getContentType(requestedResource) + "\r\n" +
                                "Content-Length: " + file.length() + "\r\n\r\n";
            out.write(response.getBytes());
        } else {
            // Send 404 Not Found if the file does not exist
            response = "HTTP/1.1 404 Not Found\r\n\r\n";
            out.write(response.getBytes());
        }
        
        return response;
    }

    // TODO: check if current implementation is good enough
    private String handleTraceRequest(HTTPRequest httpRequest, OutputStream out) throws IOException {
        // Handle TRACE request by echoing back the request
        String response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: message/http\r\n" +
                            "Content-Length: " + httpRequest.contentLength + "\r\n\r\n" +
                            httpRequest;
        out.write(response.getBytes());

        return response;
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
        return contentBuilder.toString();    }
    
    private byte[] readFileAsBytes(File file) throws IOException {
        // Convert file content to a byte array
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        return data;
    }
    
    private String getContentType(String filename) {
        if (filename.endsWith(".jpg")) {
            return "image/jpeg";
        } else if (filename.endsWith(".png")) {
            return "image/png";
        } else if (filename.endsWith(".gif")) {
            return "image/gif";
        } else if (filename.endsWith(".bmp")) {
            return "image/bmp";
        } else {
            return "application/octet-stream";
        }
    }
    
}
