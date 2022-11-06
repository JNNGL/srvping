package com.jnngl.bot;

import com.jnngl.Aliases;
import com.jnngl.Config;
import com.jnngl.ScheduledPinger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

public class DiscordBot extends ListenerAdapter {

  private final JDA jda;
  private final ThreadPoolExecutor executor;
  private final ScheduledPinger scheduledPinger;
  private final Aliases aliases;
  private final String commandPrefix;
  private final Logger logger;
  private Map<String, String> mapping = Map.of();

  public static Activity createActivity(String line) {
    if (line.isBlank()) {
      return null;
    }

    String[] split = line.split(" ", 2);
    if (split.length != 2) {
      throw new IllegalArgumentException(line);
    }

    return Activity.of(Enum.valueOf(Activity.ActivityType.class, split[0].toUpperCase()), split[1]);
  }

  public DiscordBot(String token, ThreadPoolExecutor executor, ScheduledPinger scheduledPinger,
                    Aliases aliases, String commandPrefix, Activity activity) {
    jda = JDABuilder.createDefault(token)
        .setActivity(activity)
        .setStatus(OnlineStatus.fromKey(Config.IMP.STATUS))
        .setChunkingFilter(ChunkingFilter.ALL)
        .setMemberCachePolicy(MemberCachePolicy.ALL)
        .enableIntents(GatewayIntent.MESSAGE_CONTENT)
        .enableIntents(GatewayIntent.GUILD_MEMBERS)
        .addEventListeners(this)
        .build();
    this.scheduledPinger = scheduledPinger;
    this.commandPrefix = commandPrefix;
    this.aliases = aliases;
    this.executor = executor;
    logger = LoggerFactory.getLogger("DiscordBot");
  }

  public void setMapping(Map<String, String> mapping) {
    this.mapping = mapping;
  }

  public Map<String, String> getMapping() {
    return mapping;
  }

  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent event) {
    Message message = event.getMessage();
    String content = message.getContentRaw();

    BotUtils.processMessage(DiscordBot.class, this, Message.class, message, executor, content, commandPrefix, mapping,
        exception -> message.reply(" > " + exception.getClass().getName() + ": " + exception.getMessage()).queue());
  }

  public Consumer<String> getReplyConsumer(Message message) {
    return content -> message.reply(content).queue();
  }

  public ThreadPoolExecutor getExecutor() {
    return executor;
  }

  public ScheduledPinger getScheduledPinger() {
    return scheduledPinger;
  }

  public Aliases getAliases() {
    return aliases;
  }

  public JDA getJda() {
    return jda;
  }

  public Logger getLogger() {
    return logger;
  }
}
