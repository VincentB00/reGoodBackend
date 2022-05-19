package model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class SimpleFTPClient 
{
	public FTPClient client;
	public String ip;
	public int port;
	public String username;
	public String password;
	public String defaultFolderPath;
	
	public SimpleFTPClient(String ip, int port, String username, String password, String defaultFolderLocation)
	{
		client = new FTPClient();
		this.ip = ip;
		this.port = port;
		this.username = username;
		this.password = password;
		this.defaultFolderPath = defaultFolderLocation;
	}
	
	public boolean connect(boolean displayProtocal)
	{
		try
		{
			if(displayProtocal)
				client.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
			
			client.connect(ip, port);
			
			int reply = client.getReplyCode();
	        
	        if (!FTPReply.isPositiveCompletion(reply)) 
	        {
	        	client.disconnect();
	            throw new IOException("Exception in connecting to FTP Server");
	        }

	        client.login(username, password);
	        
	        
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			disconnect();
			return false;
		}
		
		return true;
	}
	
	public void disconnect()
	{
	    try 
	    {
	    	client.logout();
			client.disconnect();
		} 
	    catch (IOException e) 
	    {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public FTPFile[] listFile(String folderPath)
	{
		try
		{
			return client.listFiles(folderPath);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public FTPFile[] listFile()
	{
		try
		{
			return client.listFiles(defaultFolderPath);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean makeDirectory(String folderLocation)
	{
		try
		{
			String[] folderPath = folderLocation.split("/");
			int count = 0;
			String path = "";
			
			while(count < folderPath.length)
			{
				path += "/" + folderPath[count];
				client.makeDirectory(path);
				count++;
			}
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
			
		return true;
	}
	
	public boolean storeFile(String folderLocation, String fileName, String[] lines)
	{
		try 
		{
			boolean makeFile = makeDirectory(folderLocation);
			
			if(!makeFile)
				return false;
			
			client.changeWorkingDirectory(folderLocation);
			client.storeFile(fileName, new ByteArrayInputStream(lines[0].getBytes()));
			
			OutputStream os = client.appendFileStream(fileName);
			
			for(int count = 1; count < lines.length; count++)
			{
				String line = "\n" + lines[count];
				os.write(line.getBytes());
			}
			
			os.close();
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean storeFile(String folderLocation, String fileName, String[] lines, int startIndex, int endIndex)
	{
		try 
		{
			boolean makeFile = makeDirectory(folderLocation);
			
			if(!makeFile)
				return false;
			
			client.changeWorkingDirectory(folderLocation);
			client.storeFile(fileName, new ByteArrayInputStream(lines[startIndex].getBytes()));
			
			OutputStream os = client.appendFileStream(fileName);
			
			for(int count = startIndex + 1; count < endIndex + 1; count++)
			{
				String line = "\n" + lines[count];
				os.write(line.getBytes());
			}
			
			os.close();
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean storeFile(String folderLocation, String fileName, InputStream fileInputSteam)
	{
		try 
		{
			boolean makeFile = makeDirectory(folderLocation);
			
			if(!makeFile)
				return false;
			
			client.changeWorkingDirectory(folderLocation);
			client.storeFile(fileName, fileInputSteam);
//			client.storeUniqueFile(fileName, fileInputSteam);
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean storeFile(String folderLocation, String fileName, String fileContent)
	{
		return storeFile(folderLocation, fileName, new ByteArrayInputStream(fileContent.getBytes()));
	}
	
	public boolean ConnectStoreFileAndClose(String folderLocation, String fileName, String fileContent)
	{
		connect(false);
		boolean result = storeFile(folderLocation, fileName, new ByteArrayInputStream(fileContent.getBytes()));
		disconnect();
		return result;
	}
	
	public boolean ConnectStoreFileAndClose(String folderLocation, String fileName, String[] lines, int startIndex, int endIndex)
	{
		connect(false);
		boolean result = storeFile(folderLocation, fileName, lines, startIndex, endIndex);
		disconnect();
		return result;
	}
}
