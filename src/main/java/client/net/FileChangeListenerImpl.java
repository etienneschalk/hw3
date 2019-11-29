package client.net;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import common.FileChangeListener;
import common.FileDTO;

public class FileChangeListenerImpl extends UnicastRemoteObject implements FileChangeListener {
	private static final long serialVersionUID = -2818971064730477277L;
	private String username;
	private static final String TAG_DETAILS = "( ͡° ͜ʖ ͡°) ";
	private static final String TAG_DOWNLOAD = "¯\\_(ツ)_/¯ ";
	private static final String TAG_OVERWRITE = " (╯ಠ‿ಠ)╯ ";
	

	public FileChangeListenerImpl(String username) throws RemoteException {
		this.username = username;
	}
	
	@Override
	public void fileChanged(FileDTO file, String accessor, String action) throws RemoteException {
		String actionSentence;
		String tag = TAG_DETAILS;
		if (action != null) {
			if ("DETAILS".equalsIgnoreCase(action)) {
				actionSentence = "have seen the details of the file ";
				tag = TAG_DETAILS;
			} else if ("DOWN".equalsIgnoreCase(action)) {
				actionSentence = "downloaded your file ";
				tag = TAG_DOWNLOAD;
			} else if ("UPW".equalsIgnoreCase(action)) {
				actionSentence = "overwrited your file ";
				tag = TAG_OVERWRITE;
			} else {
				actionSentence = "did something but no idea what with the file ";
			}
			if (username.equalsIgnoreCase(accessor)) {
				System.out.println(tag + "\n\t" + accessor + " (yourself)\n\t" + actionSentence + "\n\t'" + file.getName() +"'");
			} else {
				System.out.println(tag + "\n\t" + accessor + "\n\t" + actionSentence + "\n\t'" + file.getName() + "'");				
			}
		}
	}
	
	@Override
	public String getUsername() {
		return this.username;
	}

}
