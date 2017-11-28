package io.github.gomd.ScreenSharing;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class App implements ActionListener {
	
	private JFrame frame; //메인 프레임
	private JPanel p_choose; //선택 패널
	
	private JLabel lb_ip; // IP 표시 문구
	private JLabel lb_choose; //서버와 클라이언트를 선택 하는 문구
	private JButton btn_server; //서버 선택 버튼
	private JButton btn_client; //클라이언트 선택 버튼
	
	private Server classServer;
	private Client classClient;
	
    public static void main( String[] args ) {
        new App();
    }
    
    public App() {
    	InetAddress inet;
    	
    	frame = new JFrame();
    	p_choose = new JPanel();
    	
    	try {
    		inet = InetAddress.getLocalHost();
    		lb_ip = new JLabel("\tIP : " + inet.getHostAddress());
    	}catch (UnknownHostException e) {
    		e.printStackTrace();
    	}
    	
    	lb_choose = new JLabel("서버 또는 클라이언트를 선택해주세요.");
    	btn_server = new JButton("서버");
		btn_client = new JButton("클라이언트");
		
		/* 선택 패널 위에 Label 과 Button 추가  */
		p_choose.add(lb_choose);
		p_choose.add(btn_server);
		p_choose.add(btn_client);
		p_choose.add(lb_ip);
		
		/* Frame 위에 Panel 추가 */
		frame.add(p_choose);
		
		/* Click Event */
		btn_server.addActionListener(this);
		btn_client.addActionListener(this);
		
		/* 그 외 기타 설정 */
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setSize(260, 120);
		frame.setTitle("Screen_Sharing");
		frame.setVisible(true);
    }//Constructor 

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btn_client) {
			frame.setVisible(false);
			try {
				classClient = new Client();
			}catch(IOException e1) {
				e1.printStackTrace();
			}
			new Thread(classClient).start();
		}
		else if(e.getSource() == btn_server) {
			frame.setVisible(false);
			classServer = new Server();
			new Thread(classServer).start();
		}
	}//actionPerformed
}//CLASS

