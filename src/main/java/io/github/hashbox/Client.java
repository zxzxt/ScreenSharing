package io.github.hashbox;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;


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
	private ClientShare clnt_share;
	private boolean select_flag = false;
	private boolean exit_flag = false;
	
	public Client() throws IOException {
		clntGUI = new ClientGUI(this);
		new Thread(clntGUI).start();
		if(getConnIP()) {
			if(connServer()) {
				clntGUI.frame.setVisible(true);
			}
			else {
				clntGUI.alerClient("입력하신 " + ip_address + "서버가 없습니다.");
			}
		}
	}
		
	public boolean getConnIP() {
		ip_address = JOptionPane.showInputDialog("접속하고자하는 서버 아이피를 입력해주세요.");
		
		if(ip_address != null) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean connServer() throws IOException {
		String msg;
		InetAddress inet = null;
		
		try {
			sock = new Socket(ip_address, tcp_port);
			str_recv = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			str_send = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		msg = str_recv.readLine();
		if(msg.equals("name")) {
			inet = InetAddress.getLocalHost();
			send(inet.getHostName());
		}
		return true;
	}

	public void run() {
		String msg;
		// TODO Auto-generated method stub
		
		rect = new Rectangle(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
		BufferedImage temp = null;
		try {
			rb = new Robot();
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
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
						System.out.println("[client.java]Thumb Successfully");
					}
					else {
						System.err.println("[client.java]Thumb Fail");
					}
				}
				else if(msg.equals("share")) {
					System.out.println("[client.java]Share Signal Recv");
					send("ack");
					System.out.println("[client.java]Share Signal ACK Send");
					msg = str_recv.readLine();
					if(msg.equals("ack")) {
						select_flag = true;
						clnt_share = new ClientShare(clntGUI, select_flag, ip_address);
						new Thread(clnt_share).start();
					}
				}
				else if(msg.equals("shareStart") && !select_flag) {
					System.out.println("[client.java]Sharestart Signal Recv");
					clnt_share = new ClientShare(clntGUI, select_flag, ip_address);
					new Thread(clnt_share).start();
				}
				else if(msg.equals("stopShare")) {
					System.out.println("[client.java]Stop Sharing Signal RECV");
					clnt_share.isSharing = false;
					select_flag = false;
				}
				else if(msg.equals("closeSocket")) {
					System.out.println("[client.java]Close Socket Signal RECV");
					if(clnt_share!=null) {
						clnt_share.isSharing = false;
					}
					select_flag = false;
					break;
				}
				System.out.println("Loop END");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			send("exit");
			msg = str_recv.readLine();
			System.out.println(msg);
			if(msg.equals("ack") || msg.equals("thumb")) {
				System.out.println("[client.java]Client Exiting Successfully");
				str_send.close();
				str_recv.close();
				sock.close();
				clntGUI.do_clntExit();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void send(String str) throws IOException {
		str += System.getProperty("line.separator");
		str_send.write(str);//출력(소켓으로)
		str_send.flush();
	}
	
	public void exitClnt() throws IOException {
		exit_flag = true;
	}
}
