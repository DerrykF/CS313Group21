import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
public class ChatServer {
  private int port; // Port number for the server socket
  private ServerSocket serverSocket; // Server socket for accepting incoming client connections
  private List < ChatClientHandler > clients; // List of connected clients
  private SecretKey secretKey; // Secret key used for encryption and decryption
  private ExecutorService executorService; // Thread pool for handling client connections
  /**
   * Constructor for the ChatServer class.
   * @param port Port number for the server socket
   */
  public ChatServer(int port) {
    
    this.port = port;
    this.clients = new ArrayList < > ();
    this.executorService = Executors.newCachedThreadPool(); // Use a thread pool to handle client connections
    try {
      this.serverSocket = new ServerSocket(port);
      System.out.println("Chat server started on port " + port);
    } catch (IOException e) {
      e.printStackTrace();
    }
    // Generate a secret key for encryption and decryption
    try {
      KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
      keyGenerator.init(128);
      this.secretKey = keyGenerator.generateKey();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  /**
   * Starts the chat server and listens for incoming client connections.
   */
  public void start() {
    while (true) {
      try {
        System.out.println("Waiting for clients to connect...");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected: " + clientSocket);
        // Create a new ChatClientHandler thread to handle the new client connection
        ChatClientHandler clientHandler = new
        ChatClientHandler(clientSocket, this, secretKey);
        clients.add(clientHandler); // Add the new client to the list of connected clients
        executorService.execute(clientHandler); // Use the thread pool to handle the new client connection
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
  /**
   * Broadcasts a message to all connected clients.
   * This method encrypts the message before sending it to each client.
   * @param message The message to broadcast
   * @param sender The ChatClientHandler that sent the message
   */
  public void broadcastMessage(byte[] message, ChatClientHandler sender) {
    for (ChatClientHandler client: clients) {
      if (client != sender) { // Don't send the message back to the sender
        client.sendMessage(message);
      }
    }
  }
  /**
   * Removes a client from the list of connected clients.
   * @param client The ChatClientHandler to remove
   */
  public void removeClient(ChatClientHandler client) {
    clients.remove(client);
  }
  /**
  * Authenticates a user with the server.
  * In a real application, you would typically use a database or other secure
  storage mechanism to store user credentials.
  * For the purposes of this example, we're just hard-coding a single
  username and password.
  * @param username The username to authenticate
  * @param password The password to authenticate
  * @return true if the user is authenticated, false otherwise
  */
  public boolean authenticate(String username, String password) {
    return username.equals("admin") && password.equals("password");
  }


}

