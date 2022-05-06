package Data;

import java.sql.ResultSet;
import java.util.Random;

import com.google.gson.Gson;

public class Manager 
{
	public SQL sql;
	public Gson gson;
	public Random random;
	
	public Manager()
	{
		gson = new Gson();
		sql = null;
		random = new Random();
	}
	
	public Manager(String ip, int port, String username, String password, String schema)
	{
		sql = new SQL(ip, port, username, password, schema);
		gson = new Gson();
		random = new Random();
	}
	
	public void setSQL(String jsonBody) throws StatusException
	{
		SQLData sqlData = gson.fromJson(jsonBody, Data.SQLData.class);
		
		if(sqlData != null && sqlData.schema != null && sqlData.schema.compareTo("") != 0)
		{
			sql = new SQL(sqlData.ip, sqlData.port, sqlData.username, sqlData.password, sqlData.schema);
		}
		else
			throw new StatusException(400, "Error: body package", gson);
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
	
}
