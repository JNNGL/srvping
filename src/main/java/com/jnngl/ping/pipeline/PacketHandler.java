package com.jnngl.ping.pipeline;

import com.google.gson.Gson;
import com.jnngl.Config;
import com.jnngl.ping.MinecraftPacket;
import com.jnngl.ping.PacketCache;
import com.jnngl.ping.ServerData;
import com.jnngl.ping.packet.Handshake;
import com.jnngl.ping.packet.StatusRequest;
import com.jnngl.ping.packet.StatusResponse;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

public class PacketHandler extends ChannelDuplexHandler {

  private final int protocol;
  private ServerData serverData;

  public PacketHandler(int protocol, ServerData serverData) {
    this.protocol = protocol;
    this.serverData = serverData;
  }

  @Override
  public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
    super.channelActive(ctx);

    if (Config.IMP.FAST_HANDSHAKE) {
      ctx.channel().writeAndFlush(Handshake.getStatusCache(protocol).getCacheBuf());
    } else {
      InetSocketAddress remote = (InetSocketAddress) ctx.channel().remoteAddress();
      ctx.channel().writeAndFlush(
        new PacketCache(
        new Handshake(protocol, remote.getHostName(), remote.getPort(), Handshake.HandshakeState.STATUS), protocol)
          .getCacheBuf());
    }
    ctx.channel().writeAndFlush(StatusRequest.getCache().getCacheBuf());
  }

  @Override
  public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
    if (!(msg instanceof MinecraftPacket packet)) {
      super.channelRead(ctx, msg);
      return;
    }

    if (packet instanceof StatusResponse response) {
      Gson gson = new Gson();
      serverData = gson.fromJson(response.getStatus(), ServerData.class);
      ctx.channel().close();
    }
  }

  public ServerData getServerData() {
    return serverData;
  }
}
