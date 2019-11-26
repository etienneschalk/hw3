package server.startup;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import common.FileCatalog;
import server.controller.Controller;

public class ServerMain {
	public static final String fileCatalogName = FileCatalog.FILE_CATALOG_NAME_IN_REGISTRY;
	public static final String TAG = "[" + ServerMain.class.getSimpleName() + "] ";
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
		} catch (MalformedURLException e) {
			System.err.println("MalformedURLException");
			System.err.println(TAG + e.getClass().getName() + ERROR_MESSAGE);
		} catch (RemoteException e) {
			System.err.println("RemoteException");
			System.out.println(TAG + e.getClass().getName() + ERROR_MESSAGE);
			e.printStackTrace();
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

}
