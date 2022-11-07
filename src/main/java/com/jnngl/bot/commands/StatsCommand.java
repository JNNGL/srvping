package com.jnngl.bot.commands;

import com.jnngl.Aliases;
import com.jnngl.Config;
import com.jnngl.Messages;
import com.jnngl.ScheduledPinger;
import com.jnngl.ServerDatabase;
import com.jnngl.bot.DiscordBot;
import com.jnngl.bot.TelegramBot;
import com.jnngl.bot.VkBot;
import com.jnngl.bot.VkMessageAuthor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.photos.responses.GetMessagesUploadServerResponse;
import com.vk.api.sdk.objects.photos.responses.MessageUploadResponse;
import com.vk.api.sdk.objects.photos.responses.SaveMessagesPhotoResponse;
import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.graphics.DrawingContext;
import de.erichseifert.gral.graphics.Insets2D;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.areas.AreaRenderer;
import de.erichseifert.gral.plots.areas.DefaultAreaRenderer2D;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.axes.LinearRenderer2D;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.plots.points.DefaultPointRenderer2D;
import de.erichseifert.gral.plots.points.PointRenderer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.elytrium.java.commons.config.Placeholders;
import org.slf4j.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class StatsCommand implements Command {

  private static final Font CUSTOM_FONT;
  static {
    if (Config.IMP.CUSTOM_FONT.isBlank()) {
      CUSTOM_FONT = null;
    } else {
      try {
        CUSTOM_FONT = Font.createFont(Font.TRUETYPE_FONT, new File(Config.IMP.CUSTOM_FONT));
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(CUSTOM_FONT);
      } catch (FontFormatException | IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void handle(String[] args, Logger logger, Aliases aliases, ScheduledPinger scheduledPinger, Consumer<String> reply,
                      BiConsumer<String, BufferedImage> replyImage) throws SQLException, ParseException {
    if (args.length < 1) {
      reply.accept(Messages.IMP.STATS.USAGE);
      return;
    }

    args[0] = aliases.getServer(args[0]).toLowerCase();

    ServerDatabase.ServerTargetEntry target = scheduledPinger.getDatabase().queryServerTarget(args[0]);

    if (target == null) {
      reply.accept(Placeholders.replace(Messages.IMP.STATS.SERVER_NOT_ADDED, args[0]));
      return;
    }

    logger.info("Creating stats chart for {}", args[0]);

    List<ServerDatabase.ServerOnlineEntry> onlineEntries = scheduledPinger.getDatabase().queryServerEntries(args[0]);

    long time = System.currentTimeMillis();
    if (args.length >= 2) {
      time = new SimpleDateFormat("dd.MM.yyyy+HH").parse(args[1]).getTime();
    }

    @SuppressWarnings("unchecked")
    DataTable dataTable = new DataTable(Double.class, Integer.class);

    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
    Map<Double, String> ticks = new HashMap<>();

    int hours = 24; // TODO: Add argument

    int currentOnline = 0;
    int maxOnline = 0;
    int minOnline = Integer.MAX_VALUE;
    int peakOnline = 0;
    int onlineYesterday = -1;

    Date yesterday = new Date(time - 1000 * 60 * 60 * 24);
    for (ServerDatabase.ServerOnlineEntry entry : onlineEntries) {
      peakOnline = Math.max(peakOnline, entry.getOnline());

      if (onlineYesterday == -1) {
        Date date = new Date(entry.getTimestamp());
        if (dateFormat.format(yesterday).equals(dateFormat.format(date))) {
          onlineYesterday = entry.getOnline();
        }
      }
    }

    double i = 0;
    long step = Math.max(60, Config.IMP.PING_PERIOD) * 1000L;
    for (long timestamp = time - 1000 * 60 * 60 * hours; timestamp <= time; timestamp += step) {

      Date date = new Date(timestamp);
      String format = dateFormat.format(date);

      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);

      if (format.endsWith("00") && calendar.get(Calendar.HOUR_OF_DAY) % (hours / 12) == 0) {
        ticks.put(i, format);
      } else {
        ticks.put(i, "");
      }

      ServerDatabase.ServerOnlineEntry onlineEntry = null;
      for (ServerDatabase.ServerOnlineEntry entry : onlineEntries) {
        if (entry.getTimestamp() + 60000 < timestamp) {
          continue;
        }

        if (format.equals(dateFormat.format(new Date(entry.getTimestamp())))) {
          onlineEntry = entry;
          break;
        }
      }

      if (onlineEntry != null) {
        dataTable.add(i, onlineEntry.getOnline());
        currentOnline = onlineEntry.getOnline();
        maxOnline = Math.max(maxOnline, onlineEntry.getOnline());
        minOnline = Math.min(minOnline, onlineEntry.getOnline());
      }

      i++;
    }

    if (dataTable.getRowCount() < 10) {
      reply.accept(Messages.IMP.STATS.NOT_ENOUGH_ENTRIES);
      return;
    }

    if (minOnline == Integer.MAX_VALUE) {
      minOnline = 0;
    }

    DataSeries dataSeries = new DataSeries(Messages.IMP.STATS.ONLINE, dataTable, 0, 1);
    XYPlot plot = new XYPlot(dataSeries);
    plot.setBackground(Color.WHITE);
    plot.setLegendVisible(true);
    plot.setInsets(new Insets2D.Double(20, 80, 40, 20));
    plot.setBounds(new Rectangle(0, 0, Config.IMP.CHART_WIDTH, Config.IMP.CHART_HEIGHT));
    plot.getTitle().setText(
        Placeholders.replace(
            Messages.IMP.STATS.CHART_TITLE,
            args[0],
            currentOnline,
            maxOnline,
            minOnline,
            onlineYesterday,
            peakOnline
        )
    );

    PointRenderer pointRenderer = new DefaultPointRenderer2D();
    pointRenderer.setShape(null);
    pointRenderer.setColor(Color.BLACK);
    plot.setPointRenderers(dataSeries, pointRenderer);

    Color lineColor = new Color(26, 136, 255);

    LineRenderer lineRenderer = new DefaultLineRenderer2D();
    lineRenderer.setColor(lineColor);
    lineRenderer.setGap(3.0);
    lineRenderer.setGapRounded(true);
    plot.setLineRenderers(dataSeries, lineRenderer);

    AreaRenderer areaRenderer = new DefaultAreaRenderer2D();
    areaRenderer.setColor(new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 64));
    plot.setAreaRenderers(dataSeries, areaRenderer);

    AxisRenderer axisRenderer = new LinearRenderer2D();
    axisRenderer.setCustomTicks(ticks);
    axisRenderer.setTickStroke(new BasicStroke(0));
    plot.setAxisRenderer(XYPlot.AXIS_X, axisRenderer);

    if (CUSTOM_FONT != null) {
      AxisRenderer axisRendererY = plot.getAxisRenderer(XYPlot.AXIS_Y);

      plot.setFont(CUSTOM_FONT.deriveFont(plot.getFont().getSize2D()));
      plot.getTitle().setFont(CUSTOM_FONT.deriveFont(plot.getTitle().getFont().getSize2D()));
      plot.getLegend().setFont(CUSTOM_FONT.deriveFont(plot.getLegend().getFont().getSize2D()));
      axisRendererY.setTickFont(CUSTOM_FONT.deriveFont(axisRendererY.getTickFont().getSize2D()));
      axisRenderer.setTickFont(CUSTOM_FONT.deriveFont(axisRenderer.getTickFont().getSize2D()));
    }

    BufferedImage bufferedImage =
        new BufferedImage(Config.IMP.CHART_WIDTH, Config.IMP.CHART_HEIGHT, BufferedImage.TYPE_INT_RGB);

    Graphics2D graphics = bufferedImage.createGraphics();
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

    DrawingContext drawingContext = new DrawingContext(graphics);

    plot.draw(drawingContext);

    graphics.dispose();

    replyImage.accept("", bufferedImage);
  }

  @Override
  public void handle(DiscordBot discordBot, String[] args, Message message) throws Exception {
    handle(
        args,
        discordBot.getLogger(),
        discordBot.getAliases(),
        discordBot.getScheduledPinger(),
        discordBot.getReplyConsumer(message),
        (content, image) -> {
          try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            MessageCreateBuilder builder = new MessageCreateBuilder();
            builder.setContent(content);

            ImageIO.write(image, "PNG", baos);

            builder.setFiles(FileUpload.fromData(baos.toByteArray(), "image.png"));
            message.reply(builder.build()).queue();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
    );
  }

  @Override
  public void handle(VkBot vkBot, String[] args, VkMessageAuthor author) throws Exception {
    handle(
        args,
        vkBot.getLogger(),
        vkBot.getAliases(),
        vkBot.getScheduledPinger(),
        vkBot.getReplyConsumer(author.getChatId()),
        (content, image) -> {
          try {
            File tempFile = File.createTempFile("vk_photo", ".png");
            ImageIO.write(image, "PNG", tempFile);

            GetMessagesUploadServerResponse uploadServerResponse =
                vkBot.getVk().photos()
                    .getMessagesUploadServer(vkBot.getActor())
                    .execute();

            MessageUploadResponse uploadResponse =
                vkBot.getVk().upload()
                    .photoMessage(uploadServerResponse.getUploadUrl().toString(), tempFile)
                    .execute();

            SaveMessagesPhotoResponse photo =
                vkBot.getVk().photos()
                    .saveMessagesPhoto(vkBot.getActor(), uploadResponse.getPhoto())
                    .hash(uploadResponse.getHash())
                    .server(uploadResponse.getServer())
                    .execute().get(0);

            vkBot.getVk().messages()
                .send(vkBot.getActor())
                .peerId((int) author.getChatId())
                .message(content)
                .attachment("photo" + photo.getOwnerId() + "_" + photo.getId())
                .randomId(ThreadLocalRandom.current().nextInt())
                .execute();

            if (!tempFile.delete()) {
              tempFile.deleteOnExit();
            }
          } catch (ApiException | ClientException | IOException e) {
            throw new RuntimeException(e);
          }
        }
    );
  }

  @Override
  public void handle(TelegramBot telegramBot, String[] args, long id) throws Exception {
    handle(
        args,
        telegramBot.getLogger(),
        telegramBot.getAliases(),
        telegramBot.getScheduledPinger(),
        telegramBot.getReplyConsumer(id),
        (content, image) -> {
          try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            byte[] bytes = baos.toByteArray();

            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setPhoto(new InputFile(new ByteArrayInputStream(bytes), "favicon.png"));
            sendPhoto.setCaption(content);
            sendPhoto.setChatId(id);
            telegramBot.sendPhoto(sendPhoto);
          } catch (IOException | TelegramApiException e) {
            throw new RuntimeException(e);
          }
        }
    );
  }
}
