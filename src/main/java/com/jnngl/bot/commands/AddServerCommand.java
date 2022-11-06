package com.jnngl.bot.commands;

import com.jnngl.Aliases;
import com.jnngl.Messages;
import com.jnngl.ScheduledPinger;
import com.jnngl.ServerDatabase;
import com.jnngl.bot.DiscordBot;
import com.jnngl.bot.TelegramBot;
import com.jnngl.bot.VkBot;
import com.jnngl.ping.ServerPinger;
import com.jnngl.resolver.ServerAddress;
import com.jnngl.resolver.ServerNameResolver;
import net.dv8tion.jda.api.entities.Message;
import net.elytrium.java.commons.config.Placeholders;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Consumer;

public class AddServerCommand implements Command {

  private void handle(String[] args, String owner, Logger logger, ScheduledPinger scheduledPinger, Aliases aliases,
                      Consumer<String> reply) throws SQLException {
    if (args.length < 1) {
      reply.accept(Messages.IMP.ADD_SERVER.USAGE);
      return;
    }

    int protocol = 760;

    if (args.length >= 2) {
      protocol = Integer.parseInt(args[1]);
    }

    if(args.length >= 3) {
      if (Boolean.parseBoolean(args[2])) {
        owner = null;
      }
    }

    args[0] = aliases.getServer(args[0]).toLowerCase();

    try {
      ServerAddress serverAddress = ServerAddress.fromString(args[0]);
      Optional<InetSocketAddress> inetSocketAddress =
          ServerNameResolver.DEFAULT.resolveAddress(serverAddress);
      ServerPinger.pingServer(inetSocketAddress.orElseThrow(), protocol);
    } catch (Throwable e) {
      reply.accept(Placeholders.replace(Messages.IMP.ADD_SERVER.COULDNT_PING, args[0]));
      return;
    }

    if (scheduledPinger.getDatabase().addServerTarget(new ServerDatabase.ServerTargetEntry(args[0], protocol, owner))) {
      logger.info("Added server: ip=" + args[0] + ", protocol=" + protocol + ", owner=" + owner);
      reply.accept(Placeholders.replace(Messages.IMP.ADD_SERVER.SERVER_ADDED, args[0]));
    } else {
      reply.accept(Placeholders.replace(Messages.IMP.ADD_SERVER.ALREADY_EXISTS, args[0]));
    }
  }

  @Override
  public void handle(DiscordBot discordBot, String[] args, Message message) throws SQLException {
    handle(
        args,
        message.getAuthor().getId(),
        discordBot.getLogger(),
        discordBot.getScheduledPinger(),
        discordBot.getAliases(),
        discordBot.getReplyConsumer(message)
    );
  }

  @Override
  public void handle(VkBot vkBot, String[] args, long id) throws Exception {
    handle(
        args,
        String.valueOf(id),
        vkBot.getLogger(),
        vkBot.getScheduledPinger(),
        vkBot.getAliases(),
        vkBot.getReplyConsumer(id)
    );
  }

  @Override
  public void handle(TelegramBot telegramBot, String[] args, long id) throws Exception {
    handle(
        args,
        String.valueOf(id),
        telegramBot.getLogger(),
        telegramBot.getScheduledPinger(),
        telegramBot.getAliases(),
        telegramBot.getReplyConsumer(id)
    );
  }
}
