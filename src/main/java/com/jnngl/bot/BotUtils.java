package com.jnngl.bot;

import com.jnngl.bot.commands.Command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

public class BotUtils {

  public static void processMessage(Class<?> botClass, Object bot, Class<?> idClass, Object id,
                                    ThreadPoolExecutor executor, String content, String commandPrefix,
                                    Map<String, String> mapping, Consumer<Throwable> exceptionHandler) {
    executor.execute(() -> {
      if (content.startsWith(commandPrefix)) {
        mapping.entrySet().stream()
            .filter(e ->
                content.toLowerCase().startsWith(commandPrefix + e.getKey() + " ") ||
                content.toLowerCase().equals(commandPrefix + e.getKey()))
            .forEach(e -> {
              try {
                @SuppressWarnings("unchecked")
                Class<Command> commandClass = (Class<Command>) Class.forName(e.getValue());
                Method method = commandClass.getMethod("handle", botClass, String[].class, idClass);
                String[] args = content.substring((commandPrefix + e.getKey()).length()).split(" ");
                if (args.length < 2) {
                  args = new String[0];
                } else {
                  args = Arrays.copyOfRange(args, 1, args.length);
                }
                method.invoke(commandClass.getConstructor().newInstance(), bot, args, id);
              } catch (ClassNotFoundException | NoSuchMethodException ignored) {
              } catch (InstantiationException |
                       IllegalAccessException ex) {
                throw new RuntimeException(ex);
              } catch (InvocationTargetException ex) {
                Throwable actual = ex.getCause();
                actual.printStackTrace();
                exceptionHandler.accept(actual);
              }
            });
      }
    });
  }
}
