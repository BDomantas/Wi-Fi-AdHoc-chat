package Routing;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** This class holds client's data and merges received routing tables */
public class Client extends java.util.Observable {

  private PublicKey publicKey;
  private PrivateKey privateKey;
  private List<String> neigbours;
  private ArrayList<String> allKnownMembersOnNetwork;
  private HashMap<String, InetAddress> routing;
  private HashMap<String, PublicKey> keys;
  private String name;
  private Advertiser advertiser;
  private AdvertismentsReceiver advertismentsReceiver;

  public Client(String name) {
    this.neigbours = new ArrayList<>();
    this.allKnownMembersOnNetwork = new ArrayList<>();
    this.name = name;
    routing = new HashMap<>();
    keys = new HashMap<>();
    advertiser = new Advertiser(this);
    advertismentsReceiver = new AdvertismentsReceiver(this);
    try {
      routing.put(this.name, InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()));
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    neigbours.add(name);
    KeyPair keyPair = null;
    try {
      keyPair = buildKeyPair();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    publicKey = keyPair.getPublic();
    privateKey = keyPair.getPrivate();
  }

  public static KeyPair buildKeyPair() throws NoSuchAlgorithmException {
    final int keySize = Constants.KEY_SIZE;
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(keySize);
    return keyPairGenerator.genKeyPair();
  }

  public PublicKey getPublicKey() {
    return publicKey;
  }

  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  public PublicKey getPubKey() {
    return publicKey;
  }

  public PublicKey getPubKeyOfMember(String name) {
    return keys.get(name);
  }

  public void stopAdvertismentReceiver() {
    advertismentsReceiver.setStop(true);
  }

  public Advertiser getAdvertiser() {
    return advertiser;
  }

  public void stopAdvertiser() {
    advertiser.setStop(true);
  }

  public AdvertismentsReceiver getAdvertismentsReceiver() {
    return advertismentsReceiver;
  }

  public HashMap<String, InetAddress> getRouting() {
    return routing;
  }

  /**
   * Merges received routing table with client's routing table forming new routing table
   *
   * @param receivedTable Routing table received from another node
   * @param address Address of the node from whom routing table is coming from
   * @param sendersName Name of received routing table
   */
  public void receiveHashMap(
      HashMap<String, InetAddress> receivedTable, InetAddress address, String sendersName) {
    for (HashMap.Entry<String, InetAddress> a : receivedTable.entrySet()) {
      if (!allKnownMembersOnNetwork.contains(a.getKey())) {
        allKnownMembersOnNetwork.add(a.getKey());
      }
      if (!a.getKey().equals(name)) {
        receivedTable.put(a.getKey(), address);
      }
      if (!neigbours.contains(sendersName)) {
        neigbours.add(sendersName);
      }
    }
    for (HashMap.Entry<String, InetAddress> a : routing.entrySet()) {
      if (!neigbours.contains(a.getKey())) {
        routing.put(a.getKey(), receivedTable.get(a.getKey()));
      }
      receivedTable.remove(a.getKey());
    }
    routing.putAll(receivedTable);
    routing.put(sendersName, address);
    setChanged();
    notifyObservers();
    System.out.println("I have this routing table: " + getRouting());
  }

  public void addPublicKey(String name, PublicKey key) {
    keys.put(name, key);
  }

  public String getClientName() {
    return this.name;
  }

  public InetAddress getReceiptAddress(String name) {
    return routing.get(new String(Constants.trim(name.getBytes())));
  }

  public ArrayList<String> getAllKnownMembers() {
    return this.allKnownMembersOnNetwork;
  }

  public void remove(String name) {
    routing.remove(name);
    allKnownMembersOnNetwork.remove(name);
  }
}
