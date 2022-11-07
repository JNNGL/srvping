package com.jnngl.bot.commands;

import com.google.gson.JsonElement;
import com.jnngl.Aliases;
import com.jnngl.Messages;
import com.jnngl.bot.DiscordBot;
import com.jnngl.bot.TelegramBot;
import com.jnngl.bot.VkBot;
import com.jnngl.bot.VkMessageAuthor;
import com.jnngl.ping.ServerData;
import com.jnngl.ping.ServerPinger;
import com.jnngl.resolver.ServerAddress;
import com.jnngl.resolver.ServerNameResolver;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.elytrium.java.commons.config.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.slf4j.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class PingCommand implements Command {

  private static String getChatPlainText(JsonElement description) {
    Component chatComponent = GsonComponentSerializer.gson().deserializeFromTree(description);
    return PlainTextComponentSerializer.plainText().serialize(chatComponent).replaceAll("§[a-z0-9]", "");
  }

  private static String toMessageContent(ServerData serverData) {
    StringBuilder content = new StringBuilder();

    content.append(Messages.IMP.PING.SERVER_DATA)
        .append(": \n");

    content.append(" - ")
        .append(Messages.IMP.PING.IP)
        .append(": ")
        .append(serverData.address)
        .append('\n');

    content.append(" - ")
        .append(Messages.IMP.PING.PING)
        .append(": ")
        .append(serverData.responseTime)
        .append("ms\n");

    if (serverData.version != null) {
      content.append(" - ")
          .append(Messages.IMP.PING.VERSION_NAME)
          .append(": ")
          .append(serverData.version.name.replaceAll("§[a-z0-9]", ""))
          .append('\n');
    }
    if (serverData.players != null) {
      content.append(" - ")
          .append(Messages.IMP.PING.ONLINE)
          .append(": ")
          .append(serverData.players.online)
          .append('/')
          .append(serverData.players.max)
          .append('\n');
    }
    if (serverData.description != null) {
      content.append(" - ")
          .append(Messages.IMP.PING.DESCRIPTION)
          .append(": \n")
          .append(getChatPlainText(serverData.description))
          .append('\n');
    }
    return content.toString();
  }

  private void handle(String[] args, Aliases aliases, Logger logger, Consumer<String> reply,
                      Consumer<ServerData> dataReply) throws Exception {
    if (args.length < 1) {
      reply.accept(Messages.IMP.PING.USAGE);
      return;
    }

    args[0] = aliases.getServer(args[0]);

    logger.info("Pinging {}", args[0]);

    Optional<InetSocketAddress> remote =
        ServerNameResolver.DEFAULT.resolveAddress(ServerAddress.fromString(args[0]));
    if (remote.isEmpty()) {
      reply.accept(Placeholders.replace(Messages.IMP.COULDNT_RESOLVE, args[0]));
      return;
    }

    int protocol = 760;

    if (args.length > 1) {
      protocol = Integer.parseInt(args[1]);
    }

    reply.accept(Placeholders.replace(Messages.IMP.PING.PINGING_SERVER, args[0]));

    ServerData serverData = ServerPinger.pingServer(remote.get(), protocol);
    dataReply.accept(serverData);
  }

  @Override
  public void handle(DiscordBot discordBot, String[] args, Message message) throws Exception {
    handle(
        args,
        discordBot.getAliases(),
        discordBot.getLogger(),
        discordBot.getReplyConsumer(message),
        serverData -> {
          EmbedBuilder eb = new EmbedBuilder();

          eb.setTitle(Messages.IMP.PING.SERVER_DATA + ":", null);
          eb.setColor(Color.WHITE);

          eb.addField(
              Messages.IMP.PING.IP + ":",
              serverData.address.toString(),
              true
          );

          eb.addField(
              Messages.IMP.PING.PING + ":",
              serverData.responseTime + "ms",
              true
          );

          if (serverData.version != null) {
            eb.addField(
                Messages.IMP.PING.VERSION_NAME + ":",
                serverData.version.name.replaceAll("§[a-z0-9]", ""),
                true
            );
          }

          if (serverData.description != null) {
            eb.addField(
                Messages.IMP.PING.DESCRIPTION + ":",
                getChatPlainText(serverData.description),
                true
            );
          }

          if (serverData.players != null) {
            eb.addField(
                Messages.IMP.PING.ONLINE + ":",
                serverData.players.online + "/" + serverData.players.max,
                true
            );
          }

          if (serverData.favicon != null) {
            eb.setThumbnail("attachment://favicon.png");
          }

          MessageCreateAction reply = message.replyEmbeds(eb.build());
          if (serverData.favicon != null) {
            String base64icon = serverData.favicon.substring("data:image/png;base64,".length());
            byte[] image = Base64.getDecoder().decode(base64icon);
            reply = reply.addFiles(FileUpload.fromData(image, "favicon.png"));
          }
          reply.queue();
        }
    );
  }

  @Override
  public void handle(VkBot vkBot, String[] args, VkMessageAuthor author) throws Exception {
    handle(
        args,
        vkBot.getAliases(),
        vkBot.getLogger(),
        vkBot.getReplyConsumer(author.getChatId()),
        serverData -> {
          try {
            vkBot.getVk().messages()
                .send(vkBot.getActor())
                .peerId((int) author.getChatId())
                .message(toMessageContent(serverData))
                .dontParseLinks(true)
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
        args,
        telegramBot.getAliases(),
        telegramBot.getLogger(),
        telegramBot.getReplyConsumer(id),
        serverData -> {
          try {
            if (serverData.favicon != null) {
              String base64icon = serverData.favicon.substring("data:image/png;base64,".length());
              byte[] image = Base64.getDecoder().decode(base64icon);
              try (ByteArrayInputStream bais = new ByteArrayInputStream(image)) {
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setPhoto(new InputFile(bais, "favicon.png"));
                sendPhoto.setCaption(toMessageContent(serverData));
                sendPhoto.setChatId(id);
                telegramBot.sendPhoto(sendPhoto);
              }
            } else {
              SendMessage sendMessage = new SendMessage();
              sendMessage.setText(toMessageContent(serverData));
              sendMessage.setChatId(id);
              telegramBot.sendMethod(sendMessage);
            }
          } catch (TelegramApiException | IOException e) {
            throw new RuntimeException(e);
          }
        }
    );
  }
}