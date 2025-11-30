import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Client {

    //Fields
    public static final int TLS_PORT = 43221;
    public static final String TLS_HOST = "localhost";
    public static final String TRUSTSTORE_LOCATION = "C:\\CA\\ClientKeyStore.jks";
    public static final String TRUSTSTORE_PASSWORD = "Coursework^1";

    private SSLSocket socket;
    private String name;
    private BufferedReader br;
    private BufferedWriter bw;
    private String password;


    //Constructor
    public Client(SSLSocket socket, String name, String password) {
        try {
            this.socket = socket;
            this.name = name;
            this.password = password;

            //Convert byte streams to character streams
            this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        //Client connection terminated
        catch (IOException e) {
            CloseClient(socket, br, bw);
        }
    }

    //Executed on run
    public static void main(String[] args) throws IOException {

        //Set SSL system properties
        System.setProperty("javax.net.ssl.trustStore", TRUSTSTORE_LOCATION);
        System.setProperty("javax.net.ssl.trustStorePassword", TRUSTSTORE_PASSWORD);

        Scanner sc = new Scanner(System.in);

        //Client sets their identity/name
        System.out.println("Choose your username: ");
        String name = sc.nextLine();

        //Name validation
        boolean isValid = false;

        while (!isValid) {
            if (!name.matches("^[-a-zA-Z0-9]+")) {
                System.out.println("Username cannot contain special characters or spaces!");
                System.out.println("please choose a valid username: ");
                name = sc.nextLine();
            } else if (name.length() == 0 || name.length() > 14) {
                System.out.println("Username length should be between 1 and 14 characters");
                System.out.println("please choose a valid username: ");
                name = sc.nextLine();
            } /* else if (!CheckName(name)) {
                System.out.println("Username is inappropriate");
                System.out.println("please choose a valid username: ");
                name = sc.nextLine();
            } */
            else {
                isValid = true;
            }
        }

        //Retrieve password inputted from user
        System.out.println("Enter the room password: ");
        String password = sc.nextLine();

        //Retrieve set password
        final String serverPassword = ClientHandler.GetPassword();

        System.out.println("Connecting to server...");

        //Check if inputted password is equal to the room password
        if (!password.equals(serverPassword)) {
            System.out.println("Connection refused. Reason - Wrong password");
        }
        else {
            //Client attempts to connect to server
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket clientSocket = (SSLSocket) ssf.createSocket(TLS_HOST, TLS_PORT);
            clientSocket.startHandshake();

            Client client = new Client(clientSocket, name, password);
            System.out.println("You have joined the chat room!");

            //Both methods being run on separate threads till client leaves the room
            client.ReadMessage();
            client.SendMessage();
        }
    }

    //Method to send a message in the chat room
    public void SendMessage() {
        try {
            //Send client identity to server
            bw.write(name);
            bw.newLine();
            bw.flush();

            //Send client's inputted password to server
            bw.write(password);
            bw.newLine();
            bw.flush();

            Scanner sc = new Scanner(System.in);

            while (socket.isConnected()) {
                String message = sc.nextLine();

                //Limit message size
                if (message.length() > 300) {
                    System.out.println("Sever: Message size too large!");
                } else {
                    //Message format
                    bw.write(name + ": " + message);
                    bw.newLine();
                    bw.flush();
                }
            }
        }
        //Client connection terminated
        catch (IOException e) {
            CloseClient(socket, br, bw);
        }
    }

    //Method to receive messages in the chat room
    public void ReadMessage() {
        //Separate thread created
        new Thread(() -> {
            String messageFromChat;

            //While connected to the chat room, client can receive messages
            while (socket.isConnected()) {
                try {
                    messageFromChat = br.readLine();
                    System.out.println(messageFromChat);
                }
                //Client connection terminated
                catch (IOException e) {
                    CloseClient(socket, br, bw);
                }
            }
        }).start(); //Start separate thread when method is called
    }

    /*
    //Check appropriateness of client's username
    public static boolean CheckName(String name) throws IOException {

        //Reference - https://www.cs.cmu.edu/~biglou/resources/bad-words.txt
        final List<String> lines = Files.readAllLines(Paths.get(("resources/censorwords.txt")), StandardCharsets.UTF_8);
        boolean containsName = lines.stream().anyMatch(name::equalsIgnoreCase);

        if (containsName) {
            return false;
        } else {
            return true;
        }
    }

     */

    //Close client socket connection and close relating data streams
    public static void CloseClient(SSLSocket socket, BufferedReader br, BufferedWriter bw) {
        try {
            //Close data streams first
            if (br != null) {
                br.close();
            }
            if (bw != null) {
                bw.close();
            }
            //Close client connection last
            if (socket != null) {
                socket.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
