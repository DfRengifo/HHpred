package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Blast implements Runnable
{
	//declarar ULRS
	
	private static final String URL = "https://blast.ncbi.nlm.nih.gov/Blast.cgi";	
	private static final String PARAMETERS_INTER_GET = "?CMD=Get&RID=";		
	private static final String PARAMETERS_GET = "?CMD=Get&RESULTS_FILE=on&FORMAT_TYPE=CSV&FORMAT_OBJECT=Alignment&DESCRIPTIONS=100&ALIGNMENT_VIEW=Tabular&RID=";
    private static final String PARAMETERS_POST = "?CMD=Put&PROGRAM=blastp&DATABASE=nr&RUN_PSIBLAST=on&EXPECT=***&QUERY=";
    
    //declarar estados
	
    private static final String STARTING = "starting";
	private static final String WAITING = "waiting";
	private static final String FAILURE = "failure";
	private static final String SUCCESS = "success";
	private static final String RETRIEVING = "retrieving";
	
	//declarar variables
    
    private static String id;    
    private static String status;    
    private static String[] parameters;
    
    /**
     *  constructor default
     *
     *  @arg String[] args => parametros para el blast
     */
	public Blast(String[] args) 
	{
		parameters = args; 
		status = STARTING;
	}

	/**
     *  run: ejecuta el proceso blast     
     */
	public void run() 
	{	
		System.out.println("STARTING BLAST");
		
		try 
		{
			sendPOST(parameters[0], parameters[1]);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}	
		
		if (status.equals(SUCCESS))
		{
			status = WAITING;
			
			while (status.equals(WAITING))
			{
				System.out.println("BLAST WAITING FOR: "+ id);
				
				try 
				{
					sendInterGET();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
				
				if (status.equals(WAITING))
				{
					try 
					{
						Thread.sleep(30000);
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
				}	
			}
			
			System.out.println("BLAST RETRIEVING: "+id);
			
			try 
			{
				sendGET();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			
			if (status.equals(SUCCESS))
			{
				System.out.println("PSI-BLAST DONE");
			}
			else
			{
				System.out.println("PSI-BLAST FAILED");
			}
		}
		else
		{
			System.exit(0); 
		}		
	}	
	
	/**
     *  envia un POST request al servidor blast
     *  recibe y analiza validez de la respuesta
     *
     *  @arg String expect => e-value para el blast
     *  @arg String params => secuencia de aminoacidos a enviar
     */
	private static void sendPOST(String expect, String params) throws IOException 
	{
		//define the url
		
		String postUrl = (URL+PARAMETERS_POST+params).replace("***", expect);
		
		// open conection
		
		URL obj = new URL(postUrl);
		System.out.println(postUrl);
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
	
	/**
     *  envia un GET request al servidor blast
     *  recibe y procesa el estado de compeltitud de el previo POST       
     */
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
				
				status = WAITING; 
			}
			
		} 
		else 
		{
			//FAILURE
			
			status = FAILURE; 
		}
	}
	
	/**
     *  envia un GET request al servidor blast
     *  recibe los resultados del proceso blast
     *  escribe los resultados en el aarchivo Blast.csv
     */
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
