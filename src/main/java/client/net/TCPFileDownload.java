package client.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;

public class TCPFileDownload implements Runnable{
	private static final int TIMEOUT_HALF_HOUR = 1800000;
    private static final int TIMEOUT_HALF_MINUTE = 30000;
    private Socket socket;
    
    private BufferedInputStream bis = null;
    private OutputStream os = null;
    private InputStream fromServer;
    private FileOutputStream fos;
    private BufferedOutputStream bos;
    private String jwtToket;
    private String pathToSaveFile;
    private String fileToDownload;
    private String newFilename;
	private boolean connected = false;
	private int bytesRead;
    private int current = 0;
	
	private static final String host = "localhost";
	private static final int port = 5554;
	

	public TCPFileDownload(String jwtToken, String pathToSaveFile, String fileToDownload, String newFilename) {
		this.jwtToket = jwtToken;
		this.pathToSaveFile = pathToSaveFile;
		this.fileToDownload = fileToDownload;
		this.newFilename = newFilename;
		
		// TODO Auto-generated method stub
		try {
			connect();
			downloadFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
     * Creates a new instance and connects to the specified server. Also starts a listener thread
     * receiving broadcast messages from server.
     *
     * @param host             Host name or IP address of server.
     * @param port             Server's port number.
     * @param broadcastHandler Called whenever a broadcast is received from server.
     * @throws IOException If failed to connect.
     */
    private void connect() throws
            IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), TIMEOUT_HALF_MINUTE);
        socket.setSoTimeout(TIMEOUT_HALF_HOUR);
        connected = true;
    }
    
    private void downloadFile() {
    	try {
			os = socket.getOutputStream();
			byte[] requestString = fileToDownload.getBytes(Charset.forName("UTF-8"));
			System.out.println(new String(requestString));
			os.write(requestString,0,requestString.length);
			os.flush();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	@Override
	public void run() {
//		while(connected) {
//			try {
//				fromServer = socket.getInputStream();
//				
//				byte[] byteArray  = new byte [6022386];
//	            bytesRead = fromServer.read(byteArray,0,byteArray.length);
//	            current = bytesRead;
//	            
//	            do {
//	                bytesRead =
//	                   fromServer.read(byteArray, current, (byteArray.length-current));
//	                if(bytesRead >= 0) current += bytesRead;
//	             } while(bytesRead > -1);
//	            
//	            if(byteArray.length > 0) {
//	            	System.out.println("File received");
//	            	
//	            	String del = " ";
//	                byte[] delimeterArray = del.getBytes(Charset.forName("UTF-8"));
//	                byte delimeter = delimeterArray[0];
//	                
//	                byte[] serverResultByteArray = new byte[3];
//	                
//	                //extract server result..if error or not
//	                int fileStartIndex = 0;
//	                for(byte b : byteArray){
//	                    if(fileStartIndex < 3) {
//	                    	serverResultByteArray[fileStartIndex] = b;
//	                    }
//	                    fileStartIndex++;
//	                }
//	                
//	                String serverResult = new String(serverResultByteArray);
//	            	if(serverResult.equals("200")) {
//	            		//file was found we read the result
//	            		fos = new FileOutputStream(pathToSaveFile + newFilename);
//	            		bos = new BufferedOutputStream(fos);
//	            		
//	            		bos.write(byteArray);
//	            		bos.flush();
//	            	}
//	            	else if(serverResult.equals("404")){
//	            		System.out.println("Error. Requested file not found on server.");
//	            	}
//	            	else {
//	            		System.out.println("Unknown error occured.");
//	            	}
//	            	
//	            	bos.close();
//	            	fos.close();
//	            	socket.close();
//	            }
//	            
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		}
	}
}
