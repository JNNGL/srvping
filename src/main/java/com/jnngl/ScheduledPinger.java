package com.jnngl;

import com.jnngl.ping.ServerData;
import com.jnngl.ping.ServerPinger;
import com.jnngl.resolver.ServerAddress;
import com.jnngl.resolver.ServerNameResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ScheduledPinger {
  private static final Map<String, InetSocketAddress> ADDRESS_CACHE = Collections.synchronizedMap(new HashMap<>());

  private final ServerDatabase database;
  private final ScheduledThreadPoolExecutor executor;
  private final Logger logger;
  private ScheduledFuture<?> scheduledFuture;

  public ScheduledPinger(ServerDatabase database, ScheduledThreadPoolExecutor executor) throws SQLException {
    this.database = database;
    this.executor = executor;
    this.logger = LoggerFactory.getLogger("ScheduledPinger");

    logger.info("{} target servers", this.database.queryServerTargets().size());
  }

  public void start(int period, TimeUnit timeUnit) throws Exception {
    if (scheduledFuture != null) {
      throw new Exception("Already running.");
    }

    scheduledFuture = executor.scheduleAtFixedRate(() -> {
      long timestamp = System.currentTimeMillis();

      try {
        List< ServerDatabase.ServerTargetEntry> targets =
            database.queryServerTargets();
        logger.info("Pinging {} servers", targets.size());
        targets.forEach(target -> executor.execute(() -> {
          InetSocketAddress address = ADDRESS_CACHE.computeIfAbsent(target.getIp(),
              ip -> ServerNameResolver.DEFAULT.resolveAddress(ServerAddress.fromString(ip)).orElseThrow());

          try {
            for (int i = 0; i < 2; i++) {
              try {
                ServerData serverData = ServerPinger.pingServer(address, target.getProtocol());
                database.addServerEntry(
                    new ServerDatabase.ServerOnlineEntry(target.getIp(), timestamp, serverData.players.online));
                break;
              } catch (Exception ignored) {
                if (i == 1) {
                  database.addServerEntry(new ServerDatabase.ServerOnlineEntry(target.getIp(), timestamp, 0));
                }
              }
            }
          } catch (SQLException ex) {
            throw new RuntimeException(ex);
          }
        }));
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, 0, period, timeUnit);
  }

  public void stop() {
    if (scheduledFuture != null) {
      scheduledFuture.cancel(false);
      scheduledFuture = null;
    }
  }

  public ServerDatabase getDatabase() {
    return database;
  }
}
