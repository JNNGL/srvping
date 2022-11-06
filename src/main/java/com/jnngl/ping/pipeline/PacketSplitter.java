package com.jnngl.ping.pipeline;

import com.jnngl.ping.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class PacketSplitter extends ByteToMessageDecoder {

  private ByteBuf buf = null;
  private int remaining = 0;

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
    while (in.readableBytes() > 0) {
      if (buf == null) {
        buf = Unpooled.buffer();
        remaining = ProtocolUtils.readVarInt(in);
      }
      int toWrite = Math.min(remaining, in.readableBytes());
      buf.writeBytes(in, toWrite);
      remaining -= toWrite;
      if (remaining == 0) {
        out.add(buf);
        buf = null;
      }
    }
  }
}
