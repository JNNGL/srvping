package com.jnngl.ping;

import io.netty.buffer.ByteBuf;

public abstract class MinecraftPacket {

  public abstract int getPacketID(int protocol);
  public abstract void write(ByteBuf dst, int protocol);
  public abstract void read(ByteBuf src, int protocol);
  public abstract PacketDirection getDirection();

}
