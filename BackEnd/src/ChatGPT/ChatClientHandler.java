package ChatGPT;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
public class ChatClientHandler implements Runnable {
  private Socket clientSocket; // Socket for the connected client
  private ChatServer chatServer; // Reference to the ChatServer object
  private PrintWriter out; // Output stream for sending messages to the client
  private BufferedReader in ; // Input stream for receiving messages from the client
  private String username; // Username of the connected client
  private SecretKey secretKey; // Secret key used for encryption and decryption
  /**
   * Constructor for the ChatClientHandler class.
   * @param clientSocket Socket for the connected client
   * @param chatServer Reference to the ChatServer object
   * @param secretKey Secret key used for encryption and decryption
   */
  public ChatClientHandler(Socket clientSocket, ChatServer chatServer,
    SecretKey secretKey) {
    this.clientSocket = clientSocket;
    this.chatServer = chatServer;
    this.secretKey = secretKey;
  }
  /**
  * The run method for the ChatClientHandler thread.
  * This method handles incoming messages from the client and broadcasts them
  to all connected clients.
  */
  public void run() {
      try {
        // Get the input and output streams for the client socket
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        // Prompt the user for their username and password
        out.println("Welcome to the chat room! Please enter your username: ");
          username = in.readLine(); out.println("Please enter your password:"); String password = in.readLine();
          // Authenticate the user
          if (!chatServer.authenticate(username, password)) {
            out.println("Invalid username or password. Disconnecting.");
            clientSocket.close();
            return;
          }
          out.println("Welcome to the chat room, " + username + "!"); String inputLine;
          while ((inputLine = in.readLine()) != null) {
            System.out.println("Received message from " + username + ": " +
              inputLine);
            // Encrypt the message and broadcast it to all connected clients
            byte[] encryptedMessage = encrypt(inputLine.getBytes(),
              secretKey);
            chatServer.broadcastMessage(encryptedMessage, this);
          }
          // Remove the client from the list of connected clients and close
          //the client socket 
          chatServer.removeClient(this);
          clientSocket.close();
          System.out.println("Client disconnected: " + clientSocket);
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
      /**
      * Sends a message to the connected client.
      * This method decrypts the encrypted message before sending it to the
      client.
      * @param message The encrypted message to send to the client
      */
      public void sendMessage(byte[] message) {
        out.println(new String(decrypt(message, secretKey)));
      }
      /**
      * Encrypts a byte array using the AES encryption algorithm and the provided
      secret key.
      * @param plaintext The plaintext to be encrypted
      * @param secretKey The secret key to use for encryption
      * @return The encrypted byte array
      */
      private byte[] encrypt(byte[] plaintext, SecretKey secretKey) {
        try {
          Cipher cipher = Cipher.getInstance("AES");
          cipher.init(Cipher.ENCRYPT_MODE, secretKey);
          return cipher.doFinal(plaintext);
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      }

      private byte[] decrypt(byte[] ciphertext, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
