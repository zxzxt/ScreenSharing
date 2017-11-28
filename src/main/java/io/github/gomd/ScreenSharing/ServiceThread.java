package io.github.gomd.ScreenSharing;

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

/* 
 	클라이언트에게 서비스를 제공해주는 부분.
 	화면공유를 위한 스레드 생성, ACK전송 
 	썸네일 등 클라이언트와의 통신을 담당.
 */

public class ServiceThread extends Thread {
	String clntName;
	BufferedImage thumb = null;
	JButton btn_thumb;
	ImageIcon img_thumb;
	
	Socket clntSock;
	boolean exit_flag = false;
	
	BufferedReader str_recv = null;
	BufferedWriter str_send = null;
	ObjectInputStream recv = null;
	ObjectOutputStream send = null;

	ServerGUI servGUI;
	ServerShare servShare;
	Server serv;

	boolean isShare = false;
	boolean isSharing = false;
	boolean barrier = false;
	boolean stop = false;
	boolean close = false;
	
	
	public ServiceThread(ServerGUI servGUI, Socket clntSock, Server serv) throws IOException {
		String msg;
		
		this.serv = serv;
		this.servGUI = servGUI;
		this.clntSock = clntSock;
		System.out.println("[" + clntSock.getInetAddress() +":" +clntSock.getPort() + "]에서 접속." );
		
		/* 기존 GUI에서 클라이언트가 접속시 클라이언트의 화면을
		    미리보기 하기 위한 썸네일 GUI 생성 */
		btn_thumb = new JButton();
		img_thumb = new ImageIcon();
		servGUI.p_middle.add(btn_thumb);
		servGUI.giveActionListener(btn_thumb);
		

		try {
			str_recv = new BufferedReader(new InputStreamReader(clntSock.getInputStream()));
			str_send = new BufferedWriter(new OutputStreamWriter(clntSock.getOutputStream()));
		} catch(Exception e) {
			e.printStackTrace();
		}
		/* 접속한 클라이언트의 사용자 이름을 받음 */
		send("name");
		clntName = str_recv.readLine();	
	}//Constructor
	
	public void send(String str) throws IOException {
		str += System.getProperty("line.separator");
		str_send.write(str);
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
					System.out.println("[ServiceThread]Exit Signal Received");
					send("ack");
					System.out.println("[ServiceThread]ACK Sent");
					serv.removeThread(this);
					str_recv.close();
					str_send.close();
					clntSock.close();
					exit_flag = true;
					break;
				}//exit
				else if(msg.equals("noExit")) {
					if(isShare && !isSharing) {
						send("share");
						System.out.println("[Server]share msg sent");
						msg = str_recv.readLine();
						System.out.println(msg);
						if(msg.equals("ack")) {
							System.out.println("[Server]share ACK Received");
							send("ack");
							System.out.println("[Server]ACK Sent");
							serv.setBarrier();  // 모든 ServiceThread Barrier True
							System.out.println("[Server]shareStart");
							
							/* 화면공유 스레드 */
							servShare = new ServerShare(serv);
							new Thread(servShare).start();
						}
						isShare = false;
						isSharing = true;
					}
					else if(isShare && isSharing) {
						System.err.println("[Server]This client is already SHARING");
						isShare = false;
					}
					else if(barrier) {
						if(!stop) {
							send("shareStart");
							barrier = false;
						}
						else if(stop) {
							send("stopShare");
							barrier = false;
							stop = false;
						}
					}
					else if(!barrier) {
						send("thumb");
						thumb = ImageIO.read(clntSock.getInputStream());
						if(thumb == null) {
							continue;
						}
						System.out.println("[ServiceThread]Thumb Updated");
						img_thumb.setImage(thumb);
						btn_thumb.setIcon(img_thumb);
						btn_thumb.setText(clntName);
						servGUI.p_middle.repaint();
						msg = str_recv.readLine();
						send("ack");
						sleep(2000);
					}
				}//noExit
			} catch(IOException e) {
				e.printStackTrace();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}//while
		
		System.out.println("[ServiceThread]Exited");
		servGUI.p_middle.remove(btn_thumb);
		servGUI.p_middle.repaint();
	}//run
}//class
