package model;

public class FormData 
{
	public String key;
	public String value;
	public String contentType;
	public String fileName;
	public int beginValueIndex;
	public int endValueIndex;
	
	public FormData(String key, String value, String contentType) 
	{
		super();
		this.key = key;
		this.value = value;
		this.contentType = contentType;
	}
	
	public FormData(String key, String value, String contentType, String fileName) 
	{
		super();
		this.key = key;
		this.value = value;
		this.contentType = contentType;
		this.fileName = fileName;
	}
	
	

	public FormData(String key, String value, String contentType, String fileName, int beginIndex, int endIndex) {
		super();
		this.key = key;
		this.value = value;
		this.contentType = contentType;
		this.fileName = fileName;
		this.beginValueIndex = beginIndex;
		this.endValueIndex = endIndex;
	}

	@Override
	public String toString() {
		return "FormData [key=" + key + ", value=" + value + ", contentType=" + contentType + ", fileName=" + fileName
				+ ", beginIndex=" + beginValueIndex + ", endIndex=" + endValueIndex + "]";
	}

	
	
	
}
