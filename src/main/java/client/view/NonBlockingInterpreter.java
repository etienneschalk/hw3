package client.view;

import java.rmi.RemoteException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import client.net.FileChangeListenerImpl;

import client.net.TCPFileDownload;
import client.net.TCPFileUpload;

import common.FileCatalog;
import common.FileChangeListener;
import common.FileDTO;

/**
 * Non blocking interpreter, largely inspired from previous assignments. A
 * single loops continuously reads user input and delegates commands to a thread
 * pool with completable future.
 * 
 * @author etis3
 *
 */
public class NonBlockingInterpreter implements Runnable {
	private static final String PROMPT = "> ";
	private static final Scanner console = new Scanner(System.in);
	private boolean receivingCommands = false;
	private FileCatalog fileCatalog;
	private String jwtToken;
	
	private FileChangeListener fileChangeListener;

	/**
	 * Starts the interpreter, if not starting yet.
	 * 
	 * @param fileCatalog
	 */
	public void start(FileCatalog fileCatalog) {
		this.fileCatalog = fileCatalog;
		if (receivingCommands)
			return;
		jwtToken = null;
		receivingCommands = true;
		new Thread(this).start();
	}

	/**
	 * Loop for receiving commands
	 */
	@Override
	public void run() {
		while (receivingCommands) {
			try {
				CommandHandler commandHandler = new CommandHandler(readNextLine());
				switch (commandHandler.getCommand()) {
				case REGISTER:
					String username = commandHandler.getParam(1);
					String password = commandHandler.getParam(2);
					fileCatalog.register(username, password);
					break;
				case LOGIN:
					String username1 = commandHandler.getParam(1);
					String password1 = commandHandler.getParam(2);
					jwtToken = fileCatalog.login(username1, password1);
					try {
						this.fileChangeListener = new FileChangeListenerImpl(username1);
						fileCatalog.addFileChangeListener(this.fileChangeListener);
					} catch(RemoteException e) {
						niceErrorPrint(e);
						return;
					}
					break;
				case LIST:
					List<? extends FileDTO> files = fileCatalog.list(jwtToken);
					for (FileDTO file : files) {
						printDetails(file);
					}
					break;
				case DETAILS:
					String fileName = commandHandler.getParam(1);
					FileDTO file = fileCatalog.details(jwtToken, fileName);
					printDetails(file);
					break;
				case UPR:
					String pathFileToUploadReadOnly = commandHandler.getParam(1);
					String newFileNameOnServerReadOnly = commandHandler.getParam(2);
					fileCatalog.upload(jwtToken, newFileNameOnServerReadOnly, false);
					// TODO
					break;
				case UPW:
					String pathFileToUpload = commandHandler.getParam(1);
					String newFileNameOnServer = commandHandler.getParam(2);
					if (newFileNameOnServer == null || "".equals(newFileNameOnServer)) {
						newFileNameOnServer = pathFileToUpload.substring(pathFileToUpload.lastIndexOf('/') + 1);
					}
					new Thread(new TCPFileUpload(jwtToken, pathFileToUpload, newFileNameOnServer)).start();

//					fileCatalog.upload(jwtToken, newFileNameOnServerReadOnly, true);
					// TODO
					break;
				case DOWN:
					String fileNameToDL = commandHandler.getParam(1);
					String targetDirectory = commandHandler.getParam(2);
					String newNameDL = commandHandler.getParam(3);
					
					new Thread(new TCPFileDownload(jwtToken, targetDirectory, fileNameToDL, newNameDL)).start();
					//fileCatalog.download(jwtToken, fileNameToDL, targetDirectory, newNameDL);
					// TODO
					break;
				case DELETE:
					String fileNameToDelete = commandHandler.getParam(1);
					fileCatalog.delete(jwtToken, fileNameToDelete);
					// TODO
					break;
				case LOGOUT:
					jwtToken = null;
					fileCatalog.removeFileChangeListener(this.fileChangeListener);
					safePrintln("You have been logged out.");
					receivingCommands = false;
					safePrintln("Good bye!");
					break;
				case QUIT:
					jwtToken = null;
					fileCatalog.removeFileChangeListener(this.fileChangeListener);
					safePrintln("You have been logged out.");
					receivingCommands = false;
					safePrintln("Good bye!");
					break;
				case HELP:
					for (Command command : Command.values()) {
						if (command == Command.UNKNOWN) {
							continue;
						}
						safePrintln(command.toString().toLowerCase());
					}
					break;
				default:
					safePrintln("Unrecognized command.");
				}
			} catch (Exception e) {
				niceErrorPrint(e);
			}
		}
	}

	private String readNextLine() {
		safePrint(PROMPT);
		return console.nextLine();
	}

	private void safePrintln(String s) {
		synchronized (System.out) {
			System.out.println(s);
		}
	}

	private void safePrint(String s) {
		synchronized (System.out) {
			System.out.print(s);
		}
	}

	private void niceErrorPrint(Exception e) {
		safePrintln("[" + e.getClass().getName() + "]");
		safePrintln("\tMessage: " + e.getMessage());
		safePrintln("\tCause: " + e.getCause());
	}

	private void printDetails(FileDTO file) {
//		safePrintln(file.getName() + " - " + file.getPermission() + " - " + file.getOwnerName() + " - "
//				+ file.getSize().toString());
		if (file == null) {
			safePrintln("The file could not be retrieved or does not exist!");
		} else {
			safePrintln("[ " + file.getPermission() + " ] " + file.getSize().toString() + " \t" 
					+ normalizeFileNameString(file.getName(), 16) + " @ "
					+ file.getOwnerName());
		}
	}

	private String normalizeFileNameString(String fileName, int newSize) {
		int currentStringSize = fileName.length();
		if (currentStringSize > newSize) {
			String truncatedString = fileName.substring(0, newSize - 3);
			return truncatedString + "...";
		} else {
			String whiteSpaces = " ".repeat(newSize - currentStringSize);
			return fileName + whiteSpaces;
		}
		
	}

}
