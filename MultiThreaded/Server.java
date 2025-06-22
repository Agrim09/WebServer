import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.*;

public class Server {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    static void setupLogging(Level logLevel) {
        try {
            LogManager.getLogManager().reset();
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(logLevel);
            consoleHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(consoleHandler);
            FileHandler fileHandler = new FileHandler("server.log", true);
            fileHandler.setLevel(logLevel);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(logLevel);
        } catch (IOException e) {
            System.err.println("Failed to set up logger: " + e.getMessage());
        }
    }

    public Consumer<Socket> getConsumer() {
        return (clientSocket) -> {
            try {
                LOGGER.info("Client connected: " + clientSocket.getInetAddress());
                BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String command = fromClient.readLine();
                LOGGER.info("Received command: " + command);

                PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream(), true);
                toClient.println("Hello from the secure server!");

                fromClient.close();
                toClient.close();
                clientSocket.close();

                LOGGER.fine("Connection with client closed successfully.");
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "I/O error handling client: ", e);
            }
        };
    }


    public static void main(String[] args) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("server.properties"));
        } catch (IOException e) {
            System.err.println("Could not load server.properties: " + e.getMessage());
            return;
        }

        int port = Integer.parseInt(props.getProperty("port", "8443"));
        String keystorePath = props.getProperty("keystore.path");
        String keystorePassword = props.getProperty("keystore.password");
        Level logLevel = Level.parse(props.getProperty("log.level", "INFO"));

        setupLogging(logLevel);
        LOGGER.info("Secure TLS server is starting...");

        Server server = new Server();
        Consumer<Socket> consumer = server.getConsumer();
        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, keystorePassword.toCharArray());
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(kmf.getKeyManagers(), null, null);
            SSLServerSocketFactory ssf = sc.getServerSocketFactory();
            SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(port);
            LOGGER.info("TLS server started on port: " + port);

            while (true) {
                try {
                    SSLSocket sslSocket = (SSLSocket) serverSocket.accept();
                    LOGGER.fine("Accepted secure client connection.");
                    threadPool.execute(() -> consumer.accept(sslSocket));
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error accepting secure connection: ", e);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not start TLS server", e);
        } finally {
            threadPool.shutdown();
        }
    }
}


