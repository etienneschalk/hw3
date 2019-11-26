package client.startup;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import client.view.NonBlockingInterpreter;
import common.FileCatalog;

public class ClientMain {
	private static final String TAG = "[" + ClientMain.class.getName() + "] ";
	private static final String ERROR_MESSAGE = " Could not start file catalog client.";
	
	public static void main(String[] argv) {
		try {
			new NonBlockingInterpreter().start((FileCatalog) Naming.lookup(FileCatalog.FILE_CATALOG_NAME_IN_REGISTRY));
		} catch (NotBoundException e) {
			 System.err.println(TAG + e.getClass().getName() + ERROR_MESSAGE);
		} catch (MalformedURLException e) {
			 System.err.println(TAG + e.getClass().getName() + ERROR_MESSAGE);
		} catch (RemoteException e) {
			 System.out.println(TAG + e.getClass().getName() + ERROR_MESSAGE);
		}
	}
}
