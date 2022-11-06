package com.jnngl.bot.commands;

import com.jnngl.bot.DiscordBot;
import com.jnngl.bot.TelegramBot;
import com.jnngl.bot.VkBot;
import net.dv8tion.jda.api.entities.Message;

public interface Command {

  void handle(DiscordBot discordBot, String[] args, Message message) throws Exception;
  void handle(VkBot vkBot, String[] args, long id) throws Exception;
  void handle(TelegramBot telegramBot, String[] args, long id) throws Exception;

}
