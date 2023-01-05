import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("");
        System.out.println("SHU Forum");
        System.out.println("1. Login");
        System.out.println("2. Signup");
        System.out.print("Please choose an option: ");

        int option = 0;
        String username = "";
        String password = "";

        while (true) {
            try {
                option = scanner.nextInt();
                scanner.nextLine();
            } catch (InputMismatchException e) {
                System.out.print("Invalid option, please INPUT 1 or 2: ");
                scanner.nextLine();
                continue;
            }

            if (option == 1) {
                System.out.print("Enter username: ");
                username = scanner.nextLine();
                System.out.print("Enter password: ");
                password = scanner.nextLine();

                if (isValidLogin(username, password)) {
                    break;

                } else {
                    System.out.println("Invalid username or password, please start again");
                }

            } else if (option == 2) {
                System.out.print("Enter new username: ");
                username = scanner.nextLine();
                System.out.print("Enter new password: ");
                password = scanner.nextLine();
                saveAccount(username, password);
                break;

            } else if (option > 2) {
                System.out.print("Invalid option, please INPUT 1 or 2: ");
            }
        }

        Socket socket = new Socket("localhost", 8000);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        out.writeUTF(username);

        Thread thread = new Thread(new ClientThread(in));
        thread.start();

        while (true) {
            String message = scanner.nextLine();
            out.writeUTF(message);
        }
    }

    private static boolean isValidLogin(String username, String password) throws IOException {
        File file = new File("accounts.txt");
        if (!file.exists()) {
            return false;
        }
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(" ");
            if (parts[0].equals(username) && parts[1].equals(password)) {
                return true;
            }
        }
        return false;
    }

    private static void saveAccount(String username, String password) throws IOException {
        File file = new File("accounts.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
        writer.write(username + " " + password);
        writer.newLine();
        writer.close();
    }

    private static class ClientThread implements Runnable {
        DataInputStream in;

        public ClientThread(DataInputStream in) {
            this.in = in;
        }

        public void run() {
            try {
                while (true) {
                    String message = in.readUTF();
                    System.out.println(message);
                    System.out.println();
                }
            } catch (EOFException | SocketException e) {
                System.out.println("Connection closed, please try again later");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


