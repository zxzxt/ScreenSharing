package io.github.hashbox;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


public class ServerShare implements Runnable {
	final int broadcast_port = 9001;
	final int recv_port = 9002;
	final double DATAGRAM_MAX_SIZE = 65527;
	boolean isSharing = true;

	Server serv;

	public ServerShare(Server serv) throws IOException {
		this.serv = serv;
	}

	public void run() {
		// TODO Auto-generated method stub
		System.out.println("[serverShare.java]server Share Thread START");
		
		try {
			byte buf[] = new byte[(int) DATAGRAM_MAX_SIZE];
            DatagramPacket recv_dp = new DatagramPacket(buf, buf.length);
            DatagramSocket recv_ds = new DatagramSocket(recv_port);
            DatagramSocket send_ds = new DatagramSocket();
            
			while(isSharing) {
				recv_ds.receive(recv_dp);
				System.out.println("[serverShare.java]Recv Screen sharing data.");
				for(int i=0; i<serv.getList().size(); i++) {
					ServerThread st = serv.getList().get(i);
					DatagramPacket send_dp = new DatagramPacket(recv_dp.getData(), recv_dp.getLength(), st.clntSock.getInetAddress(), broadcast_port);
					send_ds.send(send_dp);
				}
				System.out.println("[serverShare.java]Broadcast Screen sharing data.");
			}
			recv_ds.close();
			send_ds.close();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("[serverShare.java]Stop serverShare Thread.");
	}

}
