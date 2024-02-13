import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Stack;

class RequestHandler implements Runnable {
    private Socket clientSocket;
    private String defaultPage;
    private String rootDirectory;
    private String printingColor;

    public RequestHandler(Socket clientSocket, String defaultPage, String rootDirectory, String handlerPrintingColor) {
        this.clientSocket = clientSocket;
        this.defaultPage = defaultPage;
        this.rootDirectory = rootDirectory;
        this.printingColor = handlerPrintingColor;
    }

    @Override
    public void run() {
        try {
            handleRequest();
        } catch (Exception e) {
            printlnWithColor("HTTP/1.1 500 Internal Server Error\r\n\r\n");
        }
    }

    public static String simplifyPath(String path) {
        String[] components = path.split("/");
        Stack<String> stack = new Stack<>();
        
        for (String component : components) {
            if (!component.equals("..") && !component.isEmpty()) {
                stack.push(component);
            }
        }

        StringBuilder simplifiedPath = new StringBuilder();
        for (String dir : stack) {
            simplifiedPath.append("/").append(dir);
        }
        
        if (simplifiedPath.length() == 0){
            return "/";
        }
        else{
            return simplifiedPath.toString();
        }
    }

    private void handleRequest() throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {
    
            StringBuilder requestBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                requestBuilder.append(line).append("\r\n");
            }
            String partRequest = requestBuilder.toString();

            int contentLength = getContentLength(partRequest);
            
            if (contentLength > 0) {
                char[] body = new char[contentLength];
                in.read(body, 0, contentLength);
                requestBuilder.append(new String(body));
            }
            
            String request = requestBuilder.toString();
            HTTPRequest httpRequest = new HTTPRequest(request);
            boolean requestTypeIsImplemented = hanldeSpecificRequest(httpRequest, out);

            if (!requestTypeIsImplemented) {
                String response = "HTTP/1.1 \r\n\r\n";
                out.write(response.getBytes());
            }
    
