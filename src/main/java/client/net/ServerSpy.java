package client.net;

import java.rmi.RemoteException;

import common.FileCatalog;

/**
 * This class list to the server. She runs in a separate thread and constantly wait for the corresponding 
 * server method to respond. The life cycle is the following: the class is run in a separate thread ;
 * then she ask server for notification, and blocks. When the server respond, the loop call once again the
 * ask server for notification.
 * @author etis3
 *
 */
public class ServerSpy implements Runnable {
	private FileCatalog fileCatalog;
	private String jwtToken;
	
	public ServerSpy(String jwtToken, FileCatalog fc) {
		this.fileCatalog = fc;
	}
	
	@Override
	public void run() {
		boolean spying = true;
		String notification;
		safePrintln("[ServerSpy] Started");
		while (spying) {
			try {
				notification = fileCatalog.waitForNotification(jwtToken);
				safePrintln("[ServerSpy] " + notification);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void safePrintln(String s) {
		synchronized (System.out) {
			System.out.println(s);
		}
	}
}
