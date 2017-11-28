package io.github.gomd.ScreenSharing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import javax.imageio.ImageIO;

public class ServerShare implements Runnable {
	boolean isSharing = true;
	final int broadcast_port = 9001;
	final int recv_port = 9002;
	final double DATAGRAM_MAX_SIZE = 65527;
	
	Server serv;
	
	public ServerShare(Server serv) throws IOException {
		this.serv = serv;
	}
	
	public void run() {
		System.out.println("[ServerShare]Sharing Start");
		
		try {
			byte buf[] = new byte[(int) DATAGRAM_MAX_SIZE];
			DatagramPacket recv_dp = new DatagramPacket(buf, buf.length);
			DatagramSocket recv_ds = new DatagramSocket(recv_port);
			DatagramSocket send_ds = new DatagramSocket();
			
			while(isSharing) {
				recv_ds.receive(recv_dp); //클라이언트로 부터 화면을 받는다
				System.out.println("[ServerShare]Received screen sharing data");
				// 모든 클라이언트에게 받은 화면을 다시 뿌려준다
				for(int i=0; i<serv.getList().size(); i++) {
					ServiceThread st = serv.getList().get(i);
					DatagramPacket send_dp = new DatagramPacket(recv_dp.getData(), recv_dp.getLength(), st.clntSock.getInetAddress(), broadcast_port);
					send_ds.send(send_dp);
				}
				System.out.println("[ServerShare]Broadcast screen sharing data");
			}
			recv_ds.close();
			send_ds.close();
		} catch(SocketException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("[ServerShare]Stop serverShare Thread");
	}
}//class

	