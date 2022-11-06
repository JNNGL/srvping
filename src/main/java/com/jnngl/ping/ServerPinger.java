package com.jnngl.ping;

import com.jnngl.ping.pipeline.PacketDecoder;
import com.jnngl.ping.pipeline.PacketHandler;
import com.jnngl.ping.pipeline.PacketSplitter;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.function.Supplier;

public class ServerPinger {

  private static final boolean USE_EPOLL = Epoll.isAvailable();
  private static final Class<? extends Channel> CHANNEL_CLASS =
      USE_EPOLL
          ? EpollSocketChannel.class
          : NioSocketChannel.class;
  private static final Supplier<EventLoopGroup> EVENT_LOOP_GROUP_PROVIDER =
      USE_EPOLL
          ? EpollEventLoopGroup::new
          : NioEventLoopGroup::new;
  private static final EventLoopGroup DEFAULT_EVENT_LOOP = EVENT_LOOP_GROUP_PROVIDER.get();

  public static ServerData pingServer(InetSocketAddress remote, int protocol) throws Exception {
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(DEFAULT_EVENT_LOOP);
    bootstrap.channel(CHANNEL_CLASS);
    bootstrap.remoteAddress(remote);
    PacketHandler packetHandler = new PacketHandler(protocol, null);
    bootstrap.handler(new ChannelInitializer<SocketChannel>() {
      @Override
      protected void initChannel(@NotNull SocketChannel ch) {
        ch.config().setOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator());
        ch.config().setOption(ChannelOption.TCP_NODELAY, true);
        ch.config().setOption(ChannelOption.TCP_FASTOPEN_CONNECT, true);

        ch.pipeline().addLast("splitter", new PacketSplitter());
        ch.pipeline().addLast("decoder", new PacketDecoder(protocol));
        ch.pipeline().addLast("handler", packetHandler);
      }
    });

    long startTime = System.currentTimeMillis();
    bootstrap.connect().sync().channel().closeFuture().sync();

    ServerData serverData = packetHandler.getServerData();
    if (serverData == null) {
      throw new ResponseTimeoutException();
    }

    serverData.address = remote;
    serverData.responseTime = System.currentTimeMillis() - startTime;

    return serverData;
  }

}
