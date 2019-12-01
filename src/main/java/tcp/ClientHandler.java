package tcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import common.TcpFile;
import common.TcpMessage;

public class ClientHandler implements Runnable {
	private int id;
	private FileServerUpload server;
	private FileServerDownload downloadServer;
    private final Socket clientSocket;
    private boolean connected;
    private DataInputStream fromClient;
    private DataOutputStream toClient;
    private FileInputStream fis;
    private BufferedOutputStream bos;
    private BufferedInputStream bis;
    private FileOutputStream fos;
    private BufferedReader br;
    private int bytesRead;
    private int current = 0;
    private String clientIntent;
	private String clientJwtString;
	private String providedFileName;
	private final String serverDirectory = "C:/Users/Gibson/Desktop/hm3server/";
	private final int okStatusCode = 200;
	private final int fileNotFoundStatusCode = 404;

	
	public ClientHandler(FileServerUpload server, Socket clientSocket) {
		this.server = server;
		this.clientSocket = clientSocket;
		connected = true;
		this.clientIntent = "upload";
		System.out.println("Client connected for file uploading");
	}
	
	public ClientHandler(FileServerDownload server, Socket clientSocket) {
		this.downloadServer = server;
		this.clientSocket = clientSocket;
		connected = true;
		this.clientIntent = "download";
		try {
			br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Client connected for file downloading");
	}

	/**
     * The run loop handling all communication with the connected client.
     */
    @Override
    public void run() {
    	try {
			fromClient = new DataInputStream(clientSocket.getInputStream());
			toClient = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
        if(clientIntent == "download") {
        	sendFileToClient();
        }
        else if(clientIntent == "upload") {
        	receiveFileFromClient();
        }
    }
    
    private void sendFileToClient() {
    	try {
    		int logCounter = 0;
    		System.out.println(logCounter++ + "Reading Client Download Request");
    		int messageLength;
    		messageLength = fromClient.readInt();
			char sharp = fromClient.readChar(); // delimiter ( '#' ) // 1 byte

    		int actuallyReadBytes = 0;
			int totalReadBytes = 0;
			byte[] serializedMessage = new byte[messageLength];

			while (actuallyReadBytes != -1 && (totalReadBytes < messageLength - 1)) {
				actuallyReadBytes = fromClient.read(serializedMessage, actuallyReadBytes,
						messageLength - totalReadBytes);
				totalReadBytes += actuallyReadBytes;
			}
			
			if (totalReadBytes == messageLength) {
				ByteArrayInputStream bais = new ByteArrayInputStream(serializedMessage);
				ObjectInputStream ois = new ObjectInputStream(bais);
				
				try {
					TcpMessage message = (TcpMessage)ois.readObject();
					String filename = message.getMessage();
		            String fullFilePath = serverDirectory + filename;
		        
		            File requestedFile = new File(fullFilePath);
		            
		            System.out.println(fullFilePath);
		            System.out.println(requestedFile.isDirectory());
		            System.out.println(requestedFile.exists());
		            
		            if(!requestedFile.isDirectory() && requestedFile.exists()) {
		            	fis = new FileInputStream(requestedFile);
		            	int fileSize = (int) requestedFile.length();
		            	TcpFile tcpFile = new TcpFile();
		    			tcpFile.setFileSize(fileSize);
		    			tcpFile.setResponseCode(okStatusCode);
		    			
		    			byte [] fileByteArray  = new byte [fileSize];
		    			
		    			bis = new BufferedInputStream(fis);
		    			try {
		    				bis.read(fileByteArray,0,fileByteArray.length);
		    				
		    				tcpFile.setFileContents(fileByteArray);

		    				byte[] dataToSend = getSerializedByteArray(tcpFile);
		    				toClient.writeInt(dataToSend.length);
		    				toClient.writeChar('#');
		    				toClient.write(dataToSend,0,dataToSend.length);
		    				toClient.flush();
		    			} catch (IOException e) {
		    				e.printStackTrace();
		    			}
		    			finally {
		    				if(fis != null)
		    					fis.close();
		    				if(bis != null)
		    					bis.close();
		    			}
		            }
		            else {
		            	TcpFile tcpFile = new TcpFile();
		            	tcpFile.setResponseCode(fileNotFoundStatusCode);
		            	byte[] dataToSend = getSerializedByteArray(tcpFile);
		            	
	    				toClient.writeInt(dataToSend.length);
	    				toClient.writeChar('#');
	    				toClient.write(dataToSend,0,dataToSend.length);
	    				toClient.flush();
		            }
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				
			}
           
		} catch (IOException e) {
			e.printStackTrace();
		}  	
    	finally {
    		if(clientSocket != null) {
    			try {
					clientSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    	}
    }
    
    private byte[] getSerializedByteArray(Object objectToSerialize) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);

			oos.writeObject(objectToSerialize);
			oos.flush();
			oos.close();

			return baos.toByteArray();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
    
    
    private void receiveFileFromClient() {
    	int logCounter = 0;
		System.out.println(logCounter++ + "Reading File Length");
		int messageLength;
		try {
			messageLength = fromClient.readInt();
			char sharp = fromClient.readChar(); // delimiter ( '#' ) // 1 byte

			System.out.println(logCounter++ + messageLength + sharp);
			System.out.println(logCounter++ + "Tries to read {messageLength} from Data Input Stream...");

			byte[] serializedMessage = new byte[messageLength];

			int actuallyReadBytes = 0;
			int totalReadBytes = 0;

			while (actuallyReadBytes != -1 && (totalReadBytes < messageLength - 1)) {
				actuallyReadBytes = fromClient.read(serializedMessage, actuallyReadBytes,
						messageLength - totalReadBytes);
				totalReadBytes += actuallyReadBytes;
			}
			
			if (totalReadBytes == messageLength) {
				System.out.println(logCounter++ + "Hourray");

				ByteArrayInputStream bais = new ByteArrayInputStream(serializedMessage);
				ObjectInputStream ois = new ObjectInputStream(bais);
				try {
					TcpFile clientMessage = (TcpFile) ois.readObject();
					
					fos = new FileOutputStream(serverDirectory + clientMessage.getFilename());
					bos = new BufferedOutputStream(fos);
					
					bos.write(clientMessage.getFileContents(), 0, clientMessage.getFileContents().length);
					
					fos.close();
					bos.close();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				
			} else {
				throw new IOException("Data received is not complete.");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		finally {
			if(clientSocket != null)
				try {
					clientSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
    }
}
