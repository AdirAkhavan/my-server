import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

    public static Properties getConfigProperties(String fileName){
        Properties configFileProperties = null;

        try {
            configFileProperties = loadProperties(fileName);
            printProperties(configFileProperties, fileName);
            
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
        return configFileProperties;
    }

    private static Properties loadProperties(String fileName) throws IOException {
        Properties properties = new Properties();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            properties.load(reader);
        }

        return properties;
    }

    private static void printProperties(Properties properties, String fileName){
        System.out.println("Properties obtained from " + fileName + ":");
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            System.out.println(key + " = " + value);
        }

        System.out.println("--------------------------------------------------");
    }
}
