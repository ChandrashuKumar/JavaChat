import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private BufferedReader reader;
    private PrintWriter writer;
    private String chattingWith = null;
    private volatile boolean running=true;

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5000;
        new ChatClient().start(host, port);
    }

    public void start(String host, int port){
        try(Scanner scanner = new Scanner(System.in)){
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            try(Socket socket = new Socket(host,port)){
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                send("LOGIN:" + username);
                String response = reader.readLine();

                if (!response.equals("LOGIN_OK")) {
                    System.out.println("Login failed: " + response);
                    return;
                }

                System.out.println("Logged in. Commands: /online, /ping, /accept, /reject, /disconnect, /exit");

                new Thread(new Runnable() {
                    public void run(){
                        receive();
                    }
                }).start();

                while (running) {
                    String input = scanner.nextLine().trim();
                    if (input.isEmpty()) continue;
                    
                    if (input.equals("/exit")) {
                        running = false;
                    } else if (input.equals("/online")) {
                        send("ONLINE");
                    } else if (input.equals("/ping")) {
                        send("PING");
                    } else if (input.equals("/accept")) {
                        send("ACCEPT");
                    } else if (input.equals("/reject")) {
                        send("REJECT");
                    } else if (input.equals("/disconnect")) {
                        send("DISCONNECT");
                    } else if (chattingWith != null) {
                        send("MSG:" + input);
                    } else {
                        System.out.println("Not in a chat. Use /ping");
                    }
                }
            }
        }
        catch (IOException e) {
            System.out.println("Connection error: " + e);
        }
    }

    public void receive(){
        String msg;
        try{
            while(running && (msg=reader.readLine())!=null){
                if(msg.startsWith("PING_FROM")){
                    System.out.println(msg.substring(10) + " wants to chat. /accept or /reject");
                }
                else if(msg.equals("PING_SENT")){
                    System.out.println("Request sent. Waiting...");
                }
                else if(msg.startsWith("ONLINE:")){
                    System.out.println("Online: " + msg.substring(7));
                }
                else if(msg.startsWith("CHAT_STARTED:")){
                    chattingWith = msg.substring(13);
                    System.out.println("Chat started with " + chattingWith);
                }
                else if(msg.startsWith("MSG:")){
                    System.out.println(msg.substring(4));
                }
                else if(msg.equals("REJECTED")){
                    System.out.println("Request rejected.");
                } 
                else if(msg.equals("CHAT_ENDED") || msg.equals("DISCONNECTED")){
                    chattingWith = null;
                    System.out.println("Chat ended.");
                }
                else if(msg.startsWith("ERROR:")) {
                    System.out.println(msg);
                }
            }
        }
        catch (IOException e) {
            if (running) System.out.println("Disconnected from server");
        }
    }

    private void send(String msg){
        writer.println(msg);
    }
}
