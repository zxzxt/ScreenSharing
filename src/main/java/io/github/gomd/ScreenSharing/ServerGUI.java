package io.github.gomd.ScreenSharing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/*
 	GUI와 관련된 모든 이벤트를 관리.
 */

public class ServerGUI implements Runnable, ActionListener {
	JFrame frame;
	JPanel p_top;
	JPanel p_middle;
	JLabel lb_ip;
	JButton btn_off;
	JButton btn_exit;
	Server serv;
	
	public ServerGUI(Server serv) {
		this.serv = serv;
	}

	public void run() {
		InetAddress inet;
		
		frame = new JFrame();
		p_top = new JPanel();
		p_middle = new JPanel();
		btn_off = new JButton("공유중단");
		btn_exit = new JButton("나가기");
		
		try {
			inet = InetAddress.getLocalHost();
			lb_ip = new JLabel("\tIP : " + inet.getHostAddress());
		}catch(UnknownHostException e) {
			e.printStackTrace();
		}
		
		btn_off.addActionListener(this);
		btn_exit.addActionListener(this);
		
		/* 패널위에 Label 등을 세팅 */
		p_top.setLayout(new FlowLayout());
		p_top.add(lb_ip);
		p_top.add(btn_off);
		p_top.add(btn_exit);
		p_middle.setLayout(new FlowLayout());
		
		/* 프레임 위에 패널 등을 세팅 */
		frame.setLayout(new BorderLayout());
		frame.add(p_top, BorderLayout.NORTH);
		frame.add(p_middle, BorderLayout.CENTER);
		
		/* 그 외 세팅 */
		frame.setTitle("서버");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setSize(480, 320);
		frame.setVisible(true);
	}//run

	public void actionPerformed(ActionEvent e) {
		int count = 0;
		
		/* 화면공유 종료 */
		if(e.getSource() == btn_off) {
			System.out.println("[ServerGUI]Stop Sharing");
			for(int i=0; i<serv.getList().size(); i++) {
				/* Service Thread 에게
				 * 화면공유를 중단한다고 알림 */
				ServiceThread st = serv.getList().get(i);
				if(st.servShare != null) {
					st.servShare.isSharing = false;
				}
				st.isSharing = false;
				st.isShare = false;
				try {
					serv.setStop();
				} catch(IOException e1) {
					e1.printStackTrace();
				}
			}
		}//btn_off
		/* 서버 종료 */
		else if(e.getSource() == btn_exit) {
			System.out.println("[ServerGUI]Close Server");
			try {
				serv.signal("closeSocket");
				new Thread(new ServerExit(serv)).start();
			}catch (IOException e1) {
				e1.printStackTrace();
			}
		}//btn_exit
		
		/////*  클라이언트 화면 선택 시  */////
		
		/* 현재 공유 중인 화면이 있는지 확인 */
		for(int i=0; i<serv.getList().size(); i++) {
			ServiceThread st = serv.getList().get(i);
			if(st.isSharing) {
				count++;
			}
		}
		/* 어떤 화면을 공유하기로 했는지 flag */
		for(int i=0; i<p_middle.getComponentCount(); i++) {
			if(p_middle.getComponent(i) == e.getSource()) {
				if(count != 0)
					alertServer("먼저 공유중인 화면을 종료하세요.");
				else {
					try {
						if(!serv.flagShare(i)) {
							System.err.println("flagShare() failed");
						}
						else {
							System.out.println("flagShare() success");
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}  
				}
			}
		}//for
	}//actionPerformed
	
	public void alertServer(String msg) {
		JOptionPane.showMessageDialog(null, msg);
	}
	
	public void giveActionListener(JButton btn) {
		btn.addActionListener(this);
	}
}
