package com.jnngl.ping;

import com.google.gson.JsonElement;

import java.net.InetSocketAddress;
import java.util.List;

public class ServerData {

  public static class Version {
    public String name;
    public int protocol;
  }

  public static class Players {
    public static class Sample {
      public String name;
      public String id;
    }

    public int max;
    public int online;
    public List<Sample> sample;
  }

  public Version version;
  public Players players;
  public JsonElement description;
  public String favicon;
  public boolean previewsChat;
  public InetSocketAddress address;
  public long responseTime;
}
