package view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import controller.Manager;
import model.ExceptionManager;
import model.HttpResponseData;
import model.StatusException;
import model.User;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.net.ftp.FTP;

/**
 * Servlet implementation class main
 */
@WebServlet("/rest/*")
public class main extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	private static final String TOKENHEADER = "access_token";
    Manager manager;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public main() {
        super();
        manager = new Manager();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		setAccessControlHeaders(response);
		PrintWriter pw = response.getWriter();
		String pathInfo = request.getPathInfo();
		String body = getBody(request);
		response.setContentType("application/json");
		
		try
		{
			if(pathInfo.contains("/item_size"))
			{
//				checkValidToken(request);
				
				int size = manager.getPostedItemSize();
				
				pw.append(Manager.toJson("posted_item_size", size));
				
				return;
			}
			
			if(pathInfo.contains("/item_url"))
			{
				checkValidToken(request);
				
				Object temp = tryGetParameter(request, "item_id");
				int item_id = temp == null ? -1 : Integer.parseInt(temp.toString());
				
				if(item_id > 0)
				{
					pw.append(manager.getItemURL(item_id));
					return;
				}
					
				throw new StatusException(400, "invalid/missing packet information");
			}
			
			if(pathInfo.contains("/item"))
			{
				checkValidToken(request);
				
				Object temp = tryGetParameter(request, "item_id");
				int item_id = temp == null ? -1 : Integer.parseInt(temp.toString());
				
				temp = tryGetParameter(request, "row_index");
				int rowIndex = temp == null ? -1 : Integer.parseInt(temp.toString());
				
				temp = tryGetParameter(request, "range");
				int range = temp == null ? -1 : Integer.parseInt(temp.toString());
				
				if(item_id > 0)
				{
					pw.append(manager.getItem(item_id));
					return;
				}
				else if(rowIndex > 0 && range > 0)
				{
					pw.append(manager.getItemFromRange(rowIndex, (rowIndex + range)));
					return;
				}
				else
					throw new StatusException(400, "invalid/missing packet information");
			}
			
			if(pathInfo.contains("/user_item"))
			{
				String token = checkValidToken(request);
				User user = manager.getSessionUserDataRaw(token);
				int user_id = user.user_id;
				
				if(user_id > 0)
				{
					pw.append(manager.getAllUserPostedItem(user_id));
					return;
				}
				
				throw new StatusException(500);
			}
			
			if(pathInfo.compareTo("/user_data") == 0)
			{
				String token = checkValidToken(request);
				User user = manager.getSessionUserDataRaw(token);
				pw.append(manager.gson.toJson(user));
				return;
			}
			
			if(pathInfo.compareTo("/test") == 0)
			{
				checkValidToken(request);
				pw.append(Manager.toJson("test", "pass"));
				return;
			}
			
			
			throw new StatusException(404);
		}
		catch(StatusException ex)
		{
			statusHandler(ex, response);
		}
		catch(Exception ex)
		{
			response.setStatus(500);
			ex.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		setAccessControlHeaders(response);
		PrintWriter pw = response.getWriter();
		String pathInfo = request.getPathInfo();
//		String body = getBody(request);
		response.setContentType("application/json");
		
		try
		{
			if(pathInfo.compareTo("/request_item") == 0)
			{
				String token = checkValidToken(request);
				int user_id = manager.getSessionUserDataRaw(token).user_id;
				
				String result = manager.requestItem(user_id, getBody(request));
				response.setStatus(201);
				pw.append(result);
				
				return;
			}
			
			if(pathInfo.compareTo("/item") == 0)
			{
				String token = checkValidToken(request);
				int user_id = manager.getSessionUserDataRaw(token).user_id;
				String result = manager.postItem(getBody(request), user_id);
				response.setStatus(201);
				pw.append(result);
				return;
			}
			
			if(pathInfo.compareTo("/file") == 0)
			{
				checkValidToken(request);
				Object temp = this.tryGetParameter(request, "item_id");
				int item_id = Integer.parseInt(temp == null ? "0" : temp.toString());
				String[] urls = manager.storeFile(request, item_id);
				pw.append(manager.gson.toJson(urls));
				response.setStatus(201);
				return;
			}
			
			
			if(pathInfo.compareTo("/test") == 0)
			{
				pw.append(Manager.toJson("test", "pass"));
				return;
			}
			
			if(pathInfo.compareTo("/register_user") == 0)
			{
				HttpResponseData hrd = manager.registerUser(getBody(request));
				if(hrd.rawBody != null)
					pw.append(hrd.rawBody);
				response.setStatus(hrd.statusCode);
				return;
			}
			
			if(pathInfo.compareTo("/login") == 0)
			{
				HttpResponseData hrd = manager.login(getBody(request));
				pw.append(hrd.rawBody);
				response.setStatus(hrd.statusCode);
				return;
			}
			
			throw new StatusException(404);
		}
		catch(StatusException ex)
		{
			statusHandler(ex, response);
		}
		catch(Exception ex)
		{
			response.setStatus(500);
			ex.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		setAccessControlHeaders(response);
		PrintWriter pw = response.getWriter();
		String pathInfo = request.getPathInfo();
		String jsonBody = getBody(request);
		response.setContentType("application/json");
		
		try
		{
			if(pathInfo.compareTo("/sql") == 0)
			{
				manager.setSQL(jsonBody);
				pw.append(manager.checkServerStatus());
				response.setStatus(201);
				return;
			}
			
			if(pathInfo.compareTo("/ftp") == 0)
			{
				manager.setFTPClient(jsonBody);
				response.setStatus(201);
				return;
			}
			
			if(pathInfo.compareTo("/item") == 0)
			{
				checkValidToken(request);
				pw.append(manager.updateItem(jsonBody));
				response.setStatus(201);
				return;
			}
			
			if(pathInfo.compareTo("/test") == 0)
			{
				pw.append(Manager.toJson("test", "pass"));
				return;
			}
			
			
			throw new StatusException(404);
		}
		catch(StatusException ex)
		{
			statusHandler(ex, response);
		}
		catch(Exception ex)
		{
			response.setStatus(500);
			ex.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		setAccessControlHeaders(response);
		PrintWriter pw = response.getWriter();
		String pathInfo = request.getPathInfo();
		String body = getBody(request);
		response.setContentType("application/json");
		
		try
		{
			if(pathInfo.compareTo("/item") == 0)
			{
				checkValidToken(request);
				
				Object temp = tryGetParameter(request, "item_id");
				int item_id = temp == null ? -1 : Integer.parseInt(temp.toString());
				
				if(item_id > 0)
				{
					manager.deleteItem(item_id);
					return;
				}
				
				throw new StatusException(400, "missing/invalid query data");
			}
			
			if(pathInfo.compareTo("/test") == 0)
			{
				pw.append(Manager.toJson("test", "pass"));
				return;
			}
			
			throw new StatusException(404);
		}
		catch(StatusException ex)
		{
			statusHandler(ex, response);
		}
		catch(Exception ex)
		{
			response.setStatus(500);
			ex.printStackTrace();
		}
	}
	
	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		setAccessControlHeaders(response);
		PrintWriter pw = response.getWriter();
		String pathInfo = request.getPathInfo();
		String body = getBody(request);
		response.setContentType("application/json");
		
		try
		{
			
			if(pathInfo.compareTo("/test") == 0)
			{
				pw.append(Manager.toJson("test", "pass"));
				return;
			}
			
			throw new StatusException(404);
		}
		catch(StatusException ex)
		{
			statusHandler(ex, response);
		}
		catch(Exception ex)
		{
			response.setStatus(500);
			ex.printStackTrace();
		}
	}
	
	private String getBody(HttpServletRequest request) throws IOException
	{
		StringBuilder sb = new StringBuilder();
	    BufferedReader reader = request.getReader();
	    try 
	    {
	        String line;
	        while ((line = reader.readLine()) != null) 
	        {
	            sb.append(line).append('\n');
//	            this.wait(100);
	        }
	        
	        
	    }
	    catch(Exception ex)
	    {
	    	ex.printStackTrace();
	    }
	    finally 
	    {
	        reader.close();
	    }
	    
	    return sb.toString();
	}
	
	
	private void setAccessControlHeaders(HttpServletResponse resp) 
	{
		resp.addHeader("Access-Control-Allow-Origin", "*");
		resp.addHeader("Access-Control-Allow-Headers", "Content-Type,Authorization,Accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers,Content-Length,Connection");
		resp.addHeader("Access-Control-Expose-Headers", "Authorization,Access-Control-Allow-Origin,Access-Control-Allow-Credentials,Content-Type,Content-Length,Content-Encoding,Connection");
		resp.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");

		
		
//		resp.addHeader("Access-Control-Allow-Headers", "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
//		resp.addHeader("Access-Control-Max-Age", "1728000");
//		resp.setContentType("application/json");
	}
	
	private void statusHandler(Exception exception, HttpServletResponse res)
	{
		try 
		{
			ExceptionManager exceptionManager = manager.gson.fromJson(exception.getMessage(), ExceptionManager.class);
			
			if(exceptionManager == null || exceptionManager.code < 0)
			{
				res.sendError(500);
				return;
			}
			
			
			if(exceptionManager.message.compareTo("") != 0)
				res.sendError(exceptionManager.code, exceptionManager.message);
			else
				res.sendError(exceptionManager.code);
			
			
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String tryGetTokenFromHeader(HttpServletRequest request) throws StatusException
	{
		try
		{
			String token = request.getHeader(TOKENHEADER);
			
			if(token == null || token.compareTo("") == 0)
				throw new StatusException(401, "invalid or missing token");
			
			return token;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		throw new StatusException(401, "invalid or missing token");
	}
	
	private String checkValidToken(HttpServletRequest request) throws StatusException
	{
		String token = tryGetTokenFromHeader(request);
		if(!manager.isValidToken(token))
			throw new StatusException(401, "invalid or missing token");
		
		return token;
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
}
