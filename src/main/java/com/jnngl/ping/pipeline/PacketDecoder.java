package com.jnngl.ping.pipeline;

import com.jnngl.ping.MinecraftPacket;
import com.jnngl.ping.PacketDirection;
import com.jnngl.ping.PacketRegistry;
import com.jnngl.ping.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {
  
  private final int protocol;
  
  public PacketDecoder(int protocol) {
    this.protocol = protocol;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) {
    int packetID = ProtocolUtils.readVarInt(buf);

    if (!PacketRegistry.Clientbound.STATUS.containsPacket(protocol, packetID)) {
      buf.skipBytes(buf.readableBytes());
      return;
    }
    
    MinecraftPacket packet = PacketRegistry.Clientbound.STATUS.instanceForID(protocol, packetID);

    if (packet.getDirection() == PacketDirection.SERVERBOUND) {
      throw new IllegalStateException("Received serverbound packet");
    }

    packet.read(buf, protocol);

    out.add(packet);
  }
}
