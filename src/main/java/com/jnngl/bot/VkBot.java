package com.jnngl.bot;

import com.google.gson.JsonObject;
import com.jnngl.Aliases;
import com.jnngl.ScheduledPinger;
import com.vk.api.sdk.actions.LongPoll;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.exceptions.LongPollServerKeyExpiredException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.callback.longpoll.responses.GetLongPollEventsResponse;
import com.vk.api.sdk.objects.groups.responses.GetLongPollServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

public class VkBot {

  private final ScheduledPinger scheduledPinger;
  private final Aliases aliases;
  private final ThreadPoolExecutor executor;
  private final Logger logger;
  private final String commandPrefix;
  private final VkApiClient vk;
  private final GroupActor actor;
  private Map<String, String> mapping = Map.of();
  private boolean polling;

  public VkBot(String token, ScheduledPinger scheduledPinger, Aliases aliases, String commandPrefix,
               ThreadPoolExecutor executor) throws ClientException, ApiException {
    this.scheduledPinger = scheduledPinger;
    this.aliases = aliases;
    this.commandPrefix = commandPrefix;
    this.executor = executor;
    this.logger = LoggerFactory.getLogger("VkBot");

    TransportClient transportClient = new HttpTransportClient();
    GroupActor tempActor = new GroupActor(0, token);

    vk = new VkApiClient(transportClient);
    int groupId = vk.groups().getByIdObjectLegacy(tempActor).groupIds(Collections.emptyList()).execute().get(0).getId();

    actor = new GroupActor(groupId, token);
    vk.groups().setLongPollSettings(actor, groupId).enabled(true)
        .messageNew(true)
        .execute();

    polling = true;
    executor.execute(() -> {
      while (polling) {
        try {
          GetLongPollServerResponse serverInfo = vk.groups().getLongPollServer(actor, actor.getGroupId()).execute();
          LongPoll longPoll = new LongPoll(vk);

          String server = serverInfo.getServer();
          String key = serverInfo.getKey();
          String ts = serverInfo.getTs();

          while (polling) {
            GetLongPollEventsResponse longPollResponse = longPoll.getEvents(server, key, ts).waitTime(25).execute();
            ts = longPollResponse.getTs();

            longPollResponse.getUpdates().forEach(e -> {
              if (e.has("type") && e.has("object")) {
                String type = e.get("type").getAsString();
                JsonObject object = e.get("object").getAsJsonObject();

                if (object != null) {
                  if ("message_new".equals(type)) {
                    if (object.has("message")) {
                      JsonObject message = object.get("message").getAsJsonObject();

                      if (message.has("text")) {
                        String text = message.get("text").getAsString();
                        long id = message.get("from_id").getAsLong();
                        long peer = id;
                        if (message.has("peer_id")) {
                          peer = message.get("peer_id").getAsLong();
                        }

                        processMessage(text, new VkMessageAuthor(id, peer));
                      }
                    }
                  }
                }
              }
            });
          }
        } catch (LongPollServerKeyExpiredException ignored) {

        } catch (ApiException | ClientException e) {
          e.printStackTrace();
        }
      }
    });
  }

  public void setMapping(Map<String, String> mapping) {
    this.mapping = mapping;
  }

  public Map<String, String> getMapping() {
    return mapping;
  }

  private void processMessage(String content, VkMessageAuthor author) {
    BotUtils.processMessage(VkBot.class, this, VkMessageAuthor.class, author, executor, content, commandPrefix, mapping,
        exception -> {
          try {
            vk.messages()
                .send(actor)
                .peerId((int) author.getChatId())
                .message(exception.getClass().getName() + ": " + exception.getMessage())
                .randomId(ThreadLocalRandom.current().nextInt())
                .execute();
          } catch (ApiException | ClientException e) {
            throw new RuntimeException(e);
          }
        });
  }

  public Consumer<String> getReplyConsumer(long id) {
    return content -> {
      try {
        vk.messages()
            .send(actor)
            .peerId((int) id)
            .message(content)
            .randomId(ThreadLocalRandom.current().nextInt())
            .execute();
      } catch (ApiException | ClientException e) {
        throw new RuntimeException(e);
      }
    };
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

  public VkApiClient getVk() {
    return vk;
  }

  public GroupActor getActor() {
    return actor;
  }

  public void setPolling(boolean polling) {
    this.polling = polling;
  }

  public boolean isPolling() {
    return polling;
  }
}
