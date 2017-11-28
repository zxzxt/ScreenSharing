package io.github.gomd.ScreenSharing;

public class ServerExit implements Runnable {
Server serv;
	
	public ServerExit(Server serv) {
		this.serv = serv;
	}

	public void run() {
		// TODO Auto-generated method stub
		while(serv.getList().size()==0 ? false : true) {

		}
		System.exit(0);
	}
}
