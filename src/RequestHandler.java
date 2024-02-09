import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

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

    private int getContentLength(String request) {
        String[] lines = request.split("\r\n");
        for (String line : lines) {
            if (line.startsWith("Content-Length: ")) {
                return Integer.parseInt(line.substring("Content-Length: ".length()).trim());
            }
        }
        return 0; // Returns 0 if the Content-Length header isn't found
    }

    private void handleRequest() throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {
    
            // Read the request (for simplicity, assuming a single-line request)
            // in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            StringBuilder requestBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                requestBuilder.append(line).append("\r\n");
            }
            String part_request = requestBuilder.toString();

            int contentLength = getContentLength(part_request); // You need to implement this method
            
            if (contentLength > 0) {
                char[] body = new char[contentLength];
                in.read(body, 0, contentLength);
                requestBuilder.append(new String(body));
            }
            
            String request = requestBuilder.toString();

            // System.out.println("Request: " + request);
    
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
                System.out.println("CALLING handlePostRequest");
                response = handlePostRequest(httpRequest, out);
                System.out.println("Response:");
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
                              
            response += content;
            responseToPrint = response;
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
        }else {
            // handling any other file
            String filePath = requestedResource;
            File file = new File(filePath);
    
            if (file.exists() && !file.isDirectory()) {
                // If the file exists and is not a directory, read and serve it
                byte[] content = readFileAsBytes(file);
                String contentType = getContentType(requestedResource); // Determine content type based on file extension
                response = "HTTP/1.1 200 OK\r\n" +
                           "Content-Type: " + contentType + "\r\n" +
                           "Content-Length: " + content.length + "\r\n\r\n";
                out.write(response.getBytes()); // Write headers
                out.write(content); // Write file content
            } else {
                // If the file does not exist, return a 404 Not Found response
                response = "HTTP/1.1 404 Not Found\r\n\r\n";
                out.write(response.getBytes());
            }
    
            responseToPrint = response;
        }
        
        return responseToPrint;
    }

    private String handlePostRequest(HTTPRequest httpRequest, OutputStream out) throws IOException {
        // Extract the requested page name to generate an HTML file
        // String requestedResource = httpRequest.requestedPage;
    
        // Start building the HTML content

        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html>\n");
        htmlContent.append("<html>\n");
        htmlContent.append("<head>\n");
        htmlContent.append("<title>Post Request Parameters</title>\n");
        htmlContent.append("</head>\n");
        htmlContent.append("<body>\n");
        htmlContent.append("<h1>Parameters</h1>\n");
        htmlContent.append("<ul>\n");

        System.out.println(httpRequest);
        // Iterate over parameters and append them to the HTML content
        for (Map.Entry<String, String> entry : httpRequest.parameters.entrySet()) {
            htmlContent.append("<li>").append(entry.getKey()).append(" = ").append(entry.getValue()).append("</li>\n");
        }
    
        htmlContent.append("</ul>\n");
        htmlContent.append("</body>\n");
        htmlContent.append("</html>");
    
        // Convert the StringBuilder content to String
        String responseContent = htmlContent.toString();
    
        // Prepare the HTTP response headers
        String responseHeaders = "HTTP/1.1 200 OK\r\n" +
                                 "Content-Type: text/html\r\n" +
                                 "Content-Length: " + responseContent.getBytes().length + "\r\n\r\n";
    
        // Write the headers followed by the HTML content to the output stream
        out.write(responseHeaders.getBytes());
        out.write(responseContent.getBytes());
    
        // Return the response for logging or further processing
        return responseHeaders + responseContent;
    }
    


    // TODO: Implement, current implementation is not good
    private String handleHeadRequest(HTTPRequest httpRequest, OutputStream out) throws IOException {
        // Handle HEAD request similar to GET, but do not send the body
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
        return responseToPrint;
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
