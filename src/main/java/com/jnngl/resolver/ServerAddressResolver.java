package com.jnngl.resolver;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Optional;

public interface ServerAddressResolver {

  ServerAddressResolver DEFAULT = (address) -> {
    try {
      InetAddress inetAddress = InetAddress.getByName(address.host());
      return Optional.of(new InetSocketAddress(inetAddress, address.port()));
    } catch (UnknownHostException e) {
      return Optional.empty();
    }
  };

  Optional<InetSocketAddress> resolve(ServerAddress address);
}
