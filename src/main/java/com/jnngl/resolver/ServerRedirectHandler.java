package com.jnngl.resolver;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.Optional;

public interface ServerRedirectHandler {

  Optional<ServerAddress> lookupRedirect(ServerAddress address);

  static ServerRedirectHandler createDnsSrvRedirectHandler() {
    DirContext dirContext;

    try {
      Class.forName("com.sun.jndi.dns.DnsContextFactory");
      Hashtable<String, String> hashtable = new Hashtable<>();
      hashtable.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
      hashtable.put("java.naming.provider.url", "dns:");
      hashtable.put("com.sun.jndi.dns.timeout.retries", "1");
      dirContext = new InitialDirContext(hashtable);
    } catch (NamingException | ClassNotFoundException e) {
      return a -> Optional.empty();
    }

    return address -> {
      try {
        if (address.port() == 25565) {
          Attributes attributes = dirContext.getAttributes("_minecraft._tcp." + address.host(), new String[]{"SRV"});
          Attribute attribute = attributes.get("srv");
          if (attribute != null) {
            String[] split = attribute.get().toString().split(" ", 4);
            return Optional.of(new ServerAddress(split[3], Integer.parseInt(split[2])));
          }
        }
      } catch (NamingException ignored) {}
      return Optional.empty();
    };
  }

}
