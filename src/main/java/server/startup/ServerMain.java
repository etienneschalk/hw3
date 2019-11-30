package server.startup;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import common.FileCatalog;
import server.controller.Controller;
import tcp.FileServerDownload;
import tcp.FileServerUpload;

public class ServerMain {
	public static final String fileCatalogName = FileCatalog.FILE_CATALOG_NAME_IN_REGISTRY;
	public static final String TAG = ServerMain.class.getSimpleName();
	private static final String ERROR_MESSAGE = " Could not start file catalog server.";

	/*
	 * SELECT * FROM `file`;
	 * 
	 * INSERT INTO file VALUES (1, "dsdsdsd", 1423, "http://", 1, true, 1);
	 */
	public static void main(String[] argv) {
		try {
			ServerMain server = new ServerMain();
			server.startRMIServant();
			System.out.println("File Catalog server started");
			
			new Thread(new FileServerUpload()).start();
			new Thread(new FileServerDownload()).start();
			
		} catch (Exception e) {
			niceErrorPrint(e);
		} 
	}

	private void startRMIServant() throws RemoteException, MalformedURLException {
		try {
			LocateRegistry.getRegistry().list();
		} catch (RemoteException noRegistryRunning) {
			LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
		}
		Naming.rebind(fileCatalogName, new Controller());
	}
	
	
	private static void niceErrorPrint(Exception e) {
		System.err.println(ERROR_MESSAGE);
		System.err.println("[" + TAG + "]");
		System.err.println("[" + e.getClass().getName() + "]");
		System.err.println("\tMessage: " + e.getMessage());
		System.err.println("\tCause: " + e.getCause());
	}


}
