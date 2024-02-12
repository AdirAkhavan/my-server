import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadedWebServer {

    public Properties configFileProperties;

    public MultiThreadedWebServer(String configFile) {
        this.configFileProperties = ConfigReader.getConfigProperties(configFile);
    }

    public void run() {
        int portNumber = parseIntProperty("port");
        int maxThreads = parseIntProperty("maxThreads");
        String defaultPage = configFileProperties.getProperty("defaultPage");
        String rootDirectory = changeTildeToHomeDir(configFileProperties.getProperty("root"));

        ExecutorService threadPool = Executors.newFixedThreadPool(maxThreads);

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server is listening on port " + portNumber);

            while (true) {
                String printingColor = PrintingColorProvider.provideNextPrintingColor();
                Socket clientSocket = serverSocket.accept();
                System.out.println(PrintingColorProvider.WHITE + "Accepted connection from " + clientSocket.getInetAddress());
                System.out.println("--------------------------------------------------");
                
                // Submit the task to the thread pool
                threadPool.submit(new RequestHandler(clientSocket, defaultPage, rootDirectory, printingColor));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            // Shutdown the thread pool gracefully
            threadPool.shutdown();
        }
    }

    private int parseIntProperty(String propertyName){
        String propertyAsString = this.configFileProperties.getProperty(propertyName);
        int propertyAsNumber = 0;
        
        try {
            propertyAsNumber = Integer.parseInt(propertyAsString);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing " + propertyName + ": [" + propertyAsString + "]. Got following error message:" + e.getMessage());
        }

        return propertyAsNumber;
    }

    private String changeTildeToHomeDir(String pathWithTilde){
        String homeDirectory = System.getProperty("user.home");
        String newHomeDirectory = homeDirectory.replace(File.separator, "/");
        
        return pathWithTilde.replace("~", newHomeDirectory);
    }
}