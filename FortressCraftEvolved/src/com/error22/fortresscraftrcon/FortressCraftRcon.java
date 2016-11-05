package com.error22.fortresscraftrcon;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class FortressCraftRcon {
	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;

	public boolean connect(String address, int port, String password) throws IOException {
		socket = new Socket(address, port);
		inputStream = socket.getInputStream();
		outputStream = socket.getOutputStream();

		byte[] packet = createPacket(1000, 3, password);
		outputStream.write(packet);
		ByteBuffer resp1 = readPacket();
		ByteBuffer resp2 = readPacket();
		System.out.println("Auth response 1: " + getContent(resp1));
		System.out.println("Auth response 2: " + getContent(resp2));
		return (resp1.getInt(8) == 2 || resp2.getInt(8) == 2) && resp1.getInt(4) == 1000 && resp2.getInt(4) == 1000;
	}

	public String sendCommand(String command) throws IOException {
		byte[] packet = createPacket(1000, 2, command);
		outputStream.write(packet);
		ByteBuffer response = readPacket();
		response.position(0);
		int length = response.getInt();
		response.getInt();
		response.getInt();
		byte[] content = new byte[length - 8];
		response.get(content);
		return new String(content);
	}

	public String getContent(ByteBuffer response) {
		response.position(0);
		int length = response.getInt();
		response.getInt();
		response.getInt();
		byte[] content = new byte[length - 8];
		response.get(content);
		return new String(content);
	}

	public boolean sendChatMessage(String message) throws IOException {
		return sendCommand("Chat " + message).contains("executed successfully");
	}

	public boolean stopServer() throws IOException {
		return sendCommand("Quit").contains("executed successfully");
	}

	private byte[] createPacket(int id, int type, String command) {
		ByteBuffer packet = ByteBuffer.allocate(command.length() + 12);
		packet.order(LITTLE_ENDIAN);
		packet.putInt(command.length() + 9).putInt(id).putInt(type).put(command.getBytes());
		return packet.array();
	}

	private ByteBuffer readPacket() throws IOException {
		byte[] length = new byte[4];
		inputStream.read(length);
		ByteBuffer packet = ByteBuffer.allocate(4120);
		packet.order(LITTLE_ENDIAN);
		packet.put(length);
		for (int i = 0; i < packet.getInt(0); i++) {
			packet.put((byte) inputStream.read());
		}
		return packet;
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 4) {
			System.out.println("Invalid args! FortressCraftRcon [ip] [port] [password] [command]");
			return;
		}
		System.out.println("Connecting to "+args[0]+":"+args[1]);
		FortressCraftRcon rcon = new FortressCraftRcon();
		boolean authed = rcon.connect(args[0], Integer.parseInt(args[1]), args[2]);
		if (authed) {
			System.out.println("Authed, sending command");
			System.out.println("Got: " + rcon.sendCommand(args[3]));
		} else {
			System.out.println("Failed to auth!");
		}
	}

}
