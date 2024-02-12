public class MyServer {
    public static void main(String[] args) {
        System.out.println(PrintingColorProvider.WHITE);
        MultiThreadedWebServer myServer = new MultiThreadedWebServer("config.ini");
        myServer.run();
    } 
}
