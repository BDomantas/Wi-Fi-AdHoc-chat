package Routing;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

public class AdvertismentsReceiver implements Runnable {
  private Client client;
  private int port;
  private InetAddress group;
  private MulticastSocket socket;
  private byte[] head = new byte[Constants.HEADER_SIZE];
  private byte[] buf = new byte[65507];
  private volatile boolean stop = false;

  public AdvertismentsReceiver(Client client) {
    this.client = client;
    this.port = Constants.MULTICAST_PORT;
  }

  public void run() {
    System.out.println("Advertisment receiver is working");
    setUp();
    while (!stop) {
      work();
    }
    terminate();
  }

  private void setUp() {
    try {
      this.group = InetAddress.getByName(Constants.MULTICAST_GROUP);
      socket = new MulticastSocket(Constants.MULTICAST_PORT);
      socket.joinGroup(group);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void work() {
    byte[] afterHead = new byte[65507];
    byte[] sendersName = new byte[Constants.HEADER_NAME_LENGTH];
    byte[] receivedPublicKey = new byte[Constants.KEY_SIZE];
    DatagramPacket packet = new DatagramPacket(buf, buf.length);
    try {
      socket.receive(packet);
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (packet.getData() != null) {
      System.arraycopy(packet.getData(), 0, head, 0, Constants.HEADER_SIZE);
      System.arraycopy(packet.getData(), Constants.HEADER_SIZE, afterHead, 0, packet.getLength() - Constants.HEADER_SIZE);
      System.arraycopy(head, 0, sendersName, 0, Constants.HEADER_NAME_LENGTH);
      System.arraycopy(head, Constants.HEADER_NAME_LENGTH, receivedPublicKey,0, Constants.HEADER_SIZE - Constants.HEADER_NAME_LENGTH);
      sendersName = Constants.trim(sendersName);
      try {
        if (!packet
            .getAddress()
            .toString()
            .equals("/" + InetAddress.getLocalHost().getHostAddress())) {
          HashMap<String, InetAddress> receivedTable;
          ByteArrayInputStream bis = new ByteArrayInputStream(afterHead);
          ObjectInput in = null;
          try {
            in = new ObjectInputStream(bis);
            receivedTable = (HashMap<String, InetAddress>) in.readObject();
            client.receiveHashMap(receivedTable, packet.getAddress(), new String(sendersName));
            try {
              PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(receivedPublicKey));
              client.addPublicKey(new String(sendersName), publicKey);
            } catch (InvalidKeySpecException e) {
              e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
              e.printStackTrace();
            }
          } catch (ClassNotFoundException e) {
            e.printStackTrace();
          } catch (EOFException e) {
            System.out.println("Something went wrong");
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          } finally {
            try {
              if (in != null) {
                in.close();
              }
            } catch (IOException ex) {
              // ignore close exception
            }
          }
        }
      } catch (UnknownHostException e) {
        e.printStackTrace();
      }
    }
  }

  private void terminate() {
    try {
      socket.leaveGroup(group);
    } catch (IOException e) {
      e.printStackTrace();
    }
    socket.close();
  }

  public boolean isStop() {
    return stop;
  }

  public void setStop(boolean stop) {
    this.stop = stop;
  }
}
