import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.util.Properties;
import java.util.logging.*;

public class Client {

    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
    private static String host;
    private static int port;
    private static String command;

    static {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("client.properties"));

            host = props.getProperty("server.host", "localhost");
            port = Integer.parseInt(props.getProperty("server.port", "8443"));
            command = props.getProperty("command", "HELLO");

            Level logLevel = Level.parse(props.getProperty("log.level", "INFO"));
            LogManager.getLogManager().reset();

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(logLevel);
            consoleHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(consoleHandler);

            FileHandler fileHandler = new FileHandler("client.log", true);
            fileHandler.setLevel(logLevel);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);

            LOGGER.setLevel(logLevel);
        } catch (Exception e) {
            System.err.println("Failed to load config: " + e.getMessage());
            System.exit(1);
        }
    }

    public Runnable runClient() {
        return () -> {
            try {
                System.setProperty("javax.net.ssl.trustStore", "client.truststore");
                System.setProperty("javax.net.ssl.trustStorePassword", "password");

                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
                socket.startHandshake();

                PrintWriter toServer = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                LOGGER.fine("Sending command: " + command);
                toServer.println(command);

                String response = fromServer.readLine();
                LOGGER.info("Received from server: " + response);

                fromServer.close();
                socket.close();
                LOGGER.fine("Connection closed cleanly.");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Client error: ", e);
            }
        };
    }

    public static void main(String[] args) {
        LOGGER.info("TLS Client is starting...");
        Client client = new Client();
        for (int i = 0; i < 100; i++) {
            new Thread(client.runClient()).start();
        }
    }
}
