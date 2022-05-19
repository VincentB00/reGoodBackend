package test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.github.javafaker.Faker;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import controller.Manager;
import javafx.util.Pair;
import model.AdditionalData;
import model.HttpResponseData;
import model.Item;
import model.MultipartParser;
import model.SimpleFTPClient;
import model.SimpleHttpClient;

public class test 
{

	public static void main(String[] args) throws IOException, URISyntaxException 
	{
//		String path = "/FrontEndServer/Public/file_manager/reGood";
//		SimpleFTPClient client = new SimpleFTPClient("192.168.1.6", 21, "frontEnd", "123456", "/FrontEndServer/Public/file_manager/reGood");
//		client.connect(false);
//		
////		FTPFile[] files = client.listFile();
////		
////		for(FTPFile file : files)
////		{
////			System.out.println(file.getName());
////		}
//		
//		String test = "test 9 file Update from test.java";
//		
//		boolean confirm = client.storeFile(client.defaultFolderLocation + "/1/2/3", "test.txt", test);
//		
//		System.out.println(confirm);
//		
//		client.disconnect();
		
		
//		String body = "----------------------------329172367042393189908229\n" + 
//				"Content-Disposition: form-data; name=\"item_id\"\n" + 
//				"\n" + 
//				"1\n" + 
//				"----------------------------329172367042393189908229\n" + 
//				"Content-Disposition: form-data; name=\"filename\"\n" + 
//				"\n" + 
//				"something.txt\n" + 
//				"----------------------------329172367042393189908229\n" + 
//				"Content-Disposition: form-data; name=\"file\"; filename=\"test.txt\"\n" + 
//				"Content-Type: text/plain\n" + 
//				"\n" + 
//				"post man upload\n" + 
//				"post man upload2\n" + 
//				"post man upload3\n" + 
//				"----------------------------329172367042393189908229--";
//		
//		MultipartParser mp = new MultipartParser(body);
//		
//		System.out.println(mp.get("file").beginValueIndex);
//		System.out.println(mp.get("file").endValueIndex);
//		
//		String path = "/FrontEndServer/Public/file_manager/reGood";
//		SimpleFTPClient client = new SimpleFTPClient("192.168.1.6", 21, "frontEnd", "123456", "/FrontEndServer/Public/file_manager/reGood");
//		client.connect(false);
//		
//		boolean confirm = client.storeFile(client.defaultFolderPath + "/1", "test.txt", mp.getLines(), mp.get("file").beginValueIndex, mp.get("file").endValueIndex);
//		
//		System.out.println(confirm);
//		
//		client.disconnect();
		
//		Gson gson = new Gson();
//		
//		String url = "vincentprivatenas.mynetgear.com:7070";
//		
//		
//		
//		Pair[] ps = new Pair[] {new Pair("username","hello_world"), new Pair("password","hello_world"), new Pair("ipv4","reGood")};
//		
//		SimpleHttpClient client = new SimpleHttpClient(url);
//		
//		HttpResponseData rd = client.makePostRequest("/Authentication/rest/login", null, Manager.toJson(ps));
//		
//		BasicNameValuePair[] pairs = new BasicNameValuePair[] {new BasicNameValuePair("token", gson.fromJson(rd.rawBody, AdditionalData.class).token)};
//		
//		System.out.println(client.makeGetRequest("/Authentication/rest/status", pairs));
		
//		System.out.println(Manager.toJson(new Pair[] {new Pair("hello","world"), new Pair("ooooooo", "0000000")}));
		
		setUpDefault();

    }
	
	public static void setUpDefault() throws ParseException, URISyntaxException, IOException
	{
		String url = "vincentprivatenas.mynetgear.com:7070/reGood/rest";
//		String url = "localhost:8080/reGood/rest";
		
		SimpleHttpClient client = new SimpleHttpClient(url);
		
		String body = "{\"ip\":\"192.168.1.6\",\"port\":3309,\"username\":\"vbui\",\"password\":\"123456\",\"schema\":\"Regood\"}";
		
		System.out.println(client.makePutRequest("/sql", null, body));
		
		body = "{\"ip\":\"192.168.1.6\",\"port\":21,\"username\":\"frontEnd\",\"password\":\"123456\",\"defaultFolderPath\":\"/FrontEndServer/Public/file_manager/reGood\"}";
		
		System.out.println(client.makePutRequest("/ftp", null, body));
	}
}
