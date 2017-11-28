package io.github.gomd.ScreenSharing;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/*
 	클라이언트들의 접속을 기다리고 
 	GUI, 서비스 스레드 등을 생성하는
 	가장 뒤에서 동작하는 부분.
 */

public class Server implements Runnable {
	private final int tcp_port = 9000;
	private ServerGUI servGUI;
	private ArrayList<ServiceThread> list;
	private Socket clntSock;
	
	public ArrayList<ServiceThread> getList() {
		return list;
	}
	
	public Server() {
		servGUI = new ServerGUI(this);
		new Thread(servGUI).start();
	}//Constructor
	
	/* Thread */
	public void run() {
		list = new ArrayList<ServiceThread>();
		try {
			ServerSocket server = new ServerSocket(tcp_port);
			System.out.println("[Server]Server Started.");
			while(true) {
				clntSock = server.accept();  //Client의 연결을 기다린다.			
				/* Client에게 서비스를 제공하는 스레드 */
				ServiceThread st = new ServiceThread(servGUI,clntSock, this); 
				addThread(st);  //스레드들을 리스트에 넣어서 관리
				st.start();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}//run
	
	public void addThread(ServiceThread st) {
		list.add(st);
	}
	
	public void removeThread(ServiceThread st) {
		list.remove(st);
	}

	/* Client들에게 서버의 신호를 전송. (끊겠다, 공유하겠다 등) */
	public void signal(String str) throws IOException {
		for (int i = 0; i < list.size(); i++) {
			ServiceThread st = list.get(i);
			st.send(str);  // 각각의 스레드마다 신호 전송
		}
	}
	
	public void setBarrier() throws IOException {
		for (int i = 0; i < list.size(); i++) {
			ServiceThread st = list.get(i);
			st.barrier = true;
		}
	}
	
	public void setStop() throws IOException {
		for (int i = 0; i < list.size(); i++) {
			ServiceThread st = list.get(i);
			st.stop = true;
			st.barrier = true;
		}
	}
	
	/* 어떤 Client 화면을 클릭하였는지 flag */
	public boolean flagShare(int i) throws IOException {
		ServiceThread st = list.get(i);
		st.isShare = true;
		
		return true;
	}
}
