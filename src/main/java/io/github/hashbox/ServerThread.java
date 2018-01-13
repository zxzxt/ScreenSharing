package io.github.hashbox;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;


public class ServerThread extends Thread {
	String clntName;
	BufferedImage thumb;
	JButton btn_thumb;
	ImageIcon img_thumb;
	
	Socket clntSock;
	boolean exit_flag = false;
	
	BufferedReader str_recv = null;
	BufferedWriter str_send = null;
	ObjectInputStream recv = null;
	ObjectOutputStream send = null;
	ServerGUI servGUI;
	Server serv;
	ServerShare serv_share;
	Boolean isShare = false;
	Boolean isSharing = false;
	
	public ServerThread(ServerGUI servGUI, Socket clntSock, Server serv) throws IOException {
		String msg;
		
		this.clntSock = clntSock;
		this.servGUI = servGUI;
		this.serv = serv;
		System.out.println("["+clntSock.getInetAddress()+":"+clntSock.getPort()+"]클라이언트가 접속했습니다.");
		btn_thumb = new JButton();
		img_thumb = new ImageIcon();
		servGUI.p_middle.add(btn_thumb);
		servGUI.makeAction(btn_thumb);
	
		try {
			str_recv = new BufferedReader(new InputStreamReader(clntSock.getInputStream()));
			str_send = new BufferedWriter(new OutputStreamWriter(clntSock.getOutputStream()));
		} catch (Exception e) {
			System.out.println("ServerThread 생성중 예외 : " + e);
		}
		send("name");
		clntName = str_recv.readLine();
	}
	
	public void send(String str) throws IOException {
		str += System.getProperty("line.separator");
		str_send.write(str);//출력(소켓으로)
		str_send.flush();
	}
	
	public String str_recv() throws IOException {
		String msg;
		msg = str_recv.readLine();
		
		return msg;
	}
	
	public void run() {
		String msg;
		while(!exit_flag) {
			try {
				msg = str_recv.readLine();
				if(msg.equals("exit")) {
					System.out.println("[serverThread.java]Exit Signal Recv");
					send("ack");
					System.out.println("[serverThread.java]ACK Signal Send");
					serv.removeThread(this);
					str_recv.close();
					str_send.close();
					clntSock.close();
					exit_flag = true;
					break;
				}
				else if(msg.equals("noExit")){
					if(isShare && !isSharing) {
						send("share");
						System.out.println("[server.java]Share Msg Send to " + clntSock.getInetAddress());
						msg = str_recv.readLine();
						System.out.println(msg);
						if(msg.equals("ack")) {
							System.out.println("[server.java]Share ACK Signal Recv");
							send("ack");
							System.out.println("[server.java]ACK Signal Send");
							serv.broadcast("shareStart");
							System.out.println("[server.java]Broadcast to shareStart");
							str_recv.readLine(); //자기한테도 한번 오기 때문에
							serv_share = new ServerShare(serv);
							new Thread(serv_share).start();
						}
						isShare = false;
						isSharing = true;
					}
					else if(isShare && isSharing) {
						System.err.println("[server.java]This Clnt was Sharing NOW!");
						isShare = false;
					}
					else {
						send("thumb");
						thumb = ImageIO.read(clntSock.getInputStream());
						if(thumb==null) {
							continue;
						}
						System.out.println("[serverThread.java]Thumb Update Successfully");
						img_thumb.setImage(thumb);
						btn_thumb.setIcon(img_thumb);
						btn_thumb.setText(clntName);
						servGUI.p_middle.repaint();
						msg = str_recv.readLine();
						send("ack");
						sleep(2000);// 1000 msec = 1sec
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("[serverThread.java]Exited Safely");
		servGUI.p_middle.remove(btn_thumb);
		servGUI.p_middle.repaint();
	}
}
