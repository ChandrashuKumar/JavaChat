//listen to active clients using socket server
//if a client connects, start a thread to handle it
//handle client input and output
import java.io.*;
import java.net.*;

public class ChatServer {
    private static int PORT = 6000;
    static ClientHandler client1 = null;
    static ClientHandler client2 = null;
    public static boolean isChatActive = false;

    public static void main(String[] args){
        System.out.println("Server started on port " + PORT);
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            while(true){
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        }
        catch(IOException e){
            System.out.println(e);
        }
    }

    static void remove(ClientHandler client){
        if(client == client1) client1 = null;
        else if(client == client2) client2 = null;
        isChatActive = false;
        System.out.println(client.username + " left");
    }

    static synchronized boolean register(ClientHandler client){
        if(client1==null){
            client1=client;
            System.out.println(client.username + " joined");
        }
        else if(client2==null){
            client2=client;
            System.out.println(client.username + " joined");
        }
        else return false;
        
        return true;
    }

    static String getOtherUser(ClientHandler client){
        if(client == client1 && client2 != null) return client2.username;
        if(client == client2 && client1 != null) return client1.username;
        return null;
    }

    static ClientHandler getOther(ClientHandler client){
        return (client == client1) ? client2 : client1;
    }

    static synchronized void startChat() {
        if(!isChatActive){
            isChatActive = true;
            client1.send("CHAT_STARTED:" + client2.username);
            client2.send("CHAT_STARTED:" + client1.username);
        }
    }

    static synchronized void endChat(ClientHandler handler) {
        if (isChatActive) {
            isChatActive = false;
            ClientHandler other = getOther(handler);
            if (other != null) other.send("CHAT_ENDED");
        }
    }

    static synchronized void routeMessage(ClientHandler from, String message) {
        if (isChatActive) {
            ClientHandler target = getOther(from);
            if (target != null) target.send("MSG:" + from.username + ": " + message);
        }
    }

}

class ClientHandler implements Runnable{
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    String username;

    public ClientHandler(Socket socket) throws IOException{
        this.socket=socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
    }

    public void run(){
        try{
            String msg;
            while ((msg = reader.readLine()) != null) {
                handle(msg);
            }
        }
        catch(IOException e){
        }
        finally{
            ChatServer.remove(this);
            try{
                socket.close();
            }
            catch(IOException e){
                System.out.println(e);
            }
        }
    }

    private void handle(String msg){
        if (msg.startsWith("LOGIN:")) {
            username = msg.substring(6);
            if (ChatServer.register(this)) {
                send("LOGIN_OK");
            } else {
                send("ERROR:Server full");
            }
        }
        else if (msg.equals("ONLINE")) {
            String other = ChatServer.getOtherUser(this);
            send("ONLINE:" + (other != null ? other : "none"));
        }
        else if (msg.equals("PING")){
            if (ChatServer.getOtherUser(this) == null) send("ERROR:No one else online");
            else if (ChatServer.isChatActive) send("ERROR:Already in chat");
            else {
                ChatServer.getOther(this).send("PING_FROM:" + username);
                send("PING_SENT");
            }
        }
        else if(msg.equals("ACCEPT")){
            ChatServer.startChat();
        }
        else if (msg.equals("REJECT")) {
            ClientHandler other = ChatServer.getOther(this);
            if (other != null) other.send("REJECTED");
        }
        else if (msg.startsWith("MSG:")) {
            ChatServer.routeMessage(this, msg.substring(4));
        }
        else if (msg.equals("DISCONNECT")) {
            ChatServer.endChat(this);
            send("DISCONNECTED");
        }
        
    }

    void send(String msg){
        writer.println(msg);
    }
}
