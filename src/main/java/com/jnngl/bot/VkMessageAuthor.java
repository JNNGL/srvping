package com.jnngl.bot;

public class VkMessageAuthor {

  private final long authorId;
  private final long chatId;

  public VkMessageAuthor(long authorId, long chatId) {
    this.authorId = authorId;
    this.chatId = chatId;
  }

  public long getAuthorId() {
    return authorId;
  }

  public long getChatId() {
    return chatId;
  }
}
