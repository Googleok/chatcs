package com.cafe24.network.chat.client;

import java.io.BufferedReader;
import java.io.IOException;

public class ChatClientThread extends Thread{

	private BufferedReader br = null;
	
	public ChatClientThread(BufferedReader br) {
		this.br = br;
	}

	@Override
	public void run() {
		// readere를 통해 읽은 데이터 콘솔에 출력하기 (message 처리) 
		while (true) {
			try {
				String data = br.readLine();
				if(data == null) {
					ChatClient.log("closed by server");
					break;
				}
				
				System.out.println(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
