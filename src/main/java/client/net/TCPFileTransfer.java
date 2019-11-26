package client.net;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPFileTransfer {
	private static final String host = "localhost";
	private static final int port = 5553;
	
	public static void upload(String fileOutputPath) throws UnknownHostException, IOException {
		Socket socket = new Socket(TCPFileTransfer.host, TCPFileTransfer.port); 
        InputStream is = socket.getInputStream();
        FileOutputStream fos = new FileOutputStream(fileOutputPath);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        byte[] readBytes = is.readAllBytes();
        bos.write(readBytes);
        bos.close();
        socket.close();
	}
}
