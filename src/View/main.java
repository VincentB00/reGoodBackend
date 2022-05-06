package View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Data.ExceptionManager;
import Data.Manager;
import Data.StatusException;

/**
 * Servlet implementation class main
 */
@WebServlet("/rest/*")
public class main extends HttpServlet {
	private static final long serialVersionUID = 1L;
    Manager manager;   
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public main() {
        super();
        // TODO Auto-generated constructor stub
        manager = new Manager();
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter pw = response.getWriter();
		String pathInfo = request.getPathInfo();
		String jsonBody = getBody(request);
		response.setContentType("application/json");
		
		try
		{
			if(pathInfo.compareTo("/status") == 0)
			{
				pw.append(manager.checkServerStatus());
				return;
			}
			
		}
		catch(StatusException ex)
		{
			statusHandler(ex, response);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doHead(HttpServletRequest, HttpServletResponse)
	 */
	protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doOptions(HttpServletRequest, HttpServletResponse)
	 */
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	
	//-----------------------------------------------------------------------------------------------------------------------------
	
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
	        }
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
		resp.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
		resp.addHeader("Access-Control-Allow-Headers", "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
		resp.addHeader("Access-Control-Max-Age", "1728000");
		resp.setContentType("application/json");
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
