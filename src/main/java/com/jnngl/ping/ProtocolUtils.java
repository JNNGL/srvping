package com.jnngl.ping;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class ProtocolUtils {

  public static final int SEGMENT_BITS = 0x7F;
  public static final int CONTINUE_BIT = 0x80;

  public static int readVarInt(ByteBuf target) {
    int value = 0;
    int position = 0;
    byte currentByte;

    while (true) {
      currentByte = target.readByte();
      value |= (long) (currentByte & SEGMENT_BITS) << position;

      if ((currentByte & CONTINUE_BIT) == 0) {
        break;
      }

      position += 7;

      if (position >= 32) {
        throw new RuntimeException("Couldn't read var int");
      }
    }

    return value;
  }

  public static long readVarLong(ByteBuf target) {
    long value = 0;
    int position = 0;
    byte currentByte;

    while (true) {
      currentByte = target.readByte();
      value |= (long) (currentByte & SEGMENT_BITS) << position;

      if ((currentByte & CONTINUE_BIT) == 0) {
        break;
      }

      position += 7;

      if (position >= 64) {
        throw new RuntimeException("Couldn't read var int");
      }
    }

    return value;
  }

  public static ByteBuf writeVarLong(ByteBuf target, long value) {
    while (true) {
      if ((value & ~((long) SEGMENT_BITS)) == 0) {
        target.writeByte((byte) value);
        return target;
      }

      target.writeByte((byte) ((value & SEGMENT_BITS) | CONTINUE_BIT));
      value >>>= 7;
    }
  }

  public static ByteBuf writeVarInt(ByteBuf target, int value) {
    while (true) {
      if ((value & ~SEGMENT_BITS) == 0) {
        target.writeByte((byte) value);
        return target;
      }

      target.writeByte((value & SEGMENT_BITS) | CONTINUE_BIT);
      value >>>= 7;
    }
  }

  public static ByteBuf writeString(ByteBuf target, String value) {
    writeVarInt(target, value.length());
    target.writeBytes(value.getBytes(StandardCharsets.UTF_8));
    return target;
  }

  public static String readString(ByteBuf target) {
    byte[] raw = new byte[readVarInt(target)];
    target.readBytes(raw);
    return new String(raw, StandardCharsets.UTF_8);
  }

}
