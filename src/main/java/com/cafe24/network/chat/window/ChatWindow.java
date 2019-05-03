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
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import javax.swing.JTextPane;

import com.cafe24.network.chat.client.ChatClient;

public class ChatWindow{

	private Frame frame;
	private Panel pannel;
	private Button buttonSend;
	private Button buttonwhisper;
	private TextField textField;
	private TextArea textArea;
	private TextArea textArea_user;
	private Socket socket = null;
	private BufferedReader br = null;
	private String name = null;
	private JTextPane textPane = null;
	private List<String> userList = null;
	
	public ChatWindow(String name, Socket socket, List<String> userList) {
		frame = new Frame(name);
		pannel = new Panel();
		textPane = new JTextPane();
		buttonSend = new Button("Send");
		buttonwhisper = new Button("Whisper");
		textField = new TextField();
		textArea = new TextArea(30, 80);
		textArea_user = new TextArea(30,20);
		
		this.socket = socket;
		this.name = name;
		this.userList = userList;
		
	}

	private void finish() {
		// socket 정리
		PrintWriter pw = null;
		userList.remove(name);
		try {
//			pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
//			pw.println("quit:");
//			pw.flush();

			if(socket == null && socket.isClosed() == false) {
				socket.close();
			}
			
		}catch (SocketException e) {
			e.printStackTrace();
		}catch (IOException e) {
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
		
		//Button whisper
		buttonwhisper.setBackground(Color.CYAN);
		buttonwhisper.setForeground(Color.WHITE);
		buttonwhisper.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent actionEvent ) {
				sendWhisper();
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
		pannel.add(buttonwhisper);
		frame.add(BorderLayout.SOUTH, pannel);

		
		// TextArea
		textArea.setEditable(false);
		frame.add(BorderLayout.CENTER, textArea);

		// TextArea_ user
		textArea_user.setEditable(false);
		textArea_user.setBackground(Color.LIGHT_GRAY);
		updateUsers();
		frame.add(BorderLayout.EAST, textArea_user);
		
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
		String[] tokens = message.split("=");
		if(tokens[0].contentEquals("귓말")) {
			textArea.setBackground(Color.RED);
			textArea.setForeground(Color.WHITE);
		}
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
	
	private void sendWhisper() {
		System.out.println("whisper");
		String message = textField.getText();
		// 메시지 프로토콜 써서 보내기 pr.println("msg" + message);
		try {
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
			
			pw.println("whisper:"+message);
			pw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		textField.setText("");
		textField.requestFocus();
	}
	
	public void updateUsers() {
		textArea_user.setText("");
		for (int i = 0; i < userList.size(); i++) {
			textArea_user.append(userList.get(i));
			textArea_user.append("\n");
		}
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
					
					String[] tokens = data.split("_");
					if(tokens.length > 1) {
						String users = tokens[1];
						String[] usersArray = users.split(",");
						
						textArea_user.setText("");
						for (int i = 0; i < usersArray.length; i++) {
							textArea_user.append(usersArray[i]);
							textArea_user.append("\n");
						}
						textArea_user.setBackground(Color.ORANGE);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						textArea_user.setBackground(Color.LIGHT_GRAY);
						continue;
					}
					
					
					
					updateTextArea(data);
					System.out.println(data);
					
				}catch (SocketException e) {
					finish();
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
}
