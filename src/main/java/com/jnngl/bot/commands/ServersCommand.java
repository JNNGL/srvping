package com.jnngl.bot.commands;

import com.jnngl.Messages;
import com.jnngl.ScheduledPinger;
import com.jnngl.bot.DiscordBot;
import com.jnngl.bot.TelegramBot;
import com.jnngl.bot.VkBot;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import net.dv8tion.jda.api.entities.Message;
import net.elytrium.java.commons.config.Placeholders;

import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class ServersCommand implements Command {

  private void handle(ScheduledPinger scheduledPinger, Consumer<String> reply) throws SQLException {
    reply.accept(Placeholders.replace(Messages.IMP.SERVERS.SERVERS,
        scheduledPinger.getDatabase().queryServerTargets().size()));
  }

  @Override
  public void handle(DiscordBot discordBot, String[] args, Message message) throws Exception {
    handle(
        discordBot.getScheduledPinger(),
        content -> message.reply(content).queue()
    );
  }

  @Override
  public void handle(VkBot vkBot, String[] args, long id) throws Exception {
    handle(
        vkBot.getScheduledPinger(),
        content -> {
          try {
            vkBot.getVk().messages()
                .send(vkBot.getActor())
                .userId((int) id)
                .message(content)
                .randomId(ThreadLocalRandom.current().nextInt())
                .execute();
          } catch (ApiException | ClientException e) {
            throw new RuntimeException(e);
          }
        }
    );
  }

  @Override
  public void handle(TelegramBot telegramBot, String[] args, long id) throws Exception {
    handle(
        telegramBot.getScheduledPinger(),
        telegramBot.getReplyConsumer(id)
    );
  }
}
