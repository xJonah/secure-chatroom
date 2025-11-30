import javax.net.ssl.SSLSocket;
import java.io.*;
import java.util.ArrayList;

public class ClientHandler implements Runnable{

    //Fields
    private SSLSocket socket;
    private BufferedReader br;
    private BufferedWriter bw;
    public static ArrayList<ClientHandler> clients = new ArrayList<>();
    private String clientName;
    private String clientPassword;
    public static final String SERVER_PASSWORD = "ChatRoom55!";

    //Constructor
    public ClientHandler(SSLSocket socket) {
        try {
            this.socket = socket;

            //Convert byte streams to character streams
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            clients.add(this); //Add client to the list of clients

            //Retrieve identity and password provided by client
            this.clientName = br.readLine();
            this.clientPassword = br.readLine();

            //Password server validation
            if (!clientPassword.equals(SERVER_PASSWORD)) {
                CloseClient(socket, br, bw);
            }
        }
        //Remove client on error
        catch (IOException e) {
            CloseClient(socket, br, bw);
        }
    }

    public static String GetPassword() {
        return SERVER_PASSWORD;
    }

    //Listen for messages on a separate thread
    @Override
    public void run() {
        String message;

        //While connected to the chat room, client can receive messages
        while(socket.isConnected()) {
            try {
                message = br.readLine();
                SendMessage(message);
            }
            //Remove client on error
            catch (IOException e) {
                CloseClient(socket, br, bw);
                break;
            }
        }
    }

    //Send message to other clients in room
    public void SendMessage(String message) {
        //Find other clients in the room
        for (ClientHandler client : clients) {
            try {
                //Message not sent to self
                if (!client.clientName.equals(clientName)) {
                    if(message.length() <= 300) {
                        client.bw.write(message);
                        client.bw.newLine();
                        client.bw.flush();
                    }
                }
            }
            //Remove client on error
            catch (IOException e) {
                CloseClient(socket, br, bw);
            }
        }
    }

    //Remove client from room members list, close socket connection, and close relating data streams
    public void CloseClient(SSLSocket socket, BufferedReader br, BufferedWriter bw) {
        clients.remove(this);
        SendMessage("Server: " + clientName + " has left.");

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
