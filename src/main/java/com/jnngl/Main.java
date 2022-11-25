package com.jnngl;

import com.jnngl.bot.DiscordBot;
import com.jnngl.bot.TelegramBot;
import com.jnngl.bot.VkBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {

  public static void main(String[] args) throws Exception {
    Config.IMP.reload(Config.CONFIG_FILE, null);
    Messages.IMP.reload(Messages.CONFIG_FILE, null);

    ServerDatabase database = new ServerDatabase(new File(Config.IMP.DB_FILE));

    ThreadPoolExecutor pingExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(
        Runtime.getRuntime().availableProcessors());

    ScheduledPinger scheduledPinger = new ScheduledPinger(database, executor, pingExecutor);
    scheduledPinger.start(Config.IMP.PING_PERIOD, TimeUnit.SECONDS);

    Aliases aliases = new Aliases(database);

    Logger logger = LoggerFactory.getLogger("SrvPing");

    if (Config.IMP.TOKEN != null && !Config.IMP.TOKEN.isBlank()) {
      logger.info("Running Discord bot...");
      DiscordBot discordBot =
          new DiscordBot(Config.IMP.TOKEN, executor, scheduledPinger, aliases,
              Config.IMP.COMMAND_PREFIX, DiscordBot.createActivity(Config.IMP.ACTIVITY));
      discordBot.setMapping(Config.IMP.COMMAND_MAPPING);
    }

    if (Config.IMP.VK_TOKEN != null && !Config.IMP.VK_TOKEN.isBlank()) {
      logger.info("Running VK bot...");
      VkBot vkBot = new VkBot(Config.IMP.VK_TOKEN, scheduledPinger, aliases, Config.IMP.COMMAND_PREFIX, executor);
      vkBot.setMapping(Config.IMP.COMMAND_MAPPING);
    }

    if (Config.IMP.TELEGRAM_TOKEN != null && !Config.IMP.TELEGRAM_TOKEN.isBlank()) {
      logger.info("Running Telegram bot...");
      TelegramBot telegramBot =
          new TelegramBot(Config.IMP.TELEGRAM_USERNAME, Config.IMP.TELEGRAM_TOKEN,
              scheduledPinger, aliases, Config.IMP.COMMAND_PREFIX, executor);
      telegramBot.setMapping(Config.IMP.COMMAND_MAPPING);
    }
  }
}
