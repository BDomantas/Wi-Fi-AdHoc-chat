package Routing;

import java.util.Arrays;

public class Constants {
  public static final String MULTICAST_GROUP = "224.0.0.1";
  public static final int MULTICAST_PORT = 4446;
  public static final int HEADER_SIZE = 1064;
  public static final int HEADER_NAME_LENGTH = 30;
  public static final int COMMUNICATION_PORT = 9009;
  public static final int MESSAGE_LENGTH = 256;
  public static final int KEY_SIZE = 2048;
  public static final int FULL_MESSAGE_LENGTH = MESSAGE_LENGTH + (HEADER_NAME_LENGTH * 3);

  public static byte[] trim(byte[] bytes) {
    int i = bytes.length - 1;
    while (i >= 0 && bytes[i] == 0) {
      --i;
    }
    return Arrays.copyOf(bytes, i + 1);
  }
}
