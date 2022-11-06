package com.jnngl.resolver;

import java.net.InetSocketAddress;
import java.util.Optional;

public class ServerNameResolver {

  public static final ServerNameResolver DEFAULT =
      new ServerNameResolver(ServerAddressResolver.DEFAULT, ServerRedirectHandler.createDnsSrvRedirectHandler());

  private final ServerAddressResolver resolver;
  private final ServerRedirectHandler redirectHandler;

  private ServerNameResolver(ServerAddressResolver resolver, ServerRedirectHandler redirectHandler) {
    this.redirectHandler = redirectHandler;
    this.resolver = resolver;
  }

  public Optional<InetSocketAddress> resolveAddress(ServerAddress address) {
    Optional<InetSocketAddress> resolved = resolver.resolve(address);
    Optional<ServerAddress> redirect = redirectHandler.lookupRedirect(address);

    if (redirect.isPresent()) {
      resolved = resolver.resolve(redirect.get());
    }

    return resolved;
  }

}
