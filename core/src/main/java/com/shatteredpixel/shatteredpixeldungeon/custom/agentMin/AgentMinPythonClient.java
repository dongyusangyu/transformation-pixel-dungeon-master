package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class AgentMinPythonClient {

	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;

	public synchronized boolean connected() {
		return socket != null && socket.isConnected() && !socket.isClosed();
	}

	public synchronized int requestAction(AgentMinEncodedState encoded) {
		try {
			ensureConnected();
			out.write(AgentMinJsonBridge.observationJson(encoded));
			out.write('\n');
			out.flush();
			String response = in.readLine();
			return AgentMinJsonBridge.parseActionId(response);
		} catch (IOException e) {
			close();
			AgentMinRuntimeLog.log("python bridge request failed: " + e.getMessage());
			return -1;
		}
	}

	public synchronized void close() {
		try {
			if (socket != null) socket.close();
		} catch (IOException ignored) {
		}
		socket = null;
		in = null;
		out = null;
	}

	private void ensureConnected() throws IOException {
		if (connected()) {
			return;
		}
		socket = new Socket();
		socket.connect(new InetSocketAddress(AgentMinBridgeConfig.HOST, AgentMinBridgeConfig.PORT), AgentMinBridgeConfig.TIMEOUT_MS);
		socket.setSoTimeout(AgentMinBridgeConfig.TIMEOUT_MS);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
		out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
		AgentMinRuntimeLog.log("python bridge connected: " + AgentMinBridgeConfig.HOST + ":" + AgentMinBridgeConfig.PORT);
	}
}
