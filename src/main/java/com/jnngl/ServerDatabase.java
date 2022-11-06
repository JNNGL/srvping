package com.jnngl;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ServerDatabase {

  @DatabaseTable
  public static class ServerOnlineEntry {

    @DatabaseField(id = true)
    private String ipWithTimestamp;

    @DatabaseField
    private String ip;

    @DatabaseField
    private long timestamp;

    @DatabaseField
    private int online;

    public ServerOnlineEntry(String ipWithTimestamp, String ip, long timestamp, int online) {
      this.ipWithTimestamp = ipWithTimestamp;
      this.ip = ip;
      this.timestamp = timestamp;
      this.online = online;
    }

    public ServerOnlineEntry(String ip, long timestamp, int online) {
      this(ip + "+" + timestamp, ip, timestamp, online);
    }

    public ServerOnlineEntry() {

    }

    public String getIpWithTimestamp() {
      return ipWithTimestamp;
    }

    public void setIpWithTimestamp(String ipWithTimestamp) {
      this.ipWithTimestamp = ipWithTimestamp;
    }

    public String getIp() {
      return ip;
    }

    public void setIp(String ip) {
      this.ip = ip;
    }

    public long getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
    }

    public int getOnline() {
      return online;
    }

    public void setOnline(int online) {
      this.online = online;
    }

    @Override
    public String toString() {
      return "ServerEntry{" +
          "ip='" + ip + '\'' +
          ", timestamp=" + timestamp +
          ", online=" + online +
          '}';
    }
  }

  @DatabaseTable
  public static class ServerTargetEntry {

    @DatabaseField(id = true)
    private String ip;

    @DatabaseField
    private int protocol;

    @DatabaseField
    private String owner;

    public ServerTargetEntry(String ip, int protocol, String owner) {
      this.ip = ip;
      this.protocol = protocol;
      this.owner = owner;
    }

    public ServerTargetEntry() {

    }

    public String getIp() {
      return ip;
    }

    public void setIp(String ip) {
      this.ip = ip;
    }

    public int getProtocol() {
      return protocol;
    }

    public void setProtocol(int protocol) {
      this.protocol = protocol;
    }

    public String getOwner() {
      return owner;
    }

    public void setOwner(String owner) {
      this.owner = owner;
    }

    @Override
    public String toString() {
      return "ServerTargetEntry{" +
          "ip='" + ip + '\'' +
          ", protocol=" + protocol +
          '}';
    }
  }

  @DatabaseTable
  public static class ServerAlias {

    @DatabaseField(id = true)
    private String alias;

    @DatabaseField
    private String ip;

    @DatabaseField
    private String owner;

    public ServerAlias(String alias, String ip, String owner) {
      this.alias = alias;
      this.ip = ip;
      this.owner = owner;
    }

    public ServerAlias() {

    }

    public String getAlias() {
      return alias;
    }

    public void setAlias(String alias) {
      this.alias = alias;
    }

    public String getIp() {
      return ip;
    }

    public void setIp(String ip) {
      this.ip = ip;
    }

    public String getOwner() {
      return owner;
    }

    public void setOwner(String owner) {
      this.owner = owner;
    }

    @Override
    public String toString() {
      return "ServerAlias{" +
          "alias='" + alias + '\'' +
          ", ip='" + ip + '\'' +
          ", owner='" + owner + '\'' +
          '}';
    }
  }

  private final JdbcPooledConnectionSource connectionSource;
  private final Dao<ServerOnlineEntry, String> serverOnlineDao;
  private final Dao<ServerTargetEntry, String> serverTargetDao;
  private final Dao<ServerAlias, String> serverAliasDao;

  public ServerDatabase(File databaseFile) throws SQLException {
    connectionSource = new JdbcPooledConnectionSource("jdbc:h2:" + databaseFile.getAbsoluteFile());

    TableUtils.createTableIfNotExists(connectionSource, ServerOnlineEntry.class);
    TableUtils.createTableIfNotExists(connectionSource, ServerTargetEntry.class);
    TableUtils.createTableIfNotExists(connectionSource, ServerAlias.class);

    serverOnlineDao = DaoManager.createDao(connectionSource, ServerOnlineEntry.class);
    serverTargetDao = DaoManager.createDao(connectionSource, ServerTargetEntry.class);
    serverAliasDao = DaoManager.createDao(connectionSource, ServerAlias.class);
  }

  public ServerOnlineEntry queryServerEntry(String ip, long timestamp) throws SQLException {
    return serverOnlineDao.queryForId(ip + "+" + timestamp);
  }

  public List<ServerOnlineEntry> queryServerEntries(String ip) throws SQLException {
    return serverOnlineDao.queryForEq("IP", ip);
  }

  public Map<Long, ServerOnlineEntry> queryServerAsMap(String ip) throws SQLException {
    return queryServerEntries(ip).stream()
        .collect(Collectors.toMap(ServerOnlineEntry::getTimestamp, Function.identity()));
  }

  public void addServerEntry(ServerOnlineEntry entry) throws SQLException {
    serverOnlineDao.createOrUpdate(entry);
  }

  public void removeServerEntry(ServerOnlineEntry entry) throws SQLException {
    serverOnlineDao.delete(entry);
  }

  public void removeServerEntry(String ip, long timestamp) throws SQLException {
    removeServerEntry(queryServerEntry(ip, timestamp));
  }

  public void removeServerEntries(String ip) throws SQLException {
    for (ServerOnlineEntry entry : queryServerEntries(ip)) {
      removeServerEntry(entry);
    }
  }

  public ServerTargetEntry queryServerTarget(String ip) throws SQLException {
    return serverTargetDao.queryForId(ip);
  }

  public List<ServerTargetEntry> queryServerTargets() throws SQLException {
    return serverTargetDao.queryForAll();
  }

  public boolean addServerTarget(ServerTargetEntry entry) throws SQLException {
    if (serverTargetDao.idExists(entry.getIp())) {
      return false;
    }

    serverTargetDao.create(entry);
    return true;
  }

  public void removeServerTarget(ServerTargetEntry entry) throws SQLException {
    serverTargetDao.delete(entry);
  }

  public void removeServerTarget(String ip) throws SQLException {
    removeServerTarget(queryServerTarget(ip));
  }

  public boolean addServerAlias(ServerAlias alias) throws SQLException {
    if (serverAliasDao.idExists(alias.getAlias())) {
      return false;
    }

    serverAliasDao.create(alias);
    return true;
  }

  public void removeServerAlias(ServerAlias alias) throws SQLException {
    serverAliasDao.delete(alias);
  }

  public void removeServerAlias(String alias) throws SQLException {
    removeServerAlias(queryAlias(alias));
  }

  public ServerAlias queryAlias(String alias) throws SQLException {
    return serverAliasDao.queryForId(alias);
  }

  public void close() throws Exception {
    connectionSource.close();
  }

  public Dao<ServerOnlineEntry, String> getServerOnlineDao() {
    return serverOnlineDao;
  }

  public Dao<ServerTargetEntry, String> getServerTargetDao() {
    return serverTargetDao;
  }

  public Dao<ServerAlias, String> getServerAliasDao() {
    return serverAliasDao;
  }

  public JdbcPooledConnectionSource getConnectionSource() {
    return connectionSource;
  }
}
