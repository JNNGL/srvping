package com.jnngl.bot.commands;

import com.jnngl.Aliases;
import com.jnngl.Messages;
import com.jnngl.bot.DiscordBot;
import com.jnngl.bot.TelegramBot;
import com.jnngl.bot.VkBot;
import net.dv8tion.jda.api.entities.Message;
import net.elytrium.java.commons.config.Placeholders;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.function.Consumer;

public class AliasCommand implements Command {

  private void handle(String[] args, String owner, Aliases aliases, Logger logger,
                      Consumer<String> reply) throws SQLException {
    if (args.length < 1) {
      reply.accept(Messages.IMP.ALIAS.USAGE);
      return;
    }

    if (args.length == 1) {
      String server = aliases.getServer(args[0]);
      if (server.equals(args[0])) {
        reply.accept(Placeholders.replace(Messages.IMP.ALIAS.ALIAS_NOT_FOUND, args[0]));
        return;
      }

      reply.accept(Placeholders.replace(Messages.IMP.ALIAS.ALIAS_ADDRESS, args[0], server));
      return;
    }

    if (args[1].equalsIgnoreCase("null")) {
      if (aliases.set(args[0], null, owner)) {
        logger.info("Alias {} removed", args[0]);
        reply.accept(Placeholders.replace(Messages.IMP.ALIAS.ALIAS_REMOVED, args[0]));
      } else {
        reply.accept(Messages.IMP.ALIAS.NOT_ENOUGH_PERMISSIONS);
      }
      return;
    }

    if (aliases.set(args[0], args[1], owner)) {
      logger.info("Alias {} -> {} added", args[0], args[1]);
      reply.accept(Placeholders.replace(Messages.IMP.ALIAS.ALIAS_ADDED, args[0], args[1]));
    } else {
      reply.accept(Placeholders.replace(Messages.IMP.ALIAS.ALREADY_EXISTS, args[0]));
    }
  }

  @Override
  public void handle(DiscordBot discordBot, String[] args, Message message) throws SQLException {
    handle(
        args,
        message.getAuthor().getId(),
        discordBot.getAliases(),
        discordBot.getLogger(),
        discordBot.getReplyConsumer(message)
    );
  }

  @Override
  public void handle(VkBot vkBot, String[] args, long id) throws Exception {
    handle(
        args,
        String.valueOf(id),
        vkBot.getAliases(),
        vkBot.getLogger(),
        vkBot.getReplyConsumer(id)
    );
  }

  @Override
  public void handle(TelegramBot telegramBot, String[] args, long id) throws Exception {
    handle(
        args,
        String.valueOf(id),
        telegramBot.getAliases(),
        telegramBot.getLogger(),
        telegramBot.getReplyConsumer(id)
    );
  }
}
