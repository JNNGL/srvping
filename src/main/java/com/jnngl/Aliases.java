package com.jnngl;

import java.sql.SQLException;

public class Aliases {

  private final ServerDatabase database;

  public Aliases(ServerDatabase database) {
    this.database = database;
  }

  public String getServer(String alias) throws SQLException {
    ServerDatabase.ServerAlias serverAlias = database.queryAlias(alias);
    if (serverAlias == null) {
      return alias;
    }

    return serverAlias.getIp();
  }

  public boolean set(String alias, String ip, String owner) throws SQLException {
    if (ip == null) {
      ServerDatabase.ServerAlias serverAlias = database.queryAlias(alias);
      if (serverAlias == null) {
        return true;
      }
      
      if (!serverAlias.getOwner().equals(owner)) {
        return false;
      }
      
      database.removeServerAlias(serverAlias);
      return true;
    }

    return database.addServerAlias(new ServerDatabase.ServerAlias(alias, ip, owner));
  }

  public ServerDatabase getDatabase() {
    return database;
  }
}
