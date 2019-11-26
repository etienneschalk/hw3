package client.view;

import java.util.List;
import java.util.Scanner;

import common.FileCatalog;
import common.FileDTO;
import server.model.FileException;

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
	private FileCatalog fileCatalog;
	private boolean receivingCommands = false;

	/**
	 * Starts the interpreter, if not starting yet.
	 * 
	 * @param fileCatalog
	 */
	public void start(FileCatalog fileCatalog) {
		this.fileCatalog = fileCatalog;
		if (receivingCommands)
			return;
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
					fileCatalog.login(username1, password1);
					break;
				case LIST:
					List<? extends FileDTO> files = fileCatalog.list();
					for (FileDTO file : files) {
						safePrintln(file.getName() + " - " + file.getPermission() + " - "
								+ file.getOwnerName() + " - " + file.getSize().toString());
					}
					break;
				case DETAILS:
					String fileName = commandHandler.getParam(1);
					fileCatalog.details(fileName);
					break;
				case UPLOAD:
					String path = commandHandler.getParam(1);
					// TODO
					break;
				case DOWNLOAD:
					String fileName1 = commandHandler.getParam(1);
					String targetDirectory = commandHandler.getParam(2);
					// TODO
					break;
				case DELETE:
					String fileName11 = commandHandler.getParam(1);
					// TODO
					break;
				case QUIT:
					receivingCommands = false;
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

}
