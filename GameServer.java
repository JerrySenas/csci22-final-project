/**
This class handles server and room handling logic. It creates threads for each Game instance.
@author Jerry Señas (255351) and Angelico Soriano (255468)
@version May 12, 2026

I have not discussed the Java language code in my program
with anyone other than my instructor or the teaching assistants
assigned to this course.

I have not used Java language code obtained from another student,
or any other unauthorized source, either modified or unmodified.

If any Java language code or documentation used in my program
was obtained from another source, such as a textbook or website,
that has been clearly noted with a proper citation in the comments
of my program.
*/

import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GameServer {
    private ServerSocket ss;
    private static final int PORT = 7777;
    private ArrayList<Integer> roomIDs;
    private ArrayList<Room> rooms;

    /**
     * Class constructor.
     */
    public GameServer() {
        try {
            roomIDs = new ArrayList<>();
            rooms = new ArrayList<>();
            roomIDs.add(0);
            ss = new ServerSocket(GameServer.PORT);
        } catch (IOException e) {
        }
    }

    /**
     * The entry point of a GameServer.
     * @param args
     */
    public static void main(String[] args) {
        GameServer gameServer = new GameServer();
        gameServer.acceptConnections();
    }
    
    /**
     * Prints the IP address of the machine running this GameServer and continuously accepts connections from game clients.
     * 
     * Creates a Thread with a ClientHandler each time a player connects to the server
     */
    public void acceptConnections() {
        try {
            // Print ip address
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets)) {
                displayInterfaceInformation(netint);
            }
            System.out.println("Port: " + GameServer.PORT);
            System.out.println("Server ready.");

            while (true) {
                Socket socket = ss.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays information about the machine's network interface.
     * 
     * Copied from: https://docs.oracle.com/javase/tutorial/networking/nifs/listing.html
     * @param netint the machine's network interface
     * @throws SocketException
     */
    public void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        if (!netint.getInetAddresses().hasMoreElements() || !netint.isUp()) {
            return;
        }
        System.out.printf("Display name: %s\n", netint.getDisplayName());
        System.out.printf("Name: %s\n", netint.getName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            System.out.printf("InetAddress: %s\n", inetAddress);
        }
        System.out.printf("\n");
    }

    /**
     * Retrieves the Room with the given roomID. Returns null if none exist.
     * @param roomID the room's ID
     * @return the Room with the matching roomID or null if there are none
     */
    private Room findRoom(int roomID) {
        for (Room room : rooms) {
            if (room.roomID == roomID) {
                return room;
            }
        }
        return null;
    }

    /**
     * Removes the room from this GameServer's Room ArrayList
     * @param room the room to be removed
     */
    private void removeRoom(Room room) {
        rooms.remove(room);
        roomIDs.remove((Integer) room.roomID);
        System.out.printf("[%d] Room destroyed.\n", room.roomID);
    }

    /**
    This class hosts the 2 players. It passes the DataStreams of each player to a Game instance.
    */
    private class Room {
        private int numPlayers;
        private int roomID;
        private String name;
        private final boolean isPublic;
        private final DataInputStream p1In;
        private DataInputStream p2In;
        private final DataOutputStream p1Out;
        private DataOutputStream p2Out;

        /**
         * Class constructor. Assigns a random 5-digit roomID
         * @param in DataInputStream of the host
         * @param out DatatOutputStream of the host
         * @param pubOrPriv whether this room is public or private
         */
        public Room(DataInputStream in, DataOutputStream out, boolean pubOrPriv) {
            name = "";
            isPublic = pubOrPriv;
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

        /**
         * Sets the name of this room
         * @param n the name
         */
        public void setName(String n) { name = n; }

        /**
         * Lets Player 2 join the room.
         * @param in DataInputStream of Player 2
         * @param out DataOutputStream of Player 2
         * @return false if the room was already full, true otherwise
         */
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

        /**
         * Creates a new Game instance with the two players in this Room and starts its threads.
         */
        public void startGame() {
            if (p2In == null) {
                return;
            }
            Room roomRef = this;
            // Yes i am too lazy to create a dedicated listener interface shut up
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

    /**
    This class acts a shell for the player to interact with the server. A Thread containing a ClientHandler is created each time a player connects to a server and stops running when they disconnect.
    */
    private class ClientHandler implements Runnable {
        boolean running;
        Socket socket;
        DataInputStream socketIn;
        DataOutputStream socketOut;
        Room currentRoom;

        /**
         * Class constructor.
         * @param s the socket to the Player
         */
        public ClientHandler(Socket s) {
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
        
        /**
         * Handles the player's commands continuously. When a host has a Room with 2 players already, runs Room.startGame and stops running.
         */
        @Override
        public void run() {
            while (running) {
                try {
                    String[] cmd = socketIn.readUTF().split(";");
                    
                    switch (cmd[0]) {
                        case "HOST":
                            currentRoom = new Room(socketIn, socketOut, cmd[1].equals("PUBLIC"));
                            if (cmd.length > 2) {
                                currentRoom.setName(cmd[2]);
                            }
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

                        case "GET_ROOMS":
                            String roomRes = "";
                            for (Room r : rooms) {
                                if (r.isPublic) {
                                    roomRes += r.roomID;
                                    roomRes += ";";
                                    roomRes += r.name + ";";
                                }
                            }
                            socketOut.writeUTF(roomRes);
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