package io.github.gomd.ScreenSharing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/*
	GUI와 관련된 모든 이벤트를 관리.
*/

public class ClientGUI implements Runnable, ActionListener {
	JFrame frame;
	JPanel p_top;
	JPanel p_middle;
	JLabel lb_name;
	JLabel lb_screen;
	JButton btn_exit;
	Client clnt;
	
	public ClientGUI(Client clnt) {
		this.clnt = clnt;
	}
	
	public void run() {
		InetAddress inet;
		
		frame = new JFrame("클라이언트");
		p_top = new JPanel();
		p_middle = new JPanel();
		
		try {
			inet = InetAddress.getLocalHost();
			lb_name = new JLabel("\tYour Name : " + inet.getHostName());
		} catch(UnknownHostException e) {
			e.printStackTrace();
		}
		
		lb_screen = new JLabel("공유 없음");
		lb_screen.setVerticalAlignment(JLabel.CENTER);
		lb_screen.setBackground(Color.BLACK);

		btn_exit = new JButton("종료하기");
		
		p_top.add(lb_name);
		p_top.add(btn_exit);
		p_middle.add(lb_screen);
		
		frame.add(p_top);
		frame.add(p_middle);
		
		frame.setLayout(new BorderLayout());
		frame.add(p_top, BorderLayout.NORTH);
		frame.add(p_middle, BorderLayout.CENTER);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setUndecorated(false);
		frame.pack();
		frame.setSize(640, 520);
		
		btn_exit.addActionListener(this);
	}//run
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==btn_exit) {
			System.out.println("[clientGUI]Exiting Client Program.");
			try {
				clnt.exitClnt();   //exit_flag == true 로 변경
				frame.setVisible(false);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}//actionPerformed
	
	public void do_clntExit() {
		System.exit(0);
	}
	
	public void alertClient(String msg) {
		System.out.println(msg);
		JOptionPane.showMessageDialog(null, msg);
		System.exit(0);
	}	
}//ClientGUI
