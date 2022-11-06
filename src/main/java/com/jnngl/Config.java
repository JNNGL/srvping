package com.jnngl;

import net.elytrium.java.commons.config.YamlConfig;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Config extends YamlConfig {

  @Ignore
  public static final File CONFIG_FILE = new File("config.yml");

  @Ignore
  public static final Config IMP = new Config();

  public int PING_PERIOD = 60;
  public String TOKEN = "";
  public String VK_TOKEN = "";
  public String TELEGRAM_USERNAME = "";
  public String TELEGRAM_TOKEN = "";
  @Comment("Use cached handshake. May cause issues with some servers.")
  public boolean FAST_HANDSHAKE = false;
  public String COMMAND_PREFIX = "!";
  public String ACTIVITY = "Playing Minecraft";
  public String STATUS = "online";
  public String DB_FILE = "servers";
  public int CHART_WIDTH = 800;
  public int CHART_HEIGHT = 600;
  public String CUSTOM_FONT = "";
  public Map<String, String> COMMAND_MAPPING = new HashMap<>();

  {
    COMMAND_MAPPING.put("add", "com.jnngl.bot.commands.AddServerCommand");
    COMMAND_MAPPING.put("добавить", "com.jnngl.bot.commands.AddServerCommand");
    COMMAND_MAPPING.put("ping", "com.jnngl.bot.commands.PingCommand");
    COMMAND_MAPPING.put("пинг", "com.jnngl.bot.commands.PingCommand");
    COMMAND_MAPPING.put("alias", "com.jnngl.bot.commands.AliasCommand");
    COMMAND_MAPPING.put("алиас", "com.jnngl.bot.commands.AliasCommand");
    COMMAND_MAPPING.put("remove", "com.jnngl.bot.commands.RemoveServerCommand");
    COMMAND_MAPPING.put("удалить", "com.jnngl.bot.commands.RemoveServerCommand");
    COMMAND_MAPPING.put("stats", "com.jnngl.bot.commands.StatsCommand");
    COMMAND_MAPPING.put("стата", "com.jnngl.bot.commands.StatsCommand");
    COMMAND_MAPPING.put("servers", "com.jnngl.bot.commands.ServersCommand");
    COMMAND_MAPPING.put("сервера", "com.jnngl.bot.commands.ServersCommand");
    COMMAND_MAPPING.put("help", "com.jnngl.bot.commands.HelpCommand");
    COMMAND_MAPPING.put("команды", "com.jnngl.bot.commands.HelpCommand");
  }

}
