package model;

import java.util.HashMap;
import java.util.LinkedList;

public class MultipartParser 
{
	private HashMap<String, FormData> multipartMap;
	private int startIndex;
	String[] blobLines;
	
	public MultipartParser(String body)
	{
		multipartMap = new HashMap<String, FormData>();
		parse(body);
	}
	
	public void parse(String body)
	{
		multipartMap.clear();
		startIndex = 0;
		
		blobLines = body.split("\n");
		boolean endOfBlob = false;
		
		while(!endOfBlob)
		{
			String key = null;
			String value = "";
			String contentType = null;
			String fileName = null;
			int beginIndex = -1;
			int endIndex = -1;
			
			for(int count = startIndex; count < blobLines.length; count++)
			{
				if(blobLines[count].contains("Content-Disposition: form-data;"))
				{
					if(key != null)
					{
						endIndex = count - 2;
						startIndex = count;
						break;
					}
					
					if(blobLines[count].contains("; filename=\""))
					{
						fileName = blobLines[count].substring(blobLines[count].indexOf("; filename=\"") + 2);
						fileName = fileName.substring(fileName.indexOf("\"") + 1);
						fileName = fileName.substring(0, fileName.indexOf("\""));
						
						key = blobLines[count].substring(blobLines[count].indexOf("name"), blobLines[count].indexOf("filename"));
						key = key.substring(key.indexOf("\"") + 1);
						key = key.substring(0, key.indexOf("\""));
					}
					else
					{
						key = blobLines[count].substring(blobLines[count].indexOf("name"));
						key = key.substring(key.indexOf("\"") + 1);
						key = key.substring(0, key.indexOf("\""));
					}
				}
				else if(blobLines[count].contains("Content-Type:"))
				{
					contentType = blobLines[count].substring(blobLines[count].indexOf("Content-Type:") + "Content-Type:".length() + 1).trim();
				}
				else if(beginIndex <= -1 && (blobLines[count].trim().compareTo("") == 0 || blobLines[count].trim().compareTo("\r") == 0))
				{
					beginIndex = count + 1;
				}
				else if(count >= blobLines.length - 1)
				{
					endIndex = blobLines.length - 2;
					startIndex = count;
					endOfBlob = true;
				}
				
			}
			
			FormData fd = new FormData(key, value, contentType, fileName, beginIndex , endIndex);
//			System.out.println(fd);
			
			multipartMap.put(key, fd);
		}
		
		
		
		
		
		
		
		
		//second option
//		LinkedList<String[]> blobs = new LinkedList<>();
//		
//		String[] blob = findNextBlob(lines);
//		blobs.add(blob);
//		
//		while(startIndex >= 0)
//		{
//			if(startIndex >= lines.length - 1)
//				break;
//			blob = findNextBlob(lines);
//			blobs.add(blob);
//		}
//		
//		for(String[] b : blobs)
//		{
//			parseBlob(b);
//		}
	}
	
	private void parseBlob(String[] lines)
	{
		String key = null;
		String value = "";
		String contentType = null;
		String fileName = null;
		int start = 0;
		
//		System.out.println("Here?");
		
		//header
		for(int count = 0; count < lines.length; count++)
		{
			
			
			if(lines[count].contains("Content-Disposition: form-data;"))
			{
				if(lines[count].contains("; filename=\""))
				{
					fileName = lines[count].substring(lines[count].indexOf("; filename=\"") + 2);
					key = lines[count].substring(lines[count].indexOf("name"), lines[count].indexOf("filename"));
				}
				else
				{
					key = lines[count].substring(lines[count].indexOf("name"));
				}
			}
			else if(lines[count].contains("Content-Type:"))
			{
				contentType = lines[count].substring(lines[count].indexOf("Content-Type:") + "Content-Type:".length() + 1).trim();
			}
			else
			{
				value = lines[count + 1];
				start = count + 2;
				break;
			}
		}
		
		
		//value
		for(int count = start; count < lines.length; count++)
		{
			value += "\n" + lines[count];
		}
		
		//filter
		key = key.substring(key.indexOf("\"") + 1);
		key = key.substring(0, key.indexOf("\""));
		
		if(fileName != null)
		{
			fileName = fileName.substring(fileName.indexOf("\"") + 1);
			fileName = fileName.substring(0, fileName.indexOf("\""));
		}
		
		FormData fd = new FormData(key, value, contentType, fileName);
		multipartMap.put(key, fd);
		
//		System.out.println(fd);
	}
	
	private String[] findNextBlob(String[] lines)
	{
		boolean begin = false;
		LinkedList<String> blob = new LinkedList<>();
		for(int count = startIndex; count < lines.length; count++)
		{
			String line = lines[count];
			startIndex = count;
			
			if(line.contains("Content-Disposition: form-data;"))
			{
				if(!begin)
					begin = true;
				else
					break;
				
			}
			
			if(begin)
			{
				blob.add(lines[count]);
			}
		}
		
		String[] result = new String[blob.size() - 1];
		
		for(int count = 0; count < blob.size() - 1; count++)
		{
			result[count] = blob.get(count);
		}
		
		return result;
	}
	
	public FormData get(String key)
	{
		return multipartMap.get(key);
	}
	
	public String getValue(String key)
	{
		FormData fd = multipartMap.get(key);
		String result = blobLines[fd.beginValueIndex];
		
		if(fd.beginValueIndex == fd.endValueIndex)
		{
			return result;
		}
		else
		{	
			for(int count = fd.beginValueIndex + 1; count < fd.endValueIndex + 1; count++)
			{
				result += "\n" + blobLines[count];
			}
			
			return result;
		}
	}
	
	public String[] getLines()
	{
		return this.blobLines;
	}
}

