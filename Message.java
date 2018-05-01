package Routing;

import javax.crypto.Cipher;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Message {
  String payload;
  String header;

  // MESSAGE PROTOKOLAS: TYPE RECIPIENT_NAME SENDERS_NAME CONTENT
  public static byte[] encode(
      messageType type,
      String recipient,
      String sender,
      String content,
      boolean cipher,
      PublicKey key) {
    byte[] senderName = new byte[Constants.HEADER_NAME_LENGTH];
    senderName = Arrays.copyOf(sender.getBytes(), senderName.length);

    byte[] recipientName = new byte[Constants.HEADER_NAME_LENGTH];
    byte[] command = new byte[Constants.HEADER_NAME_LENGTH];
    byte[] message = new byte[Constants.MESSAGE_LENGTH];
    if (cipher) {
      try {
        message = encrypt(key, content);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      message = Arrays.copyOf(content.getBytes(), Constants.MESSAGE_LENGTH);
    }
    byte[] encoded = new byte[Constants.HEADER_NAME_LENGTH * 3 + Constants.MESSAGE_LENGTH];
    if (type == messageType.ONE) {
      command = Arrays.copyOf("ONE".getBytes(), command.length);
    }
    if (type == messageType.ALL) {
      command = Arrays.copyOf("ALL".getBytes(), command.length);
    }
    if (type == messageType.CLOSE) {
      command = Arrays.copyOf("CLOSE".getBytes(), command.length);
    }
    if (type == messageType.FILE) {
      command = Arrays.copyOf("FILE".getBytes(), command.length);
    }
    if (type == messageType.ACK) {
      command = Arrays.copyOf("ACK".getBytes(), command.length);
    }
    recipientName = Arrays.copyOf(recipient.getBytes(), recipientName.length);
    senderName = Arrays.copyOf(sender.getBytes(), senderName.length);
    System.arraycopy(command, 0, encoded, 0, Constants.HEADER_NAME_LENGTH);
    System.arraycopy(recipientName, 0, encoded, command.length, Constants.HEADER_NAME_LENGTH);
    System.arraycopy(
        senderName, 0, encoded, Constants.HEADER_NAME_LENGTH * 2, Constants.HEADER_NAME_LENGTH);
    System.arraycopy(
        message, 0, encoded, Constants.HEADER_NAME_LENGTH * 3, Constants.MESSAGE_LENGTH);
    return encoded;
  }

  public static String[] decode(byte[] message, int length, PrivateKey key) {
    List<String> words = new ArrayList<>();
    for (int i = 0; i < Constants.HEADER_NAME_LENGTH * 3; i += Constants.HEADER_NAME_LENGTH) {
      byte[] word = new byte[Constants.HEADER_NAME_LENGTH];
      System.arraycopy(message, i, word, 0, Constants.HEADER_NAME_LENGTH);
      words.add(new String(word));
    }
    byte[] content = new byte[Constants.MESSAGE_LENGTH];
    System.arraycopy(message, (Constants.HEADER_NAME_LENGTH * 3), content, 0, length - (Constants.HEADER_NAME_LENGTH * 3));
    if (words.get(0).trim().toString().equals("ONE")) {
      try {
        content = decrypt(key, content);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    words.add(new String(content));
    return words.toArray(new String[words.size()]);
  }

  public static byte[] encrypt(PublicKey publicKey, String message) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    return cipher.doFinal(message.getBytes());
  }

  public static byte[] decrypt(PrivateKey privateKey, byte[] encrypted) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(Cipher.DECRYPT_MODE, privateKey);
    return cipher.doFinal(encrypted);
  }

  public enum messageType {
    ALL,
    ONE,
    CLOSE,
    FILE,
    ACK
  }
}