            // Close the connection
            clientSocket.close();
            printlnWithColor("Socket closed.");
            printlnWithColor("--------------------------------------------------");
        }
    }

    // function return value is true only if request type was one of the options in the switch case 
    private boolean hanldeSpecificRequest(HTTPRequest httpRequest, OutputStream out) throws IOException{
        boolean requestTypeIsImplemented = false;
        String requestType = httpRequest.requestType;
        String response = "";
        printlnWithColor("--------------------------------------------------");
        printlnWithColor("Handling " + requestType + " request for resource: " + httpRequest.requestedPage);

        if (requestType.equals("GET")) {
            response = handleGetRequest(httpRequest, out);
            requestTypeIsImplemented = true;
        }
        else if (requestType.equals("POST")) {
            response = handlePostRequest(httpRequest, out);
            requestTypeIsImplemented = true;
        }
        else if (requestType.equals("HEAD")) {
            response = handleHeadRequest(httpRequest, out);
            requestTypeIsImplemented = true;
        }
        else if (requestType.equals("TRACE")) {
            response = handleTraceRequest(httpRequest, out);
            requestTypeIsImplemented = true;
        }

        printlnWithColor("Response:");
        printlnWithColor(response);
        printlnWithColor("--------------------------------------------------");

        return requestTypeIsImplemented;
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

    private String sendChunkedResponse(OutputStream out, String content) throws IOException {
        byte[] contentBytes = content.getBytes();
        int offset = 0;
        int chunkSize = 1024;
        StringBuilder chunkedContentBuilder = new StringBuilder();
    
        while (offset < contentBytes.length) {
            int thisChunkSize = Math.min(chunkSize, contentBytes.length - offset);
            String chunkSizeHex = Integer.toHexString(thisChunkSize);
    
            // Build the chunked content for the string representation
            chunkedContentBuilder.append(chunkSizeHex).append("\r\n");
            String chunkData = new String(contentBytes, offset, thisChunkSize);
            chunkedContentBuilder.append(chunkData).append("\r\n");
    
            // Send the actual chunk
            out.write(chunkSizeHex.getBytes());
            out.write("\r\n".getBytes());
            out.write(contentBytes, offset, thisChunkSize);
            out.write("\r\n".getBytes());
    
            offset += thisChunkSize;
        }
    
        // End of content signal for both the OutputStream and the string representation
        out.write("0\r\n\r\n".getBytes());
        chunkedContentBuilder.append("0\r\n\r\n");
    
        // Return the string representation of the chunked content
        return chunkedContentBuilder.toString();
    }
    
    private String handleGetRequest(HTTPRequest httpRequest, OutputStream out) throws IOException {
        // Determine the requested resource
        String requestedResource = httpRequest.requestedPage;
        String responseToPrint = "";
        String response = "No response";

        requestedResource = simplifyPath(requestedResource);

        // Handle request for the HTML file
        if (requestedResource.equals("/") || requestedResource.equals("/" + defaultPage)) {
            String filePath = rootDirectory + defaultPage;
            File file = new File(filePath);
            String content = readFile(file);

            if (httpRequest.chunkedTransfer) {
                // Prepare and send headers for chunked transfer
                response = "HTTP/1.1 200 OK\r\n" +
                                 "Content-Type: text/html\r\n" +
                                 "Transfer-Encoding: chunked\r\n\r\n"; // Notice there's no Content-Length header
                out.write(response.getBytes());
                String chunkedContent = sendChunkedResponse(out, content);
                response += chunkedContent;
                responseToPrint = response;
            }
            else {
                response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + content.length() + "\r\n\r\n";
                response += content;
                responseToPrint = response;
                out.write(response.getBytes());
            }
        }
        // Handle request for images
        else if (requestedResource.matches(".*\\.(jpg|png|gif|bmp)")) {
            // Adjusted path for 'default' folder
            String filePath = rootDirectory + requestedResource;
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
            String filePath = rootDirectory + "favicon.ico";
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
        else {
            // handling any other file
            String filePath = rootDirectory + requestedResource;
            String simpleFilePath = simplifyPath(filePath);

            File file = new File(simpleFilePath);
    
            if (file.exists() && !file.isDirectory()) {
                byte[] content = readFileAsBytes(file);
                String contentType = getContentType(requestedResource);
                response = "HTTP/1.1 200 OK\r\n" +
                           "Content-Type: " + contentType + "\r\n" +
                           "Content-Length: " + content.length + "\r\n\r\n";
                out.write(response.getBytes()); // Write headers
                out.write(content); // Write file content
            } else {
                response = "HTTP/1.1 404 Not Found\r\n\r\n";
                out.write(response.getBytes());
            }
    
            responseToPrint = response;
        }
        
        return responseToPrint;
    }

    private String handlePostRequest(HTTPRequest httpRequest, OutputStream out) throws IOException {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html>\n");
        htmlContent.append("<html>\n");
        htmlContent.append("<head>\n");
        htmlContent.append("<title>Post Request Parameters</title>\n");
        htmlContent.append("</head>\n");
        htmlContent.append("<body>\n");
        htmlContent.append("<h1>Parameters</h1>\n");
        htmlContent.append("<ul>\n");

        // Iterate over parameters and append them to the HTML content
        for (Map.Entry<String, String> entry : httpRequest.parameters.entrySet()) {
            htmlContent.append("<li>").append(entry.getKey()).append(" = ").append(entry.getValue()).append("</li>\n");
        }
    
        htmlContent.append("</ul>\n");
        htmlContent.append("</body>\n");
        htmlContent.append("</html>");
    
        String responseContent = htmlContent.toString();
        String responseHeaders = "HTTP/1.1 200 OK\r\n" +
                                 "Content-Type: text/html\r\n" +
                                 "Content-Length: " + responseContent.getBytes().length + "\r\n\r\n";
    
        out.write(responseHeaders.getBytes());
        out.write(responseContent.getBytes());
    
        return responseHeaders + responseContent;
    }
    
    private String handleHeadRequest(HTTPRequest httpRequest, OutputStream out) throws IOException {
        String requestedResource = httpRequest.requestedPage;
        String responseToPrint = "";
        String response = "No response";

        if (requestedResource.equals("/") || requestedResource.equals("/" + defaultPage)) {
            String filePath = rootDirectory + defaultPage;
            File file = new File(filePath);
            String content = readFile(file);
            response = "HTTP/1.1 200 OK\r\n" +
                              "Content-Type: text/html\r\n" +
                              "Content-Length: " + content.length() + "\r\n\r\n";
            responseToPrint = response;                              
            out.write(responseToPrint.getBytes());
        }else{
            String filePath = rootDirectory + requestedResource;
            File file = new File(filePath);
            if (file.exists() && !file.isDirectory()) {
                String content = readFile(file);
                response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/html\r\n" +
                                "Content-Length: " + content.length() + "\r\n\r\n";
                responseToPrint = response;                              
                out.write(responseToPrint.getBytes());
            } else{
                response = "HTTP/1.1 404 Not Found\r\n\r\n";
                out.write(response.getBytes());
            }

            responseToPrint = response;
        }

        return responseToPrint;
    }

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

    private void printlnWithColor(String message){
        System.out.println(printingColor + message);
    }
}
