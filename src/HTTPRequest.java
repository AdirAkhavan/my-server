import java.util.HashMap;

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
    }
}
