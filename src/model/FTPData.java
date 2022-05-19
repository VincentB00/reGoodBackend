package model;

public class FTPData 
{
	public String ip;
	public int port;
	public String username;
	public String password;
	public String defaultFolderPath;
	
	public FTPData(String ip, int port, String username, String password, String defaultFolderPath) {
		super();
		this.ip = ip;
		this.port = port;
		this.username = username;
		this.password = password;
		this.defaultFolderPath = defaultFolderPath;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDefaultFolderPath() {
		return defaultFolderPath;
	}

	public void setDefaultFolderPath(String defaultFolderPath) {
		this.defaultFolderPath = defaultFolderPath;
	}
	
	
}
