import java.io.IOException;
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
        String portNumberString = configFileProperties.getProperty("port");
        int portNumber = 0;
        
        try {
            portNumber = Integer.parseInt(portNumberString);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing port number: [" + portNumberString + "]. Got following error message:" + e.getMessage());
        }

        String maxThreadsString = configFileProperties.getProperty("maxThreads");
        int maxThreads = 0;
        
        try {
            maxThreads = Integer.parseInt(maxThreadsString);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing max thread number: [" + maxThreadsString + "]. Got following error message:" + e.getMessage());
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(maxThreads);

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server is listening on port " + portNumber);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());

                // Submit the task to the thread pool
                threadPool.submit(new RequestHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Shutdown the thread pool gracefully
            threadPool.shutdown();
        }
    }
}