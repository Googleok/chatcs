package com.cafe24.network.chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

import com.cafe24.network.chat.server.ChatServer;

public class ChatClient {
	private static final String SERVER_IP = "0.0.0.0";
	private static final int SERVER_PORT = 7000;
	
	public static void main(String[] args) {
		Scanner scanner = null;
		Socket socket = null;
		
		try {
			// 1. 키보드 연결
			scanner = new Scanner(System.in);
			// 2. socket 생성
			socket = new Socket();
			// 3. 연결
			socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
			ChatClient.log("connected");
			
			// 4. reader/writer 생성
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
			
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
			
			// 5. join 프로토콜
			System.out.print("닉네임>>");
			String nickname = scanner.nextLine();
			pw.println("join:" + nickname);
			pw.flush();
			
			//6. ChatClientReceiveThread 시작
			new ChatClientThread(br).start();
			
			// 7. 키보드 입력 처리
			while (true) {
				System.out.print(">>");
				String input = scanner.nextLine();
				
				if("quit".equals(input) == true) {
					// 8. quit 프로토콜 처리
					pw.println("quit:" + nickname);
					pw.flush();
					break;
				} else {
					// 9. 메시지 처리
					pw.println("message:"+input);
					pw.flush();
				}
			}
			
		} catch (IOException e) {
			ChatClient.log("error:" + e); 
		} finally {
			// 10. 자원정리
			try {
				if(socket == null && socket.isClosed() == false) {
					socket.close();
				}
				scanner.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		
	}

	public static void log(String log) {
		System.out.println("[server#" + Thread.currentThread().getId() + "] " + log);
	}

}
