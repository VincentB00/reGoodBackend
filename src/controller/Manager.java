package controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.net.ftp.FTP;
import org.apache.http.ParseException;
import org.apache.http.message.BasicNameValuePair;

import com.github.javafaker.Faker;
import com.google.gson.Gson;

import model.AdditionalData;
import model.FTPData;
import model.HttpResponseData;
import model.Item;
import model.MultipartParser;
import model.SQL;
import model.SQLData;
import model.SimpleFTPClient;
import model.SimpleHttpClient;
import model.StatusException;
import model.User;

public class Manager 
{
	public SQL sql;
	public Gson gson;
	public Random random;
	public final String folderPath = "/volume1/FrontEndServer/Public/file_manager/reGood/";
	public SimpleFTPClient ftpClient;
	public final String apacheURL = "vincentprivatenas.mynetgear.com:90/file_manager/reGood";
	public SimpleHttpClient httpClient;
	public final String AUTHWEBSITE = "reGood";
	
	public Manager()
	{
		gson = new Gson();
		sql = null;
		random = new Random();
		httpClient = new SimpleHttpClient("vincentprivatenas.mynetgear.com:7070/Authentication/rest");
	}
	
	public Manager(String ip, int port, String username, String password, String schema)
	{
		sql = new SQL(ip, port, username, password, schema);
		gson = new Gson();
		random = new Random();
	}
	
	public void setSQL(String jsonBody) throws StatusException
	{
		SQLData sqlData = gson.fromJson(jsonBody, SQLData.class);
		
		if(sqlData != null && sqlData.schema != null && sqlData.schema.compareTo("") != 0)
		{
			sql = new SQL(sqlData.ip, sqlData.port, sqlData.username, sqlData.password, sqlData.schema);
		}
		else
			throw new StatusException(400, "Body package", gson);
	}
	
	public String checkSQLServerStatus() throws StatusException
	{
		try(ResultSet rs = sql.executeQuery(String.format("%s.SERVER STATUS ONLINE CHECK();", sql.schema)))
		{
			rs.close();
			return Manager.toJson("server_check", "pass");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return Manager.toJson("server_check", "fail");
		}
	}
	
	public void setFTPClient(String jsonBody) throws StatusException
	{
		FTPData ftpData = gson.fromJson(jsonBody, FTPData.class);
		
		if(ftpData != null && ftpData.ip != null && ftpData.port >= 0 && ftpData.username != null && ftpData.password != null && ftpData.defaultFolderPath != null)
		{
			ftpClient = new SimpleFTPClient(ftpData.ip, ftpData.port, ftpData.username, ftpData.password, ftpData.defaultFolderPath);
		}
		else
			throw new StatusException(400, "Body package", gson);
	}
	
	public static boolean isNumber(String value)
	{
		
		try
		{
			Double.parseDouble(value);
			return true;
		}
		catch(Exception ex)
		{
			return false;
		}
	}
	
	public static String toJson(String variable, String obj)
	{
		return String.format("{\"%s\":\"%s\"}", variable, obj);
	}
	public static String toJson(String variable, int obj)
	{
		return String.format("{\"%s\":%s}", variable, obj);
	}
	public static String toJson(String variable, double obj)
	{
		return String.format("{\"%s\":%s}", variable, obj);
	}
	
//	public static String toJson(Pair[] pairs)
//	{
//		String result = "{";
//		for(int count = 0; count < pairs.length; count++)
//		{	
//			String key = pairs[count].getKey().toString();
//			String value = pairs[count].getValue().toString();
//			if(count >= (pairs.length - 1))
//			{
//				if(isNumber(value))
//					result += String.format("\"%s\":%s", key, value);
//				else
//					result += String.format("\"%s\":\"%s\"", key, value);
//			}
//			else
//			{
//				if(isNumber(value))
//					result += String.format("\"%s\":%s,", key, value);
//				else
//					result += String.format("\"%s\":\"%s\",", key, value);
//			}
//		}
//		result += "}";
//		return result;
//	}
	
