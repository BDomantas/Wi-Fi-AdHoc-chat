package Routing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Arrays;

/**
 * This class advertises following packet: NAME (30 bytes) | PUBLIC_KEY (2048 bytes) |
 * ROUTING_TABLE_OBJECT It is advertised on a multicast address
 */
public class Advertiser implements Runnable {
  private Client client;
  private DatagramSocket socket;
  private InetAddress group;
  private byte[] buf = new byte[65507];
  private byte[] header = new byte[Constants.HEADER_SIZE];
  private volatile boolean stop = false;

  public Advertiser(Client client) {
    this.client = client;
  }

  public void run() {
    System.out.println("Advertiser running");
    setUp();
    while (!stop) {
      work();
    }
    terminate();
  }

  private void setUp() {
    try {
      socket = new DatagramSocket();
    } catch (SocketException e) {
      e.printStackTrace();
    }
    try {
      group = InetAddress.getByName(Constants.MULTICAST_GROUP);
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
  }

  private void work() {
    // Creating header for this message
    byte[] bytename =
        Arrays.copyOf(client.getClientName().getBytes(), Constants.HEADER_NAME_LENGTH);
    System.arraycopy(bytename, 0, header, 0, Constants.HEADER_NAME_LENGTH);
    System.arraycopy(
        client.getPubKey().getEncoded(),
        0,
        header,
        Constants.HEADER_NAME_LENGTH,
        client.getPubKey().getEncoded().length);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutput out = null;
    // Converting routing table HashMap to bytes
    try {
      bos.write(header, 0, Constants.HEADER_SIZE);
      out = new ObjectOutputStream(bos);
      out.writeObject(client.getRouting());
      out.flush();
      buf = bos.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        bos.close();
      } catch (IOException ex) {
        // ignore close exception
      }
    }
    DatagramPacket packet = new DatagramPacket(buf, buf.length, group, 4446);

    try {
      socket.send(packet);
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void terminate() {
    socket.close();
  }

  public boolean isStop() {
    return stop;
  }

  public void setStop(boolean stop) {
    this.stop = stop;
  }
}
