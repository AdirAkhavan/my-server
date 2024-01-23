public class MyServer {
    public static void main(String[] args) {
        MultiThreadedWebServer myServer = new MultiThreadedWebServer("config.ini");
        myServer.run();

        // String requestHeader = "GET /index.html?name=John&age=30&adir=ido HTTP/1.1\r\n" +
        //         "Host: localhost\r\n" +
        //         "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3\r\n" +
        //         "Accept-Language: en-US,en;q=0.8\r\n" +
        //         "Referer: http://localhost/form.html\r\n" +
        //         "Content-Length: 0\r\n" +
        //         "Connection: keep-alive";

        // HTTPRequest httpRequest = new HTTPRequest(requestHeader);
        
        // System.out.println("Request Type: " + httpRequest.requestType);
        // System.out.println("Requested Page: " + httpRequest.requestedPage);
        // System.out.println("Is Image: " + httpRequest.isImage);
        // System.out.println("Content Length: " + httpRequest.contentLength);
        // System.out.println("Referer: " + httpRequest.referer);
        // System.out.println("User Agent: " + httpRequest.agent);
        // System.out.println("Parameters: " + httpRequest.parameters.toString());
    }
    
}
