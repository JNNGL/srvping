package com.jnngl.ping.packet;

import com.jnngl.ping.MinecraftPacket;
import com.jnngl.ping.PacketDirection;
import com.jnngl.ping.ProtocolUtils;
import io.netty.buffer.ByteBuf;

public class StatusResponse extends MinecraftPacket {

  private String status;

  @Override
  public int getPacketID(int protocol) {
    return 0x00;
  }

  @Override
  public void read(ByteBuf src, int protocol) {
    status = ProtocolUtils.readString(src);
  }

  @Override
  public void write(ByteBuf dst, int protocol) {
    throw new IllegalStateException();
  }

  @Override
  public PacketDirection getDirection() {
    return PacketDirection.CLIENTBOUND;
  }

  public String getStatus() {
    return status;
  }
}
