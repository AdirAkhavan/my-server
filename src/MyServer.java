public class MyServer {
    public static void main(String[] args) {
        MultiThreadedWebServer myServer = new MultiThreadedWebServer("config.ini");
        myServer.run();
    } 
}
