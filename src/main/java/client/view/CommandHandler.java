package client.view;

public class CommandHandler {
	private String[] params;
	private Command command;
	private String userInput;
	
	public CommandHandler(String userInput) {
		this.userInput = userInput;
		parseUserInput();
	}
	
	private void parseUserInput() {
		params = this.userInput.trim().replaceAll("\\s+", " ").split(" ");
		for (int i = 0 ; i < params.length ; ++i) {
			if (i == 0) {
				try {
					command = Command.valueOf(params[i].toUpperCase());
				} catch(IllegalArgumentException iae) {
					command = Command.UNKNOWN;
				}
			}
		}
	}
	
	public Command getCommand() {
		return command;
	}
	
	public String getParam(int i) {
		if (i < params.length && i >= 0) {
			return params[i];
		}
		else {
			return null;
		}
	}
	
	public String getUserInput() {
		return userInput;
	}
}
