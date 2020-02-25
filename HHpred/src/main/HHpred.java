package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HHpred 
{
	private static final String USER_AGENT = "Mozilla/5.0";	
	private static final String URL = "https://blast.ncbi.nlm.nih.gov/Blast.cgi";	
	private static final String PARAMETERS_INTER_GET = "?CMD=Get&RID=";		
	private static final String PARAMETERS_GET = "?RESULTS_FILE=on&FORMAT_TYPE=CSV&FORMAT_OBJECT=Alignment&DESCRIPTIONS=100&ALIGNMENT_VIEW=Tabular&CMD=Get&RID=";	
    private static final String PARAMETERS_POST = "?CMD=Put&PROGRAM=blastp&PAGE_TYPE=BlastSearch&LINK_LOC=blasthome&DATABASE=nr&EXPECT=10&QUERY=";
    
    private static String id;    
    private static String wait = "Waiting";

	public static void main(String[] args) throws IOException, InterruptedException 
	{	
		sendPOST(args[1]);		
		while (wait.equals("Waiting"))
		{
			System.out.println("Waiting");
			sendInterGET();
			if (wait.equals("Waiting"))
			{
				Thread.sleep(3000);
			}	
		}
		System.out.println("Retrieving");
		sendGET();
		System.out.println("Blast Done");
	}
	
	private static void sendPOST(String params) throws IOException 
	{
		String postUrl = URL+PARAMETERS_POST+params;
		System.out.println("Post URL: "+postUrl);
		URL obj = new URL(postUrl);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);

		// For POST only - START
		con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		os.write(params.getBytes());
		os.flush();
		os.close();
		// For POST only - END

		int responseCode = con.getResponseCode();
		System.out.println("POST Response Code :: " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK) 
		{ //success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) 
			{
				response.append(inputLine);
			}
			in.close();
			
			String tempResponse = response.toString();
			
			if (tempResponse.contains("<input name=\"RID\" size=\"50\" type=\"text\" value=\""))
			{
				int inic = tempResponse.indexOf("<input name=\"RID\" size=\"50\" type=\"text\" value=\"")+47;
				int fini = tempResponse.indexOf("\" id=\"rid\" />");
				String resp = tempResponse.substring(inic, fini);
				
				// print result
				id = resp;
				System.out.println("ID= "+resp);
			}
			else
			{
				System.out.println("POST request not worked");
			}
			
		} 
		else 
		{
			System.out.println("POST request not worked");
		}
	}
	
	private static void sendInterGET() throws IOException 
	{
		String getUrl = URL+PARAMETERS_INTER_GET+id;
		System.out.println("Inter Url: "+getUrl);
		URL obj = new URL(getUrl);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) 
		{ // success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) 
			{
					response.append(inputLine);
			}
			in.close();
			
			if (response.toString().contains("Download All"))
			{
				wait = "Success";
				System.out.println(wait);
			}
			
		} 
		else 
		{
			wait = "Failure";
			System.out.println("GET request not worked");
		}
	}
	
	@SuppressWarnings("resource")
	private static void sendGET() throws IOException 
	{
		String getUrl = URL+PARAMETERS_GET+id;
		System.out.println("Get Url: "+getUrl);
		URL obj = new URL(getUrl);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) 
		{ // success
			
			OutputStream os = null;
			os = new FileOutputStream(new File("data/Blast.csv"));
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) 
			{
				String proc = inputLine.replace(",", ";")+"\n";
				os.write(proc.getBytes(), 0, proc.length());
			}
			in.close();	
			os.close();
			
			System.out.println("File: Blast.csv");
			
		} 
		else 
		{
			System.out.println("GET request not worked");
		}
	}
	
}
