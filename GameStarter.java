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
                System.out.println("");
                System.out.println("[1] Host a room");
                System.out.println("[2] Join a room");
                System.out.println("[3] Show rooms");
                switch (scanner.nextLine()) {
                    case "1":
                        System.out.println("\n[1] Public or [2] Private? ");
                        String pubOrPrivRes = scanner.nextLine();
                        if (pubOrPrivRes.equals("1")) {
                            System.out.print("Room name: ");
                            String roomName = scanner.nextLine();
                            if (roomName.equals("")) {
                                roomName = "I'm too lazy to think of a proper room name";
                            }
                            serverWrite.writeUTF("HOST;PUBLIC;" + roomName);
                        } else if (pubOrPrivRes.equals("2")) {
                            serverWrite.writeUTF("HOST;PRIVATE;;");
                        } else {
                            System.out.println("Invalid input");
                            break;
                        }

                        String[] hostRes = serverRead.readUTF().split(";");
                        System.out.println("Room created with roomID: " + hostRes[2]);
                        int time = 0;
                        String[] dots = new String[]{"   ", ".  ", ".. ", "..."};
                        while (!serverRead.readUTF().equals("P2_JOINED")) {
                            System.out.print("\rWaiting for player 2" + dots[time % 4]);
                            time++;
                        }
                        System.out.println("\nPlayer 2 joined. Starting game...");
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

                    case "3":
                        serverWrite.writeUTF("GET_ROOMS");
                        System.out.println("=============== PUBLIC ROOMS ===============");
                        String[] rooms = serverRead.readUTF().split(";");
                        if (rooms.length < 2) {
                            System.out.println("");
                            break;
                        }
                        for (int i = 0; i < rooms.length; i += 2) {
                            System.out.printf("[%s] - %s\n", rooms[i], rooms[i+1]);
                        }
                        break;
    
                    default:
                        break;
                }

            }
            


        } catch (IOException e) {
            e.printStackTrace();
            scanner.close();
            return;
        }

        GameFrame gameFrame = new GameFrame(socket);
        gameFrame.setupGUI();
        scanner.close();
    }
}
