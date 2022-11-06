package com.jnngl.ping;

public class ResponseTimeoutException extends Exception {

  public ResponseTimeoutException() {
    super();
  }

  public ResponseTimeoutException(String msg) {
    super(msg);
  }

}
