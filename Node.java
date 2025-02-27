import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Scanner;

public class Node {
    private Ledger ledger;
    private int port;
    private String otherNodeHost;
    private int otherNodePort;

    public Node(int port, String otherNodeHost, int otherNodePort) {
        this.ledger = new Ledger();
        this.port = port;
        this.otherNodeHost = otherNodeHost;
        this.otherNodePort = otherNodePort;
    }

    // Start the node server
    public void start() {
        // Sync with existing node first
        syncWithOtherNode();
        // Start listening and adding blocks
        new Thread(this::listenForBlocks).start();
        sendBlocksToOtherNode();
    }

    // Sync by requesting the full chain from the other node
    private void syncWithOtherNode() {
        try (Socket socket = new Socket(otherNodeHost, otherNodePort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            // Send a sync request (simple string for now)
            out.writeObject("SYNC_REQUEST");
            Object response = in.readObject();
            if (response instanceof List) {
                @SuppressWarnings("unchecked")
                List<Block> receivedChain = (List<Block>) response;
                synchronized (ledger) {
                    ledger.replaceChain(receivedChain);
                    System.out.println("Synced chain with " + receivedChain.size() + " blocks.");
                }
            } else {
                System.out.println("Sync failed: unexpected response.");
            }
        } catch (IOException e) {
            System.out.println("Sync error: " + e.getMessage() + ". Starting with genesis block.");
        } catch (ClassNotFoundException e) {
            System.out.println("Sync error: invalid response format: " + e.getMessage());
        }
    }

    // Listen for incoming blocks from the other node
    private void listenForBlocks() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Node listening on port " + port + " at " + InetAddress.getLocalHost().getHostAddress());
            while (true) {
                Socket clientSocket = null;
                try {
                    clientSocket = serverSocket.accept(); // Accept incoming connection
                    try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                     ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
                        Object receivedObject = in.readObject(); // Read the object
                        
                        if ("SYNC_REQUEST".equals(receivedObject)) {
                            synchronized (ledger) {
                                out.writeObject(ledger.getChain());
                                out.flush();
                                System.out.println("Sent full chain of " + ledger.getChain().size() + " blocks for sync.");
                            }
                        }
                        else if (receivedObject instanceof Block) { // Verify it's a Block
                            Block receivedBlock = (Block) receivedObject;
                            synchronized (ledger) {
                                if (ledger.isValidNewBlock(receivedBlock)) { // Use Ledger's validation
                                    ledger.addBlock(receivedBlock);
                                    System.out.println("Received and added block: " + receivedBlock.getHash());
                                } else {
                                    System.out.println("Received invalid block: " + receivedBlock.getHash());
                                }
                            }
                        } else {
                            System.out.println("Received non-Block object, ignoring.");
                        }
                    }
                } catch (IOException e) {
                    System.out.println("IO error while reading block: " + e.getMessage());
                } catch (ClassNotFoundException e) {
                    System.out.println("Class not found during deserialization: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Unexpected error processing block: " + e.getMessage());
                } finally {
                    if (clientSocket != null && !clientSocket.isClosed()) {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            System.out.println("Error closing client socket: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Server socket error: " + e.getMessage());
        }
    }
    // Send a new block to the other node
    private void sendBlock(Block block) {
        try (Socket socket = new Socket(otherNodeHost, otherNodePort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            out.writeObject(block);
            System.out.println("Sent block to " + otherNodeHost + ":" + otherNodePort);
        } catch (IOException e) {
            System.out.println("Error sending block: " + e.getMessage());
        }
    }

    // Simulate initial sync or manual block addition
    private void sendBlocksToOtherNode() {
        Scanner scanner = new Scanner(System.in);
        // Initial sync: send genesis block if needed
        synchronized (ledger) {
            if (ledger.getChain().size() == 1) { // Only genesis exists
                sendBlock(ledger.getChain().get(0));
            }
        }
        while (true) {
            System.out.println("Enter data to add to ledger (or 'exit' to stop):");
            String data = scanner.nextLine();
            if ("exit".equalsIgnoreCase(data)) break;
            Block newBlock = new Block(data, ledger.getChain().get(ledger.getChain().size() - 1).getHash());
            synchronized (ledger) {
                ledger.addBlock(newBlock);
                sendBlock(newBlock);
            }
        }
        scanner.close();
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            //calling format ->  java .\Node.java 8080 8081 localhost 8081
            System.out.println("Usage: java Node <port> <otherNodeHost> <otherNodePort>");
            System.exit(1);
        }
        // Example: Node 1 on port 8080, syncing with Node 2 on localhost:8081
        Node node1 = new Node(Integer.parseInt(args[0]), "localhost", Integer.parseInt(args[1]));
        node1.start();

        // Run Node 2 in a separate process or JVM in real use
        // Node node2 = new Node(8081, "localhost", 8080);
        // node2.start();
    }
}