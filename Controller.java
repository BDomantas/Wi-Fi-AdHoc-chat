package Routing;

import java.util.List;
import java.util.stream.Collectors;

/** Gluing all parts of the program - advertisers, communicator anc GUI */
public class Controller {

  private NameAsker asker;
  private MainFrame mainFrame;
  private Client client;
  private String clientName;
  private Communicator communicator;
  private Thread receiverThread;
  private Thread advertiserThread;

  public Controller() {
    asker = new NameAsker(this);
  }

  public void showMain(String name) throws Exception {
    clientName = name;
    System.out.println(clientName);
    client = new Client(clientName);
    communicator = new Communicator(this);
    mainFrame = new MainFrame(this);
    mainFrame.repaint();
    mainFrame.validate();
    setUp();
  }

  private void setUp() {
    if (client != null) {
      receiverThread = new Thread(client.getAdvertismentsReceiver());
      receiverThread.start();
      advertiserThread = new Thread(client.getAdvertiser());
      advertiserThread.start();
    }
  }

  public void terminateClient() {
    if (client != null) {
      client.stopAdvertiser();
      client.stopAdvertismentReceiver();
      communicator.setStop(true);
    }
  }

  public List<String> getOnlineMembers() {
    List<String> membersWithoutSelf;
    membersWithoutSelf = client.getRouting().keySet().stream().collect(Collectors.toList());
    membersWithoutSelf.remove(clientName);
    return membersWithoutSelf;
  }

  public String getClientName() {
    return clientName;
  }

  public void send(Message.messageType type, String recipient, String message, boolean cipher) {
    if (type == Message.messageType.ONE) {
      communicator.send(type, recipient, message, cipher, client.getPubKeyOfMember(recipient));
    }
    if (type == Message.messageType.ALL) {
      communicator.sendAll(type, recipient, message, cipher, null);
    }
  }

  public Client getClient() {
    return client;
  }

  public Communicator getCommunicator() {
    return communicator;
  }

  public void removeFromList(String name) {
    mainFrame.removeFromUserList(name);
  }
}
