package com.jnngl.bot;

import com.jnngl.Aliases;
import com.jnngl.Messages;
import com.jnngl.ScheduledPinger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

public class TelegramBot extends TelegramLongPollingBot {

  private final ScheduledPinger scheduledPinger;
  private final Aliases aliases;
  private final ThreadPoolExecutor executor;
  private final Logger logger;
  private final String commandPrefix;
  private final String username;
  private final String token;
  private Map<String, String> mapping = Map.of();

  public TelegramBot(String username, String token, ScheduledPinger scheduledPinger, Aliases aliases,
                     String commandPrefix, ThreadPoolExecutor executor) throws TelegramApiException {
    this.username = username;
    this.token = token;
    this.scheduledPinger = scheduledPinger;
    this.aliases = aliases;
    this.commandPrefix = commandPrefix;
    this.executor = executor;
    this.logger = LoggerFactory.getLogger("TelegramBot");

    TelegramBotsApi telegramBots = new TelegramBotsApi(DefaultBotSession.class);
    telegramBots.registerBot(this);
  }

  @Override
  public String getBotUsername() {
    return username;
  }

  @Override
  public String getBotToken() {
    return token;
  }

  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage()) {
      Message message = update.getMessage();
      if (message != null && message.hasText()) {
        processMessage(message.getText(), message.getChatId());
      }
    }
  }

  private void processMessage(String content, long id) {
    if (content.equals("/start")) {
      try {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(Messages.IMP.HELP.MESSAGE);
        sendMessage.setChatId(id);
        sendMethod(sendMessage);
      } catch (TelegramApiException exc) {
        throw new RuntimeException(exc);
      }

      return;
    }

    BotUtils.processMessage(TelegramBot.class, this, long.class, id, executor, content, commandPrefix, mapping,
        exception -> {
          try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setText(exception.getClass().getName() + ": " + exception.getMessage());
            sendMessage.setChatId(id);
            sendApiMethod(sendMessage);
          } catch (TelegramApiException exc) {
            throw new RuntimeException(exc);
          }
        });
  }

  public ScheduledPinger getScheduledPinger() {
    return scheduledPinger;
  }

  public Aliases getAliases() {
    return aliases;
  }

  public ThreadPoolExecutor getExecutor() {
    return executor;
  }

  public Logger getLogger() {
    return logger;
  }

  public String getCommandPrefix() {
    return commandPrefix;
  }

  public Map<String, String> getMapping() {
    return mapping;
  }

  public void setMapping(Map<String, String> mapping) {
    this.mapping = mapping;
  }

  public Consumer<String> getReplyConsumer(long id, boolean markdown) {
    return content -> {
      try {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(markdown);
        sendMessage.setText(content);
        sendMessage.setChatId(id);
        sendMethod(sendMessage);
      } catch (TelegramApiException e) {
        throw new RuntimeException(e);
      }
    };
  }

  public Consumer<String> getReplyConsumer(long id) {
    return getReplyConsumer(id, true);
  }

  public void sendMethod(BotApiMethod<? extends Serializable> method) throws TelegramApiException {
    sendApiMethod(method);
  }

  public void sendPhoto(SendPhoto sendPhoto) throws TelegramApiException {
    super.execute(sendPhoto);
  }

  public void sendMethodAsync(BotApiMethod<? extends Serializable> method) {
    sendApiMethodAsync(method);
  }

  public void sendPhotoAsync(SendPhoto sendPhoto) {
    super.executeAsync(sendPhoto);
  }
}
