package Routing;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Communicator extends java.util.Observable implements Runnable {
  private Client client;
  private int serverPort = Constants.COMMUNICATION_PORT;
  private ServerSocket serverSocket;
  private List<ConnectionHandler> connections = new ArrayList<>();
  private HashMap<String, ConnectionHandler> networkClients = new HashMap<>();
  private Controller controller;
  private volatile boolean stop = false;

  public Communicator(Controller main) {
    controller = main;
    this.client = main.getClient();
    new Thread(this).start();
  }

  @Override
  public void run() {
    try {
      setUp();
      while (!stop) {
        work();
      }
      terminateAllSockets();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean isStop() {
    return stop;
  }

  public void setStop(boolean stop) {
    this.stop = stop;
  }

  private void setUp() throws IOException {
    serverSocket = new ServerSocket(serverPort);
  }

  private void work() throws IOException {
    Socket socket = serverSocket.accept();
    System.out.println("Connected: " + socket);
    ConnectionHandler service = new ConnectionHandler(socket);
    connections.add(service);
    new Thread(service).start();
  }

  public ConnectionHandler connect(String name) {
    Socket socket;
    ConnectionHandler conHand = null;
    try {
      socket = new Socket(client.getReceiptAddress(name), Constants.COMMUNICATION_PORT);
      conHand = new ConnectionHandler(socket);
      networkClients.put(name, conHand);
      if (!connections.contains(conHand)) {
        connections.add(conHand);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return conHand;
  }

  public void send(
      Message.messageType type, String recipient, String message, boolean cipher, PublicKey key) {
    ConnectionHandler handler = networkClients.get(recipient);
    byte[] messageWithHeader =
        Message.encode(type, recipient, client.getClientName(), message, cipher, key);
    String string = new String(messageWithHeader);
    if (handler == null) {
      handler = connect(recipient);
    }
    handler.sendMessage(messageWithHeader);
  }

  public void sendAll(
      Message.messageType type, String recipient, String message, boolean cipher, PublicKey key) {
    byte[] messageWithHeader =
        Message.encode(type, recipient, client.getClientName(), message, cipher, key);
    for (String n : client.getAllKnownMembers()) {
      ConnectionHandler hand = networkClients.get(n);
      if (hand == null) {
        hand = connect(n);
      }
      hand.sendMessage(messageWithHeader);
    }
  }

  private void terminateAllSockets() {
    for (ConnectionHandler handler : connections) {
      handler.setStop(true);
    }
  }

  class ConnectionHandler implements Runnable {
    public volatile boolean stop = false;
    Socket socket;
    private BufferedReader inputReader;
    private InputStream stream;
    private OutputStream outStream;
    private PrintWriter outputWriter;

    public ConnectionHandler(Socket socket) {
      this.socket = socket;
      try {
        inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outputWriter = new PrintWriter(socket.getOutputStream(), true);
        stream = socket.getInputStream();
        outStream = socket.getOutputStream();
      } catch (IOException e) {
        System.out.println(e.getMessage());
      }
    }

    private void work() {
      try {
        // MESSAGE PROTOCOL: TYPE[0]("ALL", "ONE", "CLOSE", "FILE") RECIPIENT_NAME[1] SENDERS_NAME[2] CONTENT[3]
        if (socket.getInputStream().available() != 0) {
          byte[] messageBytes = new byte[Constants.FULL_MESSAGE_LENGTH];
          int count = stream.read(messageBytes);
          String decodedMessage[] = Message.decode(messageBytes, count, client.getPrivateKey());
          String type = decodedMessage[0];
          String name = decodedMessage[1];
          System.out.println(name.trim().toString().equals(client.getClientName().toString()));
          List<String> list = new ArrayList<>();
          list.add(decodedMessage[2]);
          list.add(decodedMessage[3]);
          if (type.trim().toString().equals("ACK")) {
            System.out.println(decodedMessage[2] + "Acknowledge our message");
          }
          if (type.trim().toString().equals("ALL")) {
            sendToAnyone(messageBytes);
            setChanged();
            notifyObservers(list);
          } else if (name.trim().toString().equals(client.getClientName().toString())
              && type.trim().toString().equals("ONE")) {
            send(
                Message.messageType.ACK,
                decodedMessage[2].toString(),
                client.getClientName(),
                false,
                null);
            setChanged();
            notifyObservers(list);
          } else if (type.trim().toString().equals("ONE")
              && !name.trim().toString().equals(client.getClientName().toString())) {
            if (client.getReceiptAddress(name) != null) {
              ConnectionHandler test = networkClients.get(name);
              if (test == null) {
                connect(name);
              }
              sendMessage(messageBytes);
            } else {
              System.out.println("Packet is lost, because no routing is found for that packet");
            }
          } else if (type.equals("CLOSE")) {
            ConnectionHandler hand = networkClients.get(decodedMessage[2]);
            if (hand != null) {
              hand.setStop(true);
            }
            networkClients.remove(hand);
            client.remove(decodedMessage[2]);
            socket.close();
            inputReader.close();
            outputWriter.close();
            stream.close();
            controller.removeFromList(decodedMessage[1].toString());
          }
        }

      } catch (IOException ex) {
        Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, "", ex);
      } catch (NullPointerException e) {
        System.out.println(e.getMessage());
      } finally {
        // outputWriter.close();
      }
    }

    @Override
    public void run() {
      while (!stop) {
        work();
      }
      terminate();
    }

    private void terminate() {
      try {
        inputReader.close();
        outputWriter.close();
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    protected void sendMessage(byte[] message) {
      try {
        outStream.write(message);
        outStream.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
      // outputWriter.println(message);
    }

    private void sendToAnyone(byte[] message) {
      for (ConnectionHandler connection : connections) {
        connection.sendMessage(message);
      }
    }

    public boolean isStop() {
      return stop;
    }

    public void setStop(boolean stop) {
      this.stop = stop;
    }
  }
}
