package io.github.hashbox;

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
		// TODO Auto-generated method stub
		InetAddress inet;
		
		frame = new JFrame();
		p_top = new JPanel();
		p_middle = new JPanel();
		btn_off = new JButton("화면공유 끄기");
		btn_exit = new JButton("나가기");
		
		try {
			inet = InetAddress.getLocalHost();
			lb_ip = new JLabel("\tIP : " + inet.getHostAddress());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		btn_off.addActionListener(this);
		btn_exit.addActionListener(this);
		
		p_top.setLayout(new FlowLayout());
		p_top.add(lb_ip);
		p_top.add(btn_off);
		p_top.add(btn_exit);
		p_middle.setLayout(new FlowLayout());
		
		frame.setLayout(new BorderLayout());
		frame.add(p_top, BorderLayout.NORTH);
		frame.add(p_middle, BorderLayout.CENTER);
		
		frame.setTitle("서버 프로그램");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setSize(480, 320);
		frame.setVisible(true);
	}
	public void actionPerformed(ActionEvent e) {
		int count = 0;
		// TODO Auto-generated method stub
		//화면공유 끄기 버튼
		if(e.getSource()==btn_off) {
			System.out.println("[serverGUI.java]Stop Sharing");
			for(int i=0; i<serv.getList().size(); i++) {
				ServerThread st = serv.getList().get(i);
				st.serv_share.isSharing = false;
				st.isSharing = false;
				st.isShare = false;
				try {
					serv.broadcast("stopShare");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		else if(e.getSource()==btn_exit) {
			System.out.println("[serverGUI.java]Close Server");
			try {
				serv.broadcast("closeSocket");
				new Thread(new ServerExit(serv)).start();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		for(int i=0; i<serv.getList().size(); i++) {
			ServerThread st = (ServerThread)serv.getList().get(i);
			if(st.isSharing) {
				count++;
			}
		}
		for(int i=0; i<p_middle.getComponentCount();i++) {
			if(p_middle.getComponent(i)==e.getSource()) {
				if(count!=0) {
					alertServer("먼저 화면 공유 끄기를 실행해주세요.");
				}
				else {
					try {
						if(!serv.doShare(i)) {
							System.err.println("Excute doShare() Failed");
						}
						else {
							System.out.println("Excute doShare() Success");
						}
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
	}
	
	public void makeAction(JButton btn) {
		btn.addActionListener(this);
	}
	public void alertServer(String msg) {
		JOptionPane.showMessageDialog(null, msg);
	}
}
