package com.cafe24.network.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ChatServerThread extends Thread{
	private String nickname = null;
	private Socket socket = null;
	BufferedReader br = null;
	PrintWriter pw = null;
	private List<Writer> listWriters = null;
	Writer writer = null;
	
	public ChatServerThread(Socket socket, List<Writer> listWriters) {
		this.socket = socket;
		this.listWriters = listWriters;
	}
	
	
	@Override
	public void run() {
		// 1. Remote Host Information
		InetSocketAddress inetRemoteSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
		String remoteHostAddress = inetRemoteSocketAddress.getAddress().getHostAddress();
		int remotePort = inetRemoteSocketAddress.getPort();
		ChatServer.log("[server] connected by client[" + remoteHostAddress + ":" + remotePort + " ]");

		// 2. 스트림 얻기
		try {
			br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
	
			while (true) {
				String request = br.readLine();
				if( request == null) {
					ChatServer.log("클라이언트로 부터 연결 끊김");
					doQuit(writer);
					break;
				}
				
				// 4. 프로토콜 분석
				
				String[] tokens = request.split(":");
				if("join".equals(tokens[0])) {
					doJoin( tokens[1], pw);
				} else if("message".equals(tokens[0])) {
					doMessage(tokens[1]);
				} else if("quit".equals(tokens[0])) {
					doQuit(writer);
				} else {
					ChatServer.log("에러: 알수 없는 요청("+ tokens[0] + ")");
				}
				
			}
		
		} catch (SocketException e) {
			System.out.println("[server] sudden closed by client");
		}catch (IOException e) {
			e.printStackTrace();
		} finally {
				try {
					if(socket == null && socket.isClosed() == false) {
						socket.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	
	}
	
	// 가입 로직
	private void doJoin(String nickname, Writer writer) {
		this.nickname = nickname;
		
		String data = nickname + "님이 참여하였습니다.";
		broadcast(data);
		
		// writer pool 에 저장
		addWriter(writer);
		
		// ack
		pw.println("success");
		pw.flush();
	}
	
	private void doMessage(String data) {
		broadcast(data);
	}

	private void doQuit(Writer writer) {
		removeWriter(writer);
		
		String data = nickname + "님이 퇴장 하였습니다.";
		broadcast(data);
	}

	private void addWriter(Writer writer) {
		synchronized (listWriters) {
			listWriters.add(writer);
		}
	}
	
	private void removeWriter(Writer writer) {
		synchronized (listWriters) {
			listWriters.remove(writer);
		}
	}

	private void broadcast (String data) {
		synchronized (listWriters) {
			for(Writer writer : listWriters) {
				PrintWriter pw = (PrintWriter)writer;
				pw.println(nickname+": "+data);
				pw.flush();
			}
		}
	}
	

}