	public static String toJson(BasicNameValuePair[] pairs)
	{
		String result = "{";
		for(int count = 0; count < pairs.length; count++)
		{	
			String key = pairs[count].getName().toString();
			String value = pairs[count].getValue().toString();
			if(count >= (pairs.length - 1))
			{
				if(isNumber(value))
					result += String.format("\"%s\":%s", key, value);
				else
					result += String.format("\"%s\":\"%s\"", key, value);
			}
			else
			{
				if(isNumber(value))
					result += String.format("\"%s\":%s,", key, value);
				else
					result += String.format("\"%s\":\"%s\",", key, value);
			}
		}
		result += "}";
		return result;
	}
	
	public String checkServerStatus() throws StatusException
	{
		String status = "normal";
		String result = "{";
		if(sql == null)
		{
			result += "\"SQL_connection\":\"fail\"";
			status = "bad";
			result += String.format(",\"status\":\"%s\"}", status);
			return result;
		}
		
		try(ResultSet rs = sql.executeQuery("select now();"))
		{
			String temp = "";
			while(rs.next())
				temp = rs.getString(1);
			
			rs.close();
			
			if(temp == null || temp.compareTo("") == 0)
			{
				status = "bad";
				result += "\"SQL_connection\":\"fail\"";
			}
			else
			{
				result += "\"SQL_connection\":\"pass\"";
				result += String.format(",\"SQL_datetime_test\":\"%s\"", temp);
			}
				
		}
		catch(Exception ex)
		{
			status = "bad";
			result += "\"SQL_connection\":\"fail\"";
		}
		
		sql.closeConnection();	
		
		result += String.format(",\"status\":\"%s\"}", status);
		return result;
	}
	
	public void log(String log)
	{
		sql.executeUpdate(String.format("INSERT INTO %s.log (log) VALUES ('%s');", sql.schema, log));
	}
	
	
	private Object tryGetParameter(HttpServletRequest request, String parameterName)
	{
		try
		{
			Object obj = request.getParameter(parameterName);
			return obj;
		}
		catch(Exception ex)
		{
			return null;
		}
	}
	
	///////////////////////////request item/////////////////////
	
