package com.jnngl.ping;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PacketCache {

  private final byte[] cache;

  public PacketCache(MinecraftPacket packet, int protocol) {
    if (packet.getDirection() != PacketDirection.SERVERBOUND) {
      throw new IllegalArgumentException(packet.getDirection().toString());
    }

    ByteBuf raw = Unpooled.buffer();
    ProtocolUtils.writeVarInt(raw, packet.getPacketID(protocol));
    packet.write(raw, protocol);

    ByteBuf prepended = Unpooled.buffer();
    ProtocolUtils.writeVarInt(prepended, raw.readableBytes());
    prepended.writeBytes(raw);

    cache = new byte[prepended.readableBytes()];

    prepended.getBytes(0, cache);
    prepended.release();
  }

  public ByteBuf getCacheBuf() {
    return Unpooled.buffer().writeBytes(cache);
  }

  public byte[] getCacheBytes() {
    return cache;
  }
}
