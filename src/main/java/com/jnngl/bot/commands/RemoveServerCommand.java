package com.jnngl.bot.commands;

import com.jnngl.Aliases;
import com.jnngl.Messages;
import com.jnngl.ScheduledPinger;
import com.jnngl.ServerDatabase;
import com.jnngl.bot.DiscordBot;
import com.jnngl.bot.TelegramBot;
import com.jnngl.bot.VkBot;
import net.dv8tion.jda.api.entities.Message;
import net.elytrium.java.commons.config.Placeholders;

import java.sql.SQLException;
import java.util.function.Consumer;

public class RemoveServerCommand implements Command {

  private void handle(String[] args, ScheduledPinger scheduledPinger, Aliases aliases,
                      String authorId, Consumer<String> reply) throws SQLException {
    if (args.length < 1) {
      reply.accept(Messages.IMP.REMOVE_SERVER.USAGE);
      return;
    }

    args[0] = aliases.getServer(args[0]).toLowerCase();

    boolean keepServerEntries = false;
    if (args.length >= 2) {
      keepServerEntries = Boolean.parseBoolean(args[1]);
    }

    ServerDatabase.ServerTargetEntry targetServer = scheduledPinger.getDatabase().queryServerTarget(args[0]);

    if (targetServer.getOwner() != null && !targetServer.getOwner().equals(authorId)) {
      reply.accept(Messages.IMP.REMOVE_SERVER.NOT_ENOUGH_PERMISSIONS);
      return;
    }

    scheduledPinger.getDatabase().removeServerTarget(targetServer);

    if (!keepServerEntries) {
      scheduledPinger.getDatabase().removeServerEntries(args[0]);
      reply.accept(Placeholders.replace(Messages.IMP.REMOVE_SERVER.SERVER_AND_ENTRIES_REMOVED, args[0]));
    } else {
      reply.accept(Placeholders.replace(Messages.IMP.REMOVE_SERVER.SERVER_REMOVED, args[0]));
    }
  }

  @Override
  public void handle(DiscordBot discordBot, String[] args, Message message) throws SQLException {
    handle(
        args,
        discordBot.getScheduledPinger(),
        discordBot.getAliases(),
        message.getAuthor().getId(),
        discordBot.getReplyConsumer(message)
    );
  }

  @Override
  public void handle(VkBot vkBot, String[] args, long id) throws Exception {
    handle(
        args,
        vkBot.getScheduledPinger(),
        vkBot.getAliases(),
        String.valueOf(id),
        vkBot.getReplyConsumer(id)
    );
  }

  @Override
  public void handle(TelegramBot telegramBot, String[] args, long id) throws Exception {
    handle(
        args,
        telegramBot.getScheduledPinger(),
        telegramBot.getAliases(),
        String.valueOf(id),
        telegramBot.getReplyConsumer(id)
    );
  }
}
