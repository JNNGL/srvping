package com.jnngl;

import net.elytrium.java.commons.config.YamlConfig;

import java.io.File;

public class Messages extends YamlConfig {

  @Ignore
  public static final File CONFIG_FILE = new File("messages.yml");

  @Ignore
  public static final Messages IMP = new Messages();

  @Placeholders("ip")
  public String COULDNT_RESOLVE = "Не получается резолвить {IP}";

  @Create
  public ADD_SERVER ADD_SERVER;

  public static class ADD_SERVER {
    public String USAGE = "Использование:{NL} > `<ip[:порт]|алиас> [протокол] [без владельца (true или false)]`";

    @Placeholders("server")
    public String SERVER_ADDED = "Добавлен сервер '{SERVER}'";

    @Placeholders("server")
    public String COULDNT_PING = "Сервер '{SERVER}' не был добавлен, т.к. не получилось его пингануть";

    @Placeholders("server")
    public String ALREADY_EXISTS = "Сервер '{SERVER}' уже существует";
  }

  @Create
  public PING PING;

  public static class PING {
    public String USAGE = "Использование:{NL} > `<ip[:порт]|алиас> [протокол]`";

    @Placeholders("server")
    public String PINGING_SERVER = "Пингую сервер {SERVER}...";
    public String SERVER_DATA = "Информация о сервере";
    public String IP = "IP";
    public String PING = "Время ответа";
    public String VERSION_NAME = "Версия";
    public String ONLINE = "Онлайн";
    public String DESCRIPTION = "Описание";
  }

  @Create
  public REMOVE_SERVER REMOVE_SERVER;

  public static class REMOVE_SERVER {
    public String USAGE = "Использование:{NL} > `<ip[:порт]|алиас> [оставлять записи об онлайне (true или false)]`";

    @Placeholders("server")
    public String SERVER_REMOVED = "Сервер '{SERVER}' был удален";

    @Placeholders("server")
    public String SERVER_AND_ENTRIES_REMOVED = "Сервер '{SERVER}' и записи об онлайне были удалены";

    public String NOT_ENOUGH_PERMISSIONS = "Недостаточно прав для удаления сервера";
  }

  @Create
  public ALIAS ALIAS;

  public static class ALIAS {
    public String USAGE = "Использование: {NL} > Добавить алиас: `<алиас> <ip>` {NL} > Адрес алиаса: `<алиас>` {NL} > Удалить алиас: `<алиас> null`";

    @Placeholders("alias")
    public String ALIAS_NOT_FOUND = "Алиас '{ALIAS}' не найден";

    @Placeholders({"alias", "ip"})
    public String ALIAS_ADDRESS = "'{ALIAS}' -> {IP}";

    @Placeholders({"alias", "ip"})
    public String ALIAS_ADDED = "К серверу {IP} добавлен алиас '{ALIAS}'";

    @Placeholders("alias")
    public String ALIAS_REMOVED = "Алиас '{ALIAS}' удален";

    @Placeholders("alias")
    public String ALREADY_EXISTS = "Алиас '{ALIAS}' уже существует";

    public String NOT_ENOUGH_PERMISSIONS = "Недостаточно прав для удаления алиаса";
  }

  @Create
  public STATS STATS;

  public static class STATS {
    public String USAGE = "Использование:{NL} > `<ip[:порт]|алиас> [дата в формате dd.MM.yyyy+HH]`";
    public String NOT_ENOUGH_ENTRIES = "Недостаточно записей об онлайне сервера";

    @Placeholders("server")
    public String SERVER_NOT_ADDED = "Сервер '{SERVER}' не находится в списке отслеживаемых серверов";

    @Comment({
        "Плейсхолдеры:",
        " - {SERVER}: Айпи сервера",
        " - {ONLINE}: Текущий онлайн сервера",
        " - {MAX_ONLINE}: Максимальный онлайн на графике",
        " - {MIN_ONLINE}: Минимальный онлайн на графике",
        " - {ONLINE_YESTERDAY}: Онлайн вчера в это же время",
        " - {PEAK_ONLINE}: Пик онлайна все время"
    })
    @Placeholders({"server", "online", "max-online", "min-online", "online-yesterday", "peak-online"})
    public String CHART_TITLE = "График онлайна сервера '{SERVER}'{NL}Онлайн: {ONLINE}{NL}Макс. онлайн на графике: {MAX_ONLINE}{NL}Мин. онлайн на графике: {MIN_ONLINE}{NL}Онлайн вчера: {ONLINE_YESTERDAY}{NL}Пик онлайна: {PEAK_ONLINE}";

    public String ONLINE = "Онлайн";
  }

  @Create
  public SERVERS SERVERS;

  public static class SERVERS {

    @Placeholders("count")
    public String SERVERS = "Количество отслеживаемых серверов: {COUNT}";
  }

  @Create
  public HELP HELP;

  public static class HELP {

    public String MESSAGE = "Доступные команды: {NL} > Добавить сервер: !add <ip[:порт]|алиас> [протокол] [без владельца (true или false)] {NL} > Добавить алиас: !alias <алиас> <ip> {NL} > Адрес алиаса: !alias <алиас> {NL} > Удалить алиас: !alias <алиас> null {NL} > Пингануть сервер: !ping <ip[:порт]|алиас> [протокол] {NL} > Удалить сервер: !remove <ip[:порт]|алиас> [оставлять записи об онлайне (true или false)] {NL} > Количество серверов: !servers {NL} > Статистика сервера: !stats <ip[:порт]|алиас> [дата в формате dd.MM.yyyy+HH]";
  }
}
