package com.openxc.messages.formatters;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Utility functions to read byte arrays from JSON strings.
 */
public class ByteAdapter extends TypeAdapter<byte[]> {

    public byte[] read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
          reader.nextNull();
          return null;
        }
        String payload = reader.nextString();
        payload = payload.replace("\"", "").replace("0x", "");
        return hexStringToByteArray(payload);
      }

      public void write(JsonWriter writer, byte[] bytes) throws IOException {
        if (bytes == null) {
          writer.nullValue();
          return;
        }
        writer.value(byteArrayToHexString(bytes));
      }

      //adapted from stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
      private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
      public static String byteArrayToHexString(byte[] bytes) {
          if(bytes == null) {
              return "";
          } else {
          }
              char[] hexChars = new char[bytes.length * 2];
              for (int j = 0; j < bytes.length; j++) {
                  int v = bytes[j] & 0xFF;
                  hexChars[j * 2] = hexArray[v >>> 4];
                  hexChars[j * 2 + 1] = hexArray[v & 0x0F];
              }
              return new String(hexChars);
      }

      //adapted from stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
      //the benefit of this over using a BigInteger(s, 16).toByteArray() is that leading zeroes are not lost
      public static byte[] hexStringToByteArray(String s) {
          int length = s.length();
          byte[] data = new byte[length / 2];
          for (int i = 0; i < length; i += 2) {
              data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                   + Character.digit(s.charAt(i+1), 16));
          }
          return data;
      }
}
