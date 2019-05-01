package com.cafe24.network.chat.window;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import com.cafe24.network.chat.client.ChatClient;

public class ChatWindow{

	private Frame frame;
	private Panel pannel;
	private Button buttonSend;
	private TextField textField;
	private TextArea textArea;
	private Socket socket = null;
	private BufferedReader br = null;
	
	public ChatWindow(String name, Socket socket) {
		frame = new Frame(name);
		pannel = new Panel();
		buttonSend = new Button("Send");
		textField = new TextField();
		textArea = new TextArea(30, 80);
		this.socket = socket;
	}

	private void finish() {
		// socket 정리
		PrintWriter pw = null;
		try {
//			pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
//			pw.println("quit:");
//			pw.flush();

			if(socket == null && socket.isClosed() == false) {
				socket.close();
			}
			
		}  catch (IOException e) {
			e.printStackTrace();
		}
		
		System.exit(0);
	}
	
	public void show() {
		// Button
		buttonSend.setBackground(Color.GRAY);
		buttonSend.setForeground(Color.WHITE);
		buttonSend.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent actionEvent ) {
				sendMessage();
			}
		});
		
		// Textfield
		textField.setColumns(80);
		textField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				char KeyCode = e.getKeyChar();
				if(KeyCode == KeyEvent.VK_ENTER) {
					sendMessage();
				}
			}
			
		});

		// Pannel
		pannel.setBackground(Color.LIGHT_GRAY);
		pannel.add(textField);
		pannel.add(buttonSend);
		frame.add(BorderLayout.SOUTH, pannel);

		// TextArea
		textArea.setEditable(false);
		frame.add(BorderLayout.CENTER, textArea);

		// Frame
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				finish();
			}
		});
		frame.setVisible(true);
		frame.pack();
		
		
		// thread 생성
		try {
			br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		InnerChatClientThread thread = new InnerChatClientThread(br);
		thread.start();
	}
	
	private void updateTextArea(String message) {
		textArea.append(message);
		textArea.append("\n");
	}

	private void sendMessage() {
		String message = textField.getText();
		// 메시지 프로토콜 써서 보내기 pr.println("msg" + message);
		try {
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
			
			pw.println("message:"+message);
			pw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		textField.setText("");
		textField.requestFocus();
	}
	
	// 내부클래스 updateTextArea를 접근하기 위해 내부클래스로 만듦
	private class InnerChatClientThread extends Thread{
		
		private BufferedReader br;

		public InnerChatClientThread(BufferedReader br) {
			this.br = br;
		}

		@Override
		public void run() {
			// reader를 통해 읽은 데이터 콘솔에 출력하기 (message 처리) 
			while (true) {
				try {
					String data = br.readLine();
					if(data == null) {
						ChatClient.log("closed by server");
						break;
					}
					updateTextArea(data);
					System.out.println(data);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
}
