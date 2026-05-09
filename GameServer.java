import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GameServer {
    private ServerSocket ss;
    private ArrayList<Integer> roomIDs;
    private ArrayList<Room> rooms;

    public GameServer() {
        try {
            roomIDs = new ArrayList<>();
            rooms = new ArrayList<>();
            roomIDs.add(0);
            ss = new ServerSocket(7777);
        } catch (IOException e) {
        }
    }
    
    public void acceptConnections() {
        try {
            while (true) {
                // System.out.println("Awaiting players...");
                // while (numPlayers < REQUIRED_PLAYERS) {
                //     numPlayers++;
                    
                //     Socket socket = ss.accept();
                //     DataInputStream in = new DataInputStream(socket.getInputStream());
                //     if (numPlayers == 1) {
                //         p1Out = new DataOutputStream(socket.getOutputStream());
                //     } else {
                //         p2Out = new DataOutputStream(socket.getOutputStream());
                //     }
                // }
                Socket socket = ss.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (Exception e) {
        }
    }

    private Room findRoom(int roomID) {
        for (Room room : rooms) {
            if (room.roomID == roomID) {
                return room;
            }
        }
        return null;
    }

    private void removeRoom(Room room) {
        rooms.remove(room);
        roomIDs.remove((Integer) room.roomID);
        System.out.printf("[%d] Room destroyed.\n", room.roomID);
    }

    public static void main(String[] args) {
        GameServer gameServer = new GameServer();
        gameServer.acceptConnections();
    }

    private class Room {
        private int numPlayers;
        private int roomID;
        private DataInputStream p1In;
        private DataInputStream p2In;
        private DataOutputStream p1Out;
        private DataOutputStream p2Out;

        public Room(DataInputStream in, DataOutputStream out) {
            int id = 0;
            while (roomIDs.contains(id)) {
                id = ThreadLocalRandom.current().nextInt(10000, 100000);
            }
            roomID = id;
            roomIDs.add(roomID);
            
            p1In = in;
            p1Out = out;
            numPlayers = 1;
            System.out.printf("[%d] Room created.\n", roomID);
        }

        public boolean p2Join(DataInputStream in, DataOutputStream out) {
            if (p2In == null) {
                p2In = in;
                p2Out = out;
                numPlayers = 2;
                System.out.printf("[%d] Player 2 joined.\n", roomID);
                return true;
            } else {
                return false;
            }
        }

        public void startGame() {
            Room roomRef = this;
            Game game = new Game(p1Out, p2Out, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    removeRoom(roomRef);
                }
            });
            game.startThreads(p1In, p2In);
            game.startGame();
        }
    }

    private class ClientHandler implements Runnable {
        boolean running;
        Socket socket;
        DataInputStream socketIn;
        DataOutputStream socketOut;
        Room currentRoom;

        ClientHandler(Socket s) {
            running = true;
            socket = s;
            try {
                socketIn = new DataInputStream(socket.getInputStream());
                socketOut = new DataOutputStream(socket.getOutputStream());
                // Connection established
                socketOut.writeBoolean(true);
            } catch (IOException e) {
                System.out.println("Client connection error:");
                e.printStackTrace();
            }
        }
        
        @Override
        public void run() {
            while (running) {
                try {
                    String[] cmd = socketIn.readUTF().split(";");
                    
                    switch (cmd[0]) {
                        case "HOST":
                            currentRoom = new Room(socketIn, socketOut);
                            rooms.add(currentRoom);
                            socketOut.writeUTF("ROOM;CREATED;" + currentRoom.roomID);

                            while (currentRoom.numPlayers < 2) { 
                                try {
                                    socketOut.writeUTF("u alive bro");
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                }
                            }
                            running = false;
                            currentRoom.p1Out.writeUTF("P2_JOINED");
                            currentRoom.startGame();
                            break;
        
                        case "JOIN":
                            int roomID;
        
                            try {
                                roomID = Integer.parseInt(cmd[1]);
                            } catch (NumberFormatException e) {
                                socketOut.writeUTF("CONNECT;INVALID_ROOM_ID");
                                break;
                            }
        
                            Room room = findRoom(roomID);
        
                            if (room == null || roomID == 0) {
                                socketOut.writeUTF("CONNECT;ROOM_NOT_FOUND");
                                break;
                            }
        
                            if (!room.p2Join(socketIn, socketOut)) {
                                socketOut.writeUTF("CONNECT;ROOM_FULL");
                                break;
                            }
        
                            if (room.numPlayers < 2) {
                                socketOut.writeUTF("CONNECT;HOST_MISSING");
                            }
                            socketOut.writeUTF("CONNECT;SUCCESS");
                            running = false;
                            break;
        
                        default:
                            System.out.println("ERROR;INVALID");
                    }
                } catch ( EOFException | SocketException e ) {
                    System.out.println("Player disconnected");
                    if (currentRoom != null) { removeRoom(currentRoom); }
                    running = false;
                } catch (IOException e) {
                    running = false;
                    e.printStackTrace();
                }
            }
        }
    }
}