package com.jnngl.ping.packet;

import com.jnngl.ping.MinecraftPacket;
import com.jnngl.ping.PacketCache;
import com.jnngl.ping.PacketDirection;
import io.netty.buffer.ByteBuf;

public class StatusRequest extends MinecraftPacket {

  private static final PacketCache CACHE = new PacketCache(new StatusRequest(), 760);

  @Override
  public int getPacketID(int protocol) {
    return 0x00;
  }

  @Override
  public void write(ByteBuf dst, int protocol) {}

  @Override
  public void read(ByteBuf src, int protocol) {
    throw new IllegalStateException();
  }

  @Override
  public PacketDirection getDirection() {
    return PacketDirection.SERVERBOUND;
  }

  public static PacketCache getCache() {
    return CACHE;
  }
}
