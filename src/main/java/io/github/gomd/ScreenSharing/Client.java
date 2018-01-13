package io.github.gomd.ScreenSharing;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.CyclicBarrier;

/*
 	서버에 접속을 시도 하는 부분.
 	서버와 접속이 된다면 클라이언트는 자신의
 	화면의 이미지를 주기적으로 캡쳐하여 서버에게 
 	전송하고 이는 서버의 썸네일 이미지로 활용
 */

public class Client implements Runnable {
	private final int tcp_port = 9000;
	private String ip_address;
	private Robot rb;
	private BufferedImage image;
	private Rectangle rect;
	private Socket sock;
	private BufferedReader str_recv;
	private BufferedWriter str_send;
	private ClientGUI clntGUI;
	private ClientShare clntShare;
	private boolean select_flag = false;    // 내가 선택되었는가?
	private boolean exit_flag = false;
	
	public Client() throws IOException {
		clntGUI = new ClientGUI(this);
		new Thread(clntGUI).start();
		
		if(getConnIP()) {
			if(ip_address.equals("127.0.0.1")) {    //InetAddress.getLocalHost().getHostAddress()
				clntGUI.alertClient("자기 자신은 접속 할 수 없습니다.");
			}
			else {
				if(connServer())
					clntGUI.frame.setVisible(true);
				else
					clntGUI.alertClient("입력하신 " + ip_address + "서버가 없습니다.");		
			}
		}
	}//Constructor
	
	/* 서버 IP 입력 */
	public boolean getConnIP() {
		ip_address = JOptionPane.showInputDialog("접속하는 서버 IP를 입력하세요." );
		
		if(ip_address != null) {
			return true;
		}
		else {
			return false;
		}
	}//getConnIP
	
	/* 서버와 연결 시도 */
	public boolean connServer() throws IOException {
		String msg;
		InetAddress inet = null;
		
		try {
			sock = new Socket(ip_address, tcp_port);
			str_recv = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			str_send = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		/* 서버에게 사용자의 이름을 전송 */
		msg = str_recv.readLine();
		if(msg.equals("name")) {
			inet = InetAddress.getLocalHost();
			send(inet.getHostName());
		}
		return true;
	}//connServer
	
	public void run() {	
		String msg;
		
		rect = new Rectangle(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
		BufferedImage temp = null;
		
		try {
			rb = new Robot();
		} catch (AWTException e1) {
			e1.printStackTrace();
		}
		
		while(!exit_flag) {
			try {
				send("noExit");
				msg = str_recv.readLine();
				if(msg.equals("thumb")) {	
					temp = rb.createScreenCapture(rect);	
					image = MyUtils.scaleImage(temp, 160, 120);
					ImageIO.write(image, "png", sock.getOutputStream());
					send("ack");
					msg = str_recv.readLine();
					
					if(msg.equals("ack")) {
						//System.out.println("[Client]Thumb Successfully");
					}
					else {
						System.err.println("[Client]Thumb Fail");
					}		
				}//if thumb
				else if(msg.equals("share")) {  //내 화면을 공유하겠다
					System.out.println("[Client]Received Share Signal");
					send("ack");
					System.out.println("[Client]Received ACK");
					msg = str_recv.readLine();
					if(msg.equals("ack")) {
						select_flag = true;
						clntShare = new ClientShare(clntGUI, select_flag, ip_address);
						new Thread(clntShare).start();
					}
				}//if share
				else if(msg.equals("shareStart") && !select_flag) { // 다른 화면을 공유받겠다
					System.out.println("[Client]Received shareStart Signal");
					clntShare = new ClientShare(clntGUI, select_flag, ip_address);
					new Thread(clntShare).start();
				}//if shareStart
				else if(msg.equals("stopShare")) {
					System.out.println("[Client]Received stopShare Signal");
					clntShare.isSharing = false;
					clntShare.select_flag = false;
					select_flag = false;
					clntShare = null;
				}//if stopShare
				else if(msg.equals("closeSocket")) {
					System.out.println("[Client]Received Close Socket Signal");
					if(clntShare != null) {
						clntShare.isSharing = false;
					}
					select_flag = false;
					break;
				}//if closeSocket
				System.out.println("Loop END");
			} catch(IOException e) {
				e.printStackTrace();
			}
		}//while
		/* Client가 종료버튼을 눌렀을 시 while문을 빠져나옴 */
		try {
			send("exit");
			msg = str_recv.readLine();
			System.out.println(msg);
			if(msg.equals("ack") || msg.equals("thumb")) {
				System.out.println("[Client]Client Exit: Success");
				str_send.close();
				str_recv.close();
				sock.close();
				clntGUI.do_clntExit();   //종료
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}//run

	public void send(String str) throws IOException {
		str += System.getProperty("line.separator");
		str_send.write(str); 
		str_send.flush();
	}
	
	public void exitClnt() throws IOException {
		exit_flag = true;
	}
}
