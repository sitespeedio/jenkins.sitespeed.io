/**
 * The MIT License
 *
 * Copyright (c) 2013, Sitespeed.io organization, Peter Hedenskog 
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
	
	public void send(Map<String,Object> data) throws UnknownHostException, IOException {
		
		Socket socket = null;
		PrintWriter out = null;
		
		try {
            socket = new Socket(host, port);
            OutputStream s = socket.getOutputStream();
            out = new PrintWriter(s, true);
                  
            out.write(createMessage(data));
            out.flush();
		}
		finally {	         
            if (out!=null) 
            	out.close();
            if (socket!=null)
            	socket.close();
		}
	}
	 
	private String createMessage(Map<String, Object> data) {

		StringBuilder message = new StringBuilder();
		for (Map.Entry<String, Object> entry : data.entrySet()) {
			message.append(entry.getKey()).append(" ").append(entry.getValue())
					.append(" ").append(System.currentTimeMillis() / 1000)
					.append("\n");
		}
		return message.toString();
	}
	
}