	public String requestItem(int user_id, String jsonBody) throws StatusException
	{
		AdditionalData ad = gson.fromJson(jsonBody, AdditionalData.class);
		
		if(ad.item_id <= 0)
			throw new StatusException(400, "missing or invalid item_id");
		
		int request_id = 0;
		
		try(ResultSet rs = sql.executeQuery(String.format("call %s.`Request Item`(%s, %s, '%s');", sql.schema, user_id, ad.item_id, ad.message)))
		{
			while(rs.next())
			{
				request_id = rs.getInt(1);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new StatusException(500);
		}
		
		sql.closeConnection();
		
		if(request_id <= 0)
			throw new StatusException(403, "item already been request by user");
		
		return Manager.toJson("request_id", request_id);
	}
	
	
	///////////////////////////item/////////////////////////////
	
	public int getPostedItemSize() throws StatusException
	{
		int size = -1;
		try(ResultSet rs = sql.executeQuery(String.format("select count(post_items.item_id) as size from %s.post_items;", sql.schema)))
		{
			while(rs.next())
				size = rs.getInt(1);
			
			rs.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new StatusException(500);
		}
		
		sql.closeConnection();
		
		if(size <= -1)
			throw new StatusException(500);
		
		return size;
	}
	
	public String getAllUserPostedItem(int user_id) throws StatusException
	{
		LinkedList<Item> itemList = new LinkedList<>();
		
		try(ResultSet rs = sql.executeQuery(String.format("call %s.`GET: User's posted items`(%s);", sql.schema, user_id)))
		{
			while(rs.next())
			{
				Item item = new Item(rs.getInt("item_id"), rs.getString("name"), rs.getString("description"), rs.getString("category"), rs.getString("item_condition"), rs.getString("city"), rs.getString("state"), rs.getString("zip"));
				itemList.add(item);
			}
			
			rs.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new StatusException(500);
		}
		
		sql.closeConnection();
		
		return gson.toJson(itemList.toArray(new Item[0]));
	}
	
	public void deleteItem(int item_id) throws StatusException
	{
		int effectRow = sql.executeUpdate(String.format("call %s.`Delete Item`(%s);", sql.schema, item_id));
		
		if(effectRow <= 0)
			throw new StatusException(202);
	}
	
	public String updateItem(String jsonBody) throws StatusException
	{
		Item item = gson.fromJson(jsonBody, Item.class);
		
		if(item.isMissingAnyNN())
			throw new StatusException(418, "missing important information");
		else
		{
			int effectRow = sql.executeUpdate(String.format("call %s.`Update item`('%s', '%s', '%s', '%s', '%s', %s, '%s', %s);", sql.schema, item.name, item.description, item.category, item.city, item.state, item.zipCode, item.condition, item.item_id));
			
			if(effectRow <= 0)
				throw new StatusException(202);
		}
		
		return Manager.toJson("item_id", item.item_id);
	}
	
	public String postItem(String jsonBody, int user_id) throws StatusException
	{
		Item item = gson.fromJson(jsonBody, Item.class);
		int itemID = -1;
		
		if(item.isMissingAnyNN())
			throw new StatusException(418, "missing important information");
		else
		{
			try(ResultSet rs = sql.executeQuery(String.format("call %s.`INPUT: Post linked to user`('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');", sql.schema, item.name, item.description, item.category, item.city, item.state, item.zipCode, item.condition, user_id)))
			{
				while(rs.next())
				{
					itemID = rs.getInt(1);
				}
				
				rs.close();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				throw new StatusException(500);
			}
			
			sql.closeConnection();
		}
		
		if(itemID <= 0)
			throw new StatusException(500);
		
		return Manager.toJson("item_id", itemID);
	}
	
	public String getItemFromRange(int rowIndex, int range) throws StatusException
	{
		LinkedList<Item> itemList = new LinkedList<>();
		
		try(ResultSet rs = sql.executeQuery(String.format("call %s.`GET: Info from item_id (range)`(%s, %s);", sql.schema, rowIndex, range)))
		{
			while(rs.next())
			{
				Item item = new Item(rs.getInt("item_id"), rs.getString("name"), rs.getString("description"), rs.getString("category"), rs.getString("item_condition"), rs.getString("city"), rs.getString("state"), rs.getString("zip"));
				itemList.add(item);
			}
			
			rs.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new StatusException(500);
		}
		
		sql.closeConnection();
		
		return gson.toJson(itemList.toArray(new Item[0]));
	}
	
	public String getItem(int id) throws StatusException
	{
		Item item = null;
		
		try(ResultSet rs = sql.executeQuery(String.format("SELECT * FROM %s.post_items WHERE item_id = %s;", sql.schema, id)))
		{
			while(rs.next())
			{
				item = new Item(rs.getInt("item_id"), rs.getString("name"), rs.getString("description"), rs.getString("category"), rs.getString("item_condition"), rs.getString("city"), rs.getString("state"), rs.getString("zip"));
			}
			
			rs.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		sql.closeConnection();
		
		return gson.toJson(item);
	}
	
	public String getItems() throws StatusException
	{
		LinkedList<Item> items = new LinkedList<Item>();
		
		try(ResultSet rs = sql.executeQuery(String.format("SELECT * FROM %s.post_items;", sql.schema)))
		{
			while(rs.next())
			{
				items.add(new Item(rs.getInt("item_id"), rs.getString("name"), rs.getString("description"), rs.getString("category"), rs.getString("item_condition"), rs.getString("city"), rs.getString("state"), rs.getString("zip")));
			}
			
			rs.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		sql.closeConnection();
		
		return gson.toJson(items.toArray());
	}
	///////////////////////////file manager//////////////////////////////
	
	public String getItemURL(int item_id) throws StatusException
	{
		LinkedList<String> urlList = new LinkedList<>();
		
		try(ResultSet rs = sql.executeQuery(String.format("call %s.`GET: Links from item_id`(%s);", sql.schema, item_id)))
		{
			while(rs.next())
			{
				urlList.add(rs.getString("link"));
			}
			
			rs.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new StatusException(500);
		}
		
		sql.closeConnection();
		
		return gson.toJson(urlList.toArray(new String[0]));
	}
	
	public String[] storeFile(HttpServletRequest request, int itemID) throws StatusException
	{
		if(!ServletFileUpload.isMultipartContent(request) || itemID <= 0)
			throw new StatusException(400, "bad body request | not from-data package type");
		
		try
		{
//			int maxMemSize = 4024 * 4024;
//			int maxFileSize = 4024 * 4024;

			DiskFileItemFactory fileFactory = new DiskFileItemFactory();
//			fileFactory.setSizeThreshold(maxMemSize);
			
			ServletFileUpload uploader = new ServletFileUpload(fileFactory);
//			uploader.setFileSizeMax(maxFileSize);
			
			List<FileItem> fileItemsList = uploader.parseRequest(request);
			Iterator<FileItem> fileItemsIterator = fileItemsList.iterator();
			
			LinkedList<String> urls = new LinkedList<>();
			
			while(fileItemsIterator.hasNext())
			{
				FileItem fileItem = fileItemsIterator.next();
				String fileName = fileItem.getName();
				if(fileName != null)
				{
					String fileContentType = fileItem.getContentType();
					
					ftpClient.connect(false);
					
					if(fileContentType.contains("text") || fileContentType.contains("json"))
						ftpClient.client.setFileType(FTP.ASCII_FILE_TYPE);
					else
						ftpClient.client.setFileType(FTP.BINARY_FILE_TYPE);
					
			        ftpClient.storeFile(ftpClient.defaultFolderPath + "/item_id " + itemID, fileName, fileItem.getInputStream());
			        ftpClient.disconnect();
			        
			        String url = "http://" + apacheURL + "/item_id " + itemID + "/" + fileName;
					url = url.replace(" ", "%20");
					urls.add(url);
				}
			}
			
			int effectRow = 0;
			for(String url : urls)
			{
				effectRow += sql.executeUpdate(String.format("call %s.`INPUT: Url into links`(%s, '%s');", sql.schema, itemID, url));
				sql.closeConnection();
			}
			
			if(effectRow < urls.size())
				throw new StatusException(500, "sql error");
			
			return urls.toArray(new String[0]);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new StatusException(500);
		}
		
		
	}
	
	public String storeFile(String fromDataString) throws StatusException
	{
		try
		{
			String url;
			MultipartParser mp = new MultipartParser(fromDataString);
			int itemID = Integer.parseInt(mp.getValue("item_id"));
			String fileName = mp.get("file").fileName;
			String folderLocation = ftpClient.defaultFolderPath + "/item_id " + itemID;
			int beginIndex = mp.get("file").beginValueIndex;
			int endIndex = mp.get("file").endValueIndex;
			String contentType = mp.get("file").contentType;
			
//			System.out.println(mp.get("item_id").beginValueIndex);
//			System.out.println(mp.get("item_id").endValueIndex);
//			System.out.println(mp.get("file").beginValueIndex);
//			System.out.println(mp.get("file").endValueIndex);
			

			if(itemID == 0 || fileName == null)
				throw new StatusException(400, "bad body request");
			
			ftpClient.connect(false);
			
			if(contentType.contains("text") || contentType.contains("json"))
				ftpClient.client.setFileType(FTP.ASCII_FILE_TYPE);
			else
				ftpClient.client.setFileType(FTP.BINARY_FILE_TYPE);
			
			boolean isStore = ftpClient.storeFile(folderLocation, fileName, mp.getLines(), beginIndex, endIndex);
			
			ftpClient.disconnect();
			
			if(!isStore)
				throw new StatusException(500, "server experience technical difficulty");
			
			url = apacheURL + "/item_id " + itemID + "/" + fileName;
			url = url.replace(" ", "%20");
			return url;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new StatusException(500);
		}
		
		
	}
	
	///////////////////////////token/////////////////////////////
	
	public boolean isValidToken(String token) 
	{
		try
		{
			BasicNameValuePair[] pairs = new BasicNameValuePair[] {new BasicNameValuePair("token", token)};
			
			HttpResponseData hrd = httpClient.makeGetRequest("/status", pairs);
			
			return gson.fromJson(hrd.rawBody, AdditionalData.class).session.compareTo("alive") == 0;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	public void checkValidToken(String jsonBody) throws StatusException
	{
		String token = gson.fromJson(jsonBody, AdditionalData.class).token;
		
		if(token == null)
			throw new StatusException(401, "session/token expire or invalid");
		
		boolean valid = isValidToken(token);
		
		if(!valid)
			throw new StatusException(401, "session/token expire or invalid");
	}
	
	public String getToken(String jsonBody)
	{
		return gson.fromJson(jsonBody, AdditionalData.class).token;
	}
	
	///////////////////////////user//////////////////////////////
	
	public User getSessionUserData(String jsonBody) throws StatusException
	{
		try
		{
			checkValidToken(jsonBody);
			
			String token = getToken(jsonBody);
			
			BasicNameValuePair[] pairs = new BasicNameValuePair[] {new BasicNameValuePair("token", token)};
			
			User user = gson.fromJson(httpClient.makeGetRequest("/session_data", pairs).rawBody, User.class);
			User U2 = gson.fromJson(httpClient.makeGetRequest("/user_data", pairs).rawBody, User.class);
			
			user.username = U2.username;
			user.email = U2.email;
			user.full_name = U2.full_name;
			user.phone_number = U2.phone_number;
			
			return user;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new StatusException(500);
		}
	}
	
	public User getSessionUserDataRaw(String token) throws StatusException
	{
		try
		{
			checkValidToken(Manager.toJson("token", token));
			
			BasicNameValuePair[] pairs = new BasicNameValuePair[] {new BasicNameValuePair("token", token)};
			
			User user = gson.fromJson(httpClient.makeGetRequest("/session_data", pairs).rawBody, User.class);
			User U2 = gson.fromJson(httpClient.makeGetRequest("/user_data", pairs).rawBody, User.class);
			
			user.username = U2.username;
			user.email = U2.email;
			user.full_name = U2.full_name;
			user.phone_number = U2.phone_number;
			
			return user;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new StatusException(500);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HttpResponseData registerUser(String jsonBody) throws StatusException
	{
		try
		{
			User user = gson.fromJson(jsonBody, User.class);
			
			if(user.username == null || user.password == null)
				throw new StatusException(400, "Missing important request data");
			
			LinkedList<BasicNameValuePair> body = new LinkedList<>();
			body.add(new BasicNameValuePair("username",user.username));
			body.add(new BasicNameValuePair("password",user.password));
			body.add(new BasicNameValuePair("email",user.email));
			body.add(new BasicNameValuePair("full_name",user.full_name));
			body.add(new BasicNameValuePair("phone_number",user.phone_number));
			
			if(user.user_group_name != null)
			{
				body.add(new BasicNameValuePair("dev_key","3fc9b689459d738f8c88a3a48aa9e33542016b7a4052e001aaa536fca74813cb"));
				body.add(new BasicNameValuePair("website_name","reGood"));
				body.add(new BasicNameValuePair("user_group_name",user.user_group_name));
			}
			
			HttpResponseData hrd = httpClient.makePostRequest("/register_user", null, Manager.toJson(body.toArray(new BasicNameValuePair[0])));
			
			User userT = gson.fromJson(hrd.rawBody, User.class);
			
			if(userT.user_id <= 0)
				throw new StatusException(hrd.statusCode, hrd.rawBody);
			
			if(hrd.statusCode == 201)
			{
				sql.executeUpdate(String.format("call %s.`INPUT: Register user`(%s, '%s', '%s', '%s');", sql.schema, userT.user_id, user.city, user.state, user.zip));
			}
			
			return hrd;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new StatusException(500);
		}
	}
	
	public HttpResponseData login(String jsonBody) throws StatusException
	{
		try
		{
			User user = gson.fromJson(jsonBody, User.class);
			
			BasicNameValuePair[] body = new BasicNameValuePair[] 
					{new BasicNameValuePair("username",user.username), 
					new BasicNameValuePair("password",user.password), 
					new BasicNameValuePair("ipv4",this.AUTHWEBSITE)};
			
			return httpClient.makePostRequest("/login", null, Manager.toJson(body));
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new StatusException(500);
		}
	}
	
//	public void registerUser(String jsonBody) throws StatusException
//	{
//		User user = gson.fromJson(jsonBody, User.class);
//		
//		if(user == null || user.username == null || user.password == null)
//		{
//			throw new StatusException(400, "bad body package", gson);
//		}
//		
//		user.password = DigestUtils.sha256Hex(user.password);
//		
//		int effectRow = sql.executeUpdate(String.format("INSERT INTO %s.users (username, password, email, full_name, phone_number) VALUES ('%s', '%s', '%s', '%s', '%s');", sql.schema, user.username, user.password, user.email, user.full_name, user.phone_number));
//		
//		if(effectRow <= 0)
//		{
//			throw new StatusException(202, gson);
//		}
//	}
//	
//	public User getUser(int id)
//	{
//		User newUser = null;
//		try(ResultSet rs = sql.executeQuery(String.format("SELECT * FROM %s.users where users.id = '%s';", sql.schema, id)))
//		{
//			while(rs.next())
//				newUser = new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"), rs.getString("email"), rs.getString("create_time"), rs.getString("full_name"), rs.getString("phone_number"));
//
//			rs.close();
//		}
//		catch(Exception ex)
//		{
//			log(ex.getMessage());
//		}
//		
//		sql.closeConnection();
//		return newUser;
//		
//	}
//	
//	/**
//	 * this method will execute querry to find out full user information
//	 * @param JsonBody
//	 * @return
//	 * @throws StatusException
//	 */
//	public User getUser(String JsonBody) throws StatusException
//	{
//		User user = gson.fromJson(JsonBody, User.class);
//		User newUser = null;
//		
//		if(user.id > 0)
//		{
//			newUser = getUser(user.id);
//			
//			if(newUser != null)
//				return newUser;
//		}
//		
//		if(user.username.compareTo("") == 0 || user.username == null || user.password.compareTo("") == 0 || user.password == null)
//			throw new StatusException(400, "invalid username or password", gson);
//		
//		user.password = DigestUtils.sha256Hex(user.password);
//		
//		try(ResultSet rs = sql.executeQuery(String.format("SELECT * FROM %s.users where users.username = '%s' and users.password = '%s';", sql.schema, user.username, user.password)))
//		{
//			while(rs.next())
//			{
//				User temp = new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"), rs.getString("email"), rs.getString("create_time"), rs.getString("full_name"), rs.getString("phone_number"));
//				if(temp.username.compareTo(user.username) == 0)
//				{
//					newUser = temp;
//					break;
//				}
//			}
//			
//			rs.close();
//		}
//		catch(Exception ex)
//		{
//			log(ex.getMessage());
//		}
//		
//		if(newUser == null)
//			throw new StatusException(403, "can't find user");
//		
//		sql.closeConnection();
//		return newUser;
//	}
//	
//	public String login(String jsonBody) throws StatusException
//	{
//		String token = "";
//		
//		User user = getUser(jsonBody);
//		Website website = getWebsite(jsonBody);
//		UserPrivilege userPrivilege = getUserPrivilege(user.id, website.id);
//		if(userPrivilege == null)
//			userPrivilege = createPrivilege(user.id, website.id);
//		
////		if(user == null || user.username == null || user.password == null || website == null || website.name == null || website.name.compareTo("") == 0)
////			throw new StatusException(400, "Error: body package", gson);
//		
//		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
//		LocalDateTime now = LocalDateTime.now();
//		
//		token = DigestUtils.sha256Hex(dtf.format(now).toString() + user.username);
//		
//		int effectRow = sql.executeUpdate(String.format("INSERT INTO %s.session (token, session_data) VALUES ('%s', '%s');", sql.schema, token, gson.toJson(userPrivilege).toString()));
//		
//		if(effectRow <= 0)
//			throw new StatusException(500, "something when wrong with database");
//		
//		return token;
//	}
//	
//	public void logout(String jsonBody) throws StatusException
//	{
//		SessionData sessionData = gson.fromJson(jsonBody, SessionData.class);
//		
//		if(sessionData.token != null)
//		{
//			int effectRow = sql.executeUpdate(String.format("DELETE FROM %s.session WHERE (token = '%s');", sql.schema, sessionData.token));
//			if(effectRow <= 0)
//				throw new StatusException(500, "something when wrong with database");
//		}
//	}
	
}
