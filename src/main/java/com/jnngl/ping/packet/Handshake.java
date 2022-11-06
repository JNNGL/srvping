package com.jnngl.ping.packet;

import com.jnngl.ping.MinecraftPacket;
import com.jnngl.ping.PacketCache;
import com.jnngl.ping.PacketDirection;
import com.jnngl.ping.ProtocolUtils;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

public class Handshake extends MinecraftPacket {

  private static final Map<Integer, PacketCache> STATUS_CACHE = new HashMap<>();
  private static final Map<Integer, PacketCache> LOGIN_CACHE = new HashMap<>();

  public enum HandshakeState {
    UNDEFINED,
    STATUS,
    LOGIN,
  }

  private int protocolVersion;
  private String serverAddress;
  private int serverPort;
  private HandshakeState nextState;

  @Override
  public int getPacketID(int protocolVersion) {
    return 0;
  }

  public Handshake() {}

  public Handshake(int protocolVersion, String serverAddress, int serverPort, HandshakeState nextState) {
    this.protocolVersion = protocolVersion;
    this.serverAddress = serverAddress;
    this.serverPort = serverPort;
    this.nextState = nextState;
  }

  @Override
  public void write(ByteBuf dst, int protocolVersion) {
    ProtocolUtils.writeVarInt(dst, protocolVersion);
    ProtocolUtils.writeString(dst, serverAddress);
    dst.writeShort((short) serverPort);
    ProtocolUtils.writeVarInt(dst, nextState.ordinal());
  }

  @Override
  public void read(ByteBuf src, int protocolVersion) {
    throw new IllegalStateException();
  }

  @Override
  public PacketDirection getDirection() {
    return PacketDirection.SERVERBOUND;
  }

  public HandshakeState getNextState() {
    return nextState;
  }

  public int getProtocolVersion() {
    return protocolVersion;
  }

  public int getServerPort() {
    return serverPort;
  }

  public String getServerAddress() {
    return serverAddress;
  }

  public void setNextState(HandshakeState nextState) {
    this.nextState = nextState;
  }

  public void setProtocolVersion(int protocolVersion) {
    this.protocolVersion = protocolVersion;
  }

  public void setServerAddress(String serverAddress) {
    this.serverAddress = serverAddress;
  }

  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }

  public static PacketCache getStatusCache(int protocol) {
    return STATUS_CACHE.computeIfAbsent(protocol,
        p -> new PacketCache(new Handshake(p, "", 0, HandshakeState.STATUS), p));
  }

  public static PacketCache getLoginCache(int protocol) {
    return LOGIN_CACHE.computeIfAbsent(protocol,
        p -> new PacketCache(new Handshake(p, "", 0, HandshakeState.LOGIN), p));
  }

}
