import java.util.HashMap;
import java.util.Map.Entry;

public class HTTPRequest {
    
    //Type (GET/POST…)
    public String requestType;
    // Requested Page (/ or /index.html etc.)
    public String requestedPage;
    // Is Image – if the requested page has an extension of an image (jpg, bmp, gif…)
    public boolean isImage;
    // Content Length that is written in the request
    public int contentLength;
    // Referer – The referer header
    public String referer; 
    // User Agent – the user agent header
    public String agent;
    // Parameters – the parameters in the request (I used java.util.HashMap<String,String> to hold the parameters).
    public HashMap<String, String> parameters = new HashMap<String, String>();
    
    public HTTPRequest(String requestHeader) {
        parseRequestHeader(requestHeader);
    }

    public void parseRequestHeader(String requestHeader){
        // parse request header and assign all object's fields
        // Splitting the header into lines
        String[] lines = requestHeader.split("\r\n");
        for (String line : lines) {
            if (line.startsWith("GET") || line.startsWith("POST") || line.startsWith("HEAD") || line.startsWith("TRACE")) {
                // Extracting the request type and requested page
                String[] requestLine = line.split(" ");
                requestType = requestLine[0];
                requestedPage = requestLine[1];

                // Check if the requested page has an image extension
                isImage = requestedPage.matches(".*\\.(bmp|gif|png|jpg)");

                // Extract parameters if any
                if (requestedPage.contains("?")) {
                    String paramString = requestedPage.substring(requestedPage.indexOf("?") + 1);
                    requestedPage = requestedPage.substring(0, requestedPage.indexOf("?"));
                    String[] paramPairs = paramString.split("&");
                    for (String pair : paramPairs) {
                        String[] kv = pair.split("=");
                        if (kv.length == 2) {
                            parameters.put(kv[0], kv[1]);
                        }
                    }
                }
            } else if (line.startsWith("Content-Length")) {
                // Extracting content length
                contentLength = Integer.parseInt(line.split(": ")[1].trim());
            } else if (line.startsWith("Referer")) {
                // Extracting referer
                referer = line.split(": ")[1].trim();
            } else if (line.startsWith("User-Agent")) {
                // Extracting user agent
                agent = line.split(": ")[1].trim();
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTPRequest: {");
        sb.append("requestType='").append(requestType).append('\'');
        sb.append(", requestedPage='").append(requestedPage).append('\'');
        sb.append(", isImage=").append(isImage);
        sb.append(", contentLength=").append(contentLength);
        sb.append(", referer='").append(referer).append('\'');
        sb.append(", agent='").append(agent).append('\'');
        sb.append(", parameters={");
        if (!parameters.isEmpty()) {
            for (Entry<String, String> entry : parameters.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append(", ");
            }
            sb.setLength(sb.length() - 2); // Remove the last comma and space
        }
        sb.append("}}");
        return sb.toString();
    }
}
