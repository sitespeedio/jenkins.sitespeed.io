/**
 * Sitespeed.io - How speedy is your site? (http://www.sitespeed.io)
 * Copyright (c) 2014, Peter Hedenskog, Tobias Lidskog
 * and other contributors
 * Released under the Apache 2.0 License
 */
package io.sitespeed.jenkins.graphite;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

public class GraphiteSender {

  private final String host;
  private final int port;

  public GraphiteSender(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public void send(Map<String, Object> data) throws UnknownHostException, IOException {

    Socket socket = null;
    PrintWriter out = null;

    try {
      socket = new Socket(host, port);
      OutputStream s = socket.getOutputStream();
      out = new PrintWriter(s, true);

      out.write(createMessage(data));
      out.flush();
    } finally {
      if (out != null) out.close();
      if (socket != null) socket.close();
    }
  }

  private String createMessage(Map<String, Object> data) {

    StringBuilder message = new StringBuilder();
    for (Map.Entry<String, Object> entry : data.entrySet()) {
      message.append(entry.getKey()).append(" ").append(entry.getValue()).append(" ")
          .append(System.currentTimeMillis() / 1000).append("\n");
    }
    return message.toString();
  }

}
