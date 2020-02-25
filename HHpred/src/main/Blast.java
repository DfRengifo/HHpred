package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Blast 
{
	private static final String URL = "https://blast.ncbi.nlm.nih.gov/Blast.cgi";	
	private static final String PARAMETERS_INTER_GET = "?CMD=Get&RID=";		
	private static final String PARAMETERS_GET = "?RESULTS_FILE=on&FORMAT_TYPE=CSV&FORMAT_OBJECT=Alignment&DESCRIPTIONS=100&ALIGNMENT_VIEW=Tabular&CMD=Get&RID=";	
    private static final String PARAMETERS_POST = "?CMD=Put&PROGRAM=blastp&PAGE_TYPE=BlastSearch&LINK_LOC=blasthome&DATABASE=nr&RUN_PSIBLAST=on&EXPECT=***&QUERY=";
    
    //declarar estados
	
	private static final String WAITING = "waiting";
	private static final String FAILURE = "failure";
	private static final String SUCCESS = "success";
	private static final String RETRIEVING = "retrieving";
    
    private static String id;    
    private static String status;

	public static void main(String[] args) throws IOException, InterruptedException 
	{	
		System.out.println("Starting BLAST");
		sendPOST(args[0], args[1]);	
		
		if (status.equals(SUCCESS))
		{
			status = WAITING;
			
			while (status.equals(WAITING))
			{
				System.out.println("WAITING FOR: "+ id);
				
				sendInterGET();
				
				if (status.equals(WAITING))
				{
					Thread.sleep(30000);
				}	
			}
			
			System.out.println("RETRIEVING: "+id);
			
			sendGET();
			
			if (status.equals(SUCCESS))
			{
				System.out.println("PSI-BLAST Done");
			}
			else
			{
				System.out.println("PSI-BLAST Failed");
			}
		}
		else
		{
			System.exit(0); 
		}		
	}
	
	private static void sendPOST(String expect, String params) throws IOException 
	{
		//define the url
		
		String postUrl = (URL+PARAMETERS_POST+params).replace("***", expect);
		System.out.println("Post URL: "+postUrl);
		
		// open conection
		
		URL obj = new URL(postUrl);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");

		// post file
		
		con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		os.write(params.getBytes());
		os.flush();
		os.close();
		
		//repsonse

		int responseCode = con.getResponseCode();

		if (responseCode == HttpURLConnection.HTTP_OK) 
		{ 
			//SUCCESS
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			
			//compile answer

			while ((inputLine = in.readLine()) != null) 
			{
				response.append(inputLine);
			}
			in.close();
			
			String tempResponse = response.toString();
			
			//analize response
			
			if (tempResponse.contains("<td>Request ID</td>"))
			{
				// SUCCESS
				
				int inic = tempResponse.indexOf("<td>Request ID</td>");
				int fini = tempResponse.indexOf("<tr class=\"odd\"><td>Status</td><td>Searching</td></tr>                                <tr><td>Time since submission</td>");
				
				String resp = tempResponse.substring(inic+27, fini-30);
				id = resp;
				status = SUCCESS;
			}
			else
			{
				// FAILURE
				
				status = FAILURE; 
			}
			
		} 
		else 
		{
			// FAILURE
			
			status = FAILURE; 
		}
	}
	
	private static void sendInterGET() throws IOException 
	{
		// define url
		
		String getUrl = URL+PARAMETERS_INTER_GET+id;
		URL obj = new URL(getUrl);
		
		//define conection
		
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		
		//response
		
		int responseCode = con.getResponseCode();
		
		if (responseCode == HttpURLConnection.HTTP_OK) 
		{ 
			// SUCCESS
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			
			//compile answer

			while ((inputLine = in.readLine()) != null) 
			{
					response.append(inputLine);
			}
			in.close();
			
			//process answer
			
			if (response.toString().contains("Download All"))
			{
				//SUCCESS
				
				status = RETRIEVING;
			}
			else
			{
				//FAILURE
				
				status = FAILURE; 
			}
			
		} 
		else 
		{
			//FAILURE
			
			status = FAILURE; 
		}
	}
	
	private static void sendGET() throws IOException 
	{
		//define url
		
		String getUrl = URL+PARAMETERS_GET+id;
		
		//define conection
		
		URL obj = new URL(getUrl);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		
		//response
		
		int responseCode = con.getResponseCode();
		
		if (responseCode == HttpURLConnection.HTTP_OK) 
		{ 
			//SUCCESS
			
			OutputStream os = null;
			os = new FileOutputStream(new File("data/Blast.csv"));
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			
			//write file

			while ((inputLine = in.readLine()) != null) 
			{
				String proc = inputLine.replace(",", ";")+"\n";
				os.write(proc.getBytes(), 0, proc.length());
			}
			in.close();	
			os.close();
			
			status = SUCCESS; 			
		} 
		else 
		{
			//FAILURE
			
			status = FAILURE; 
		}
	}
	
}
