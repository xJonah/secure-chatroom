import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.util.Scanner;

public class Server {

    //Fields
    public static final String KEYSTORE_LOCATION = "C:\\Keys\\ServerKeyStore.jks";
    public static final String KEYSTORE_PASSWORD = "Coursework^1";
    public static final int TLS_PORT = 43221;

    //Executed on run
    public static void main(String[] args) throws IOException {

        //SSL system properties set
        System.setProperty("javax.net.ssl.keyStore", KEYSTORE_LOCATION);
        System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASSWORD);

        //Server socket set to listen for connections on port 43211
        ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
        SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(TLS_PORT);
        serverSocket.setEnabledProtocols(new String[]{"TLSv1.3", "TLSv1.2"});

        //Start server
        try {
            int clientNumber = 1;
            //Run server while socket is open
            while (!serverSocket.isClosed()) {

                //Limit chat room to 10 clients
                if (clientNumber > 10) {
                    serverSocket.close();
                }
                //Allow connections to server
                SSLSocket socket = (SSLSocket) serverSocket.accept();
                ClientHandler client = new ClientHandler(socket);
                System.out.println("New client connected." + " (" + clientNumber + " / 10)");
                clientNumber++;

                //Assign client a thread
                Thread thread = new Thread(client);
                thread.start();
            }
        }
        //Close server socket on error
        catch (IOException e) {
            if (serverSocket != null) {
                serverSocket.close();
            }
            else {
                e.printStackTrace();
            }
        }
    }

    /*
    public static void ChoosePassword() {
        //Prompt host to choose a password
        Scanner sc = new Scanner(System.in);
        System.out.println("Please choose a password for the room: ");
        password = sc.nextLine();

        //Password validation
        boolean isValid = false;
        while (!isValid) {
            if (password.length() < 6) {
                System.out.println("Password should be at least 6 characters long and contain one number!");
                password = sc.nextLine();
            } else if (!password.matches(".*[0-9].*")) {
                System.out.println("Password should be at least 6 characters long and contain one number!");
                password = sc.nextLine();
            } else {
                isValid = true;
            }
        }
    }

    public static String GetPassword() {
        return password;
    }
     */
}

