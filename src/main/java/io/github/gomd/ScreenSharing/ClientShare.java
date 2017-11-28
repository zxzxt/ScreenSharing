package io.github.gomd.ScreenSharing;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.zip.DataFormatException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ClientShare implements Runnable {
	ClientGUI clntGUI;
	boolean select_flag;
	boolean isSharing = true;
	String ip_address;
	final int recv_port = 9001;
	final int send_port = 9002;
	final double DATAGRAM_MAX_SIZE = 65527;
	
	public ClientShare(ClientGUI clntGUI, boolean select_flag, String ip_address) {
		this.clntGUI = clntGUI;
		this.select_flag = select_flag;
		this.ip_address = ip_address;
	}
	
	public void run() {
		System.out.println("[clientShare]clientShare START");
		
		Rectangle rect;
		Robot rb = null;
		BufferedImage temp;
		ImageIcon icon_screen = new ImageIcon();
		DatagramSocket send_ds = null;
		DatagramSocket recv_ds = null;
		DatagramPacket recv_dp = null;
		byte buf[] = new byte[(int) DATAGRAM_MAX_SIZE];
		
		rect = new Rectangle(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
		
		try {
			recv_dp = new DatagramPacket(buf, buf.length);
			recv_ds = new DatagramSocket(recv_port);
			send_ds = new DatagramSocket();
			rb = new Robot();
		} catch(SocketException e) {
			e.printStackTrace();
		} catch(AWTException e1) {
			e1.printStackTrace();
		}
		
		while(isSharing) {
			/* 내가 선택되어서 내 화면을 보낼 때 */
			if(select_flag) {
				System.out.println("[clientShare]Capturing Screen");
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				temp = rb.createScreenCapture(rect);
				temp = MyUtils.scaleImage(temp, 640, 480);
				try {
					ImageIO.write(temp, "jpg", baos);
					baos.flush();
					/* JPG to Byte */
					byte[] imageInByte = baos.toByteArray();
					buf = imageInByte;
					baos.close();
					DatagramPacket send_dp = new DatagramPacket(buf, buf.length, InetAddress.getByName(ip_address), send_port);
					send_ds.send(send_dp);
					System.out.println("[clientShare]Send Screen Sharing Data.");
				} catch(IOException e) {
					e.printStackTrace();
				}
			}//if
			/* 내가 공유 받을 때 */
			try {
				recv_ds.setSoTimeout(1000);
				recv_ds.receive(recv_dp);
				System.out.println("[ClientShare]Received shared screen");
				byte[] imageInByte = new byte[(int) DATAGRAM_MAX_SIZE];
				imageInByte = recv_dp.getData();
				
				InputStream in = new ByteArrayInputStream(imageInByte);
				BufferedImage screen = ImageIO.read(in);
				if(screen==null) {
					continue;
				}
				icon_screen.setImage(screen);
				clntGUI.lb_screen.setIcon(icon_screen);
				clntGUI.lb_screen.setText(null);
				clntGUI.lb_screen.repaint();
				System.out.println("[ClientShare]Update Screen");
			} catch (SocketTimeoutException e) {
				
			} catch(IOException e) {
				e.printStackTrace();
			} 
		}//while
		clntGUI.lb_screen.setIcon(null);
		clntGUI.lb_screen.setText("공유 없음");
		clntGUI.lb_screen.repaint();
		recv_ds.close();
		send_ds.close();
		System.out.println("[clientShare.java]Stop clientShare Thread.");
	}//run
	
	
}//class
