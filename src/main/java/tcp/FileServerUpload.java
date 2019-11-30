package tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class FileServerUpload implements Runnable{
	private int currentId = 0;	// auto-increment id for clients
	private final String directory = "/User Files";
	private int portNo = 5553;
	private static final int LINGER_TIME = 5000;
	
	private void serve() {
        try {
            ServerSocket listeningSocket = new ServerSocket(portNo);
            System.out.println("Upload server started");
            while (true) {
                Socket clientSocket = listeningSocket.accept();
                startHandler(clientSocket);
            }
           
        } catch (IOException e) {
            System.err.println("Server failure.");
        }
    }

    private void startHandler(Socket clientSocket) throws SocketException {
        clientSocket.setSoLinger(true, LINGER_TIME);
        ClientHandler handler = new ClientHandler(this, clientSocket);

        Thread handlerThread = new Thread(handler);
        handlerThread.setPriority(Thread.MAX_PRIORITY);
        handlerThread.start();
    }

	@Override
	public void run() {
		// TODO Auto-generated method stub
		serve();
	}
}
