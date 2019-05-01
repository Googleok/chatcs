package com.cafe24.network.chat.window;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

import com.cafe24.network.chat.client.ChatClient;

public class ChatClientApp {
	private static final String SERVER_IP = "0.0.0.0";
	private static final int SERVER_PORT = 7000;
	
	public static void main(String[] args) {
		String name = null;
		Scanner scanner = new Scanner(System.in);
		Socket socket = null;
		while( true ) {
			System.out.println("대화명을 입력하세요.");
			System.out.print(">>> ");
			name = scanner.nextLine();
			
			if (name.isEmpty() == false ) {
				break;
			}
			
			System.out.println("대화명은 한글자 이상 입력해야 합니다.\n");
		}
		
		try {
			// 1. 소켓 만들고
			socket = new Socket();
			socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
			ChatClient.log("connected");
						
			// 2. iostream
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);

			// 3. join 프로토콜 성공하면
			pw.println("join:" + name);
			pw.flush();
			
			String successJoin = br.readLine();
			if(successJoin.equals("success")) {
				//6. ChatClientReceiveThread 시작
				ChatWindow window = new ChatWindow(name, socket);
				window.show();
			}else {
				System.out.println("가입이 승인되지 않았습니다.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		scanner.close();
	}
}
