import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class GameStarter {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter server IP address: ");
        String ipAddress = scanner.nextLine();
        System.out.print("Enter port: ");
        int port = Integer.parseInt(scanner.nextLine());

        System.out.println("Connecting to server...");
        boolean waiting = true;
        Socket socket;
        DataInputStream serverRead;
        DataOutputStream serverWrite;
        try {
            socket = new Socket(ipAddress, port);
            serverRead = new DataInputStream(socket.getInputStream());
            serverWrite = new DataOutputStream(socket.getOutputStream());

            if (serverRead.readBoolean()) {
                System.out.println("Connected to server.");
            }

            while (waiting) {
                System.out.print("Would you like to: [1] host or [2] join a room? ");
                switch (scanner.nextLine()) {
                    case "1":
                        serverWrite.writeUTF("HOST");
                        String[] hostRes = serverRead.readUTF().split(";");
                        System.out.println("Room created with roomID: " + hostRes[2]);
                        int time = 0;
                        String[] dots = new String[]{"   ", ".  ", ".. ", "..."};
                        while (!serverRead.readUTF().equals("P2_JOINED")) {
                            System.out.print("\rWaiting for player 2" + dots[time % 4]);
                            time++;
                        }
                        System.out.println("Player 2 joined. Starting game...");
                        waiting = false;
                        break;
                    
                    case "2":
                        System.out.print("Enter roomID: ");
                        String roomID = scanner.nextLine();
                        serverWrite.writeUTF("JOIN;" + roomID);
                        System.out.println("Joining room " + roomID + "...");
    
                        String[] joinRes = serverRead.readUTF().split(";");
                        switch (joinRes[1]) {
                            case "SUCCESS":
                                System.out.println("Joined room. Starting game...");
                                waiting = false;
                                break;
                            case "INVALID_ROOM_ID":
                                System.out.println("Invalid roomID.");
                                return;
                            case "ROOM_NOT_FOUND":
                                System.out.println("Room " + roomID + " does not exist.");
                                return;
                            case "ROOM_FULL":
                                System.out.println("Room " + roomID + " is already full.");
                                return;
                            case "HOST_MISSING":
                                System.out.println("The host of room " + roomID + " has already left. Closing room...");
                                return;
                            default:
                                break;
                        }
                        break;
    
                    default:
                        break;
                }

            }
            


        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        GameFrame gameFrame = new GameFrame(socket);
        gameFrame.setupGUI();
    }
}
