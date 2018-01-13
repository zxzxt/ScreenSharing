package io.github.hashbox;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;


public class Server implements Runnable {
	private final int tcp_port = 9000;
	private ServerGUI servGUI;
	private ArrayList<ServerThread> list;
	private Socket clntSock;

	public ArrayList<ServerThread> getList() {
		return list;
	}

	public Server() {
		servGUI = new ServerGUI(this);
		new Thread(servGUI).start();
	}
	public void run() {
		list = new ArrayList<ServerThread>();
		try {
			ServerSocket server = new ServerSocket(tcp_port);
			System.out.println("[SYSTEM]서버가 시작되었습니다.");
			while(true) {
				clntSock = server.accept();
				ServerThread st = new ServerThread(servGUI, clntSock, this);
				addThread(st); //벡터에 스레드를 담는다.
				st.start();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void addThread(ServerThread st) {
		  list.add(st);//벡터에 쓰레드를 담는다.
	}//addThread
	
	public void removeThread(ServerThread st) {
		  list.remove(st);//벡터에서 해당 쓰레드를 없앤다.
	}//removeThread
	
	public void broadcast(String str) throws IOException {
		for (int i = 0; i < list.size(); i++) {
			ServerThread st = (ServerThread) list.get(i);
			st.send(str);//각각의 쓰레드마다 채팅 내용 전송한다.
		}
	}
	
	public boolean doShare(int i) throws IOException {
		ServerThread st = (ServerThread) this.list.get(i);
		st.isShare = true;
		
		return true;
	}
}
