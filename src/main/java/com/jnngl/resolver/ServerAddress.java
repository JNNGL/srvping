package com.jnngl.resolver;

public record ServerAddress(String host, int port) {

  public static ServerAddress fromString(String ip) {
    if (ip.contains(":")) {
      String[] split = ip.split(":", 2);
      return new ServerAddress(split[0], Integer.parseInt(split[1]));
    }

    return new ServerAddress(ip, 25565);
  }

}
