package com.jnngl.ping;

import com.jnngl.ping.packet.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@SuppressWarnings("unchecked")
public class PacketRegistry {

  public static class Clientbound {
    public static final PacketRegistry HANDSHAKE = new PacketRegistry();
    public static final PacketRegistry STATUS = new PacketRegistry(StatusResponse::new);
    public static final PacketRegistry LOGIN = new PacketRegistry();
    public static final PacketRegistry PLAY = new PacketRegistry();
  }

  private final Map<Integer, Map<Integer, Supplier<? extends MinecraftPacket>>> suppliers;
  
  private PacketRegistry(Supplier<? extends MinecraftPacket>... providers) {
    this.suppliers = new HashMap<>();
    IntStream protocols = IntStream.rangeClosed(4, 760);
    protocols.forEach(protocol -> {
      Map<Integer, Supplier<? extends MinecraftPacket>> mapping = new HashMap<>();
      Arrays.stream(providers).forEach(supplier -> mapping.put(supplier.get().getPacketID(protocol), supplier));
      suppliers.put(protocol, mapping);
    });
  }
  
  public Supplier<? extends MinecraftPacket> getSupplierForID(int protocol, int id) {
    return suppliers.get(protocol).get(id);
  }
  
  public MinecraftPacket instanceForID(int protocol, int id) {
    return getSupplierForID(protocol, id).get();
  }

  public boolean containsPacket(int protocol, int id) {
    return suppliers.containsKey(protocol) && suppliers.get(protocol).containsKey(id);
  }

}
