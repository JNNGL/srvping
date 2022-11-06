package com.jnngl.bot.commands;

import com.jnngl.Messages;
import com.jnngl.bot.DiscordBot;
import com.jnngl.bot.TelegramBot;
import com.jnngl.bot.VkBot;
import net.dv8tion.jda.api.entities.Message;

import java.util.function.Consumer;

public class HelpCommand implements Command {

  private void handle(Consumer<String> reply) {
    reply.accept(Messages.IMP.HELP.MESSAGE);
  }

  @Override
  public void handle(DiscordBot discordBot, String[] args, Message message) throws Exception {
    handle(discordBot.getReplyConsumer(message));
  }

  @Override
  public void handle(VkBot vkBot, String[] args, long id) throws Exception {
    handle(vkBot.getReplyConsumer(id));
  }

  @Override
  public void handle(TelegramBot telegramBot, String[] args, long id) throws Exception {
    handle(telegramBot.getReplyConsumer(id, false));
  }
}
