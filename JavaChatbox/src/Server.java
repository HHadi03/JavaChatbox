import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Server {

    private static List<Socket> sockets = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8000);
        System.out.println("Server running...");

        while (true) {
            Socket socket = serverSocket.accept();
            sockets.add(socket);
            new Thread(new ServerThread(socket)).start();
        }
    }

    private static class ServerThread implements Runnable {
        Socket socket;
        String name;
        DataInputStream in;
        DataOutputStream out;

        public ServerThread(Socket socket) {
            this.socket = socket;
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
                name = in.readUTF();
                System.out.println(name + " has connected");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                sendMessage(name + " has joined the chatroom");
                while (true) {
                    String message;
                    try {
                        message = in.readUTF();
                    } catch (SocketException e) {
                        sendMessage(name + " has left the chatroom");
                        break;
                    }
                    sendMessage(name + ": " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                System.out.println(name + " has disconnected");
                sockets.remove(socket);
            }
        }

        //https://www.baeldung.com/java-concurrentmodificationexception
        private void sendMessage(String message) throws IOException {
            Iterator<Socket> iterator = sockets.iterator();
            while (iterator.hasNext()) {
                Socket socket = iterator.next();
                try {
                    new DataOutputStream(socket.getOutputStream()).writeUTF(message);
                } catch (SocketException e) {
                    iterator.remove();
                }
            }
        }
    }
}