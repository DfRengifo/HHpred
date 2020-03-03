package main;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Psipred implements Runnable
{
	//declarar urls
	
	private static final String URL = "http://bioinf.cs.ucl.ac.uk/psipred/api/submission/";	
	private static final String GET_URL = "http://bioinf.cs.ucl.ac.uk/psipred/api/submissions/";
	
	//declarar estados
	
	private static final String STARTING = "starting";
	private static final String WAITING = "waiting";
	private static final String FAILURE = "failure";
	private static final String SUCCESS = "success";
	private static final String RETRIEVING = "retrieving";
	
	//declarar variables
    
    private static String id;
    private static String name; 
    private static String status;
    private static String sequence;
    
    /**
     *  constructor default
     */
    public Psipred(String seq) 
	{
		status = STARTING;
		sequence = seq;
	}

    /**
     *  run: ejecuta el proceso psipred     
     */
	public void run()
	{	
		System.out.println("STARTING PSIPRED");
		try 
		{
			sendPOST(sequence);
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
				System.out.println("PSIPRED WAITING FOR: "+ id);
				try 
				{
					sendGET();
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
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
			
			System.out.println("PSIPRED RETRIEVING: "+name);
			
			try 
			{
				sendDefinitiveGET();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			
			if (status.equals(SUCCESS))
			{
				System.out.println("PSIPRED DONE");
			}
			else
			{
				System.out.println("PSIPRED FAILED");
			}		
		}
		else
		{
			System.exit(0);	
		}		
	}

	/**
     *  envia un POST request al servidor psipred
     *  recibe y analiza validez de la respuesta
     */
	private static void sendPOST(String sqnc) throws IOException 
	{		
		//declaracion id	
		
		String uuid =  "*****"+Long.toString(System.currentTimeMillis())+"*****";
		
		//declaracion strings

		String twoHyphens = "--";
		String lineEnd = "\r\n";
		
		//declaracion buffer
		
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1024*1024;
		
		//declaracion file, filename y filepath
		
		String[] q ="data/Proteina.txt".split("/");
		int idx = q.length - 1;		
		
		Files.write(Paths.get("data/Proteina.txt"), sqnc.getBytes());
		
		File file = new File("data/Proteina.txt");
		FileInputStream fileInputStream = new FileInputStream(file);
		
		//comenzar conexion
		
		URL url = new URL(URL);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		//parametrizacion conexion
		
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		
		//parametrizacion post
		
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary="+uuid);
		
		// parametrizacion file
		
		DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
		outputStream.writeBytes(twoHyphens + uuid + lineEnd);
		outputStream.writeBytes("Content-Disposition: form-data; name=\"" + "input_data" + "\"; filename=\"" + q[idx] +"\"" + lineEnd);
		outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
		outputStream.writeBytes(lineEnd);
		
		//conversion  y escritura file
		
		bytesAvailable = fileInputStream.available();
		bufferSize = Math.min(bytesAvailable, maxBufferSize);
		buffer = new byte[bufferSize];
		
		bytesRead = fileInputStream.read(buffer, 0, bufferSize);
		while(bytesRead > 0)
		{
			outputStream.write(buffer, 0, bufferSize);
			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
		}
		
		outputStream.writeBytes(lineEnd);	
		
		//escritura data post

		String[] posts = {"job=psipred","submission_name=uniandes","email=df.rengifo@uniandes.edu.co"};
		int max = posts.length;
		for(int i=0; i<max;i++) 
		{
			outputStream.writeBytes(twoHyphens + uuid + lineEnd);
			String[] kv = posts[i].split("=");
			outputStream.writeBytes("Content-Disposition: form-data; name=\"" + kv[0] + "\"" + lineEnd);
			outputStream.writeBytes("Content-Type: text/plain"+lineEnd);
			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(kv[1]);
			outputStream.writeBytes(lineEnd);
		}
		
		outputStream.writeBytes(twoHyphens + uuid + twoHyphens + lineEnd);
		
		//esperar response
		
		InputStreamReader isr = new InputStreamReader(connection.getInputStream());
		int responseCode = connection.getResponseCode();
		
		if (responseCode == HttpURLConnection.HTTP_CREATED) 
		{ 
			//RESPONSE OK
			
			status = SUCCESS; 
			
			//leer respuesta
			
			BufferedReader in = new BufferedReader(isr);
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) 
			{
				response.append(inputLine);
			}
			in.close();
			
			String tempResponse = response.toString();
			
			//extraer id
			
			int start = tempResponse.indexOf("UUID");
			int end = tempResponse.indexOf("uniandes");
			
			id = tempResponse.substring(start+18, end-46);
			
		} 
		else 
		{
			//RESPONSE N-OK
			
			status = FAILURE;
		}
		
		//Closing
		
		fileInputStream.close();
		isr.close();
		outputStream.flush();
		outputStream.close();
	}
		
	/**
     *  envia un GET request al servidor psipred
     *  recibe y procesa el estado de compeltitud de el previo POST
     *  al estar completado, actualiza el id con el nuevo valor        
     */
	private static void sendGET() throws IOException 
	{
		//definicion de url
		
		String getUrl = URL+id;
		URL obj = new URL(getUrl);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
		//paramtrizacion GET
		
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		int responseCode = con.getResponseCode();
		
		//response
		
		if (responseCode == HttpURLConnection.HTTP_OK) 
		{
			// SUCCESS
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			
			//compile response

			while ((inputLine = in.readLine()) != null) 
			{
				response.append(inputLine);
			}
			in.close();
			
			//anlizar response
			
			String tempResponse = response.toString();
			
			if (tempResponse.contains(";state&quot;: &quot;Complete&quot;"))
			{
				//SUCCESS
				int start = tempResponse.indexOf("job_name&quot;: &quot;psipred&quot;,            &quot;UUID&quot;");
				int end = tempResponse.indexOf("&quot;state&quot;: &quot;Complete&quot;,            &quot;last_message&quot;: &quot;Completed job at step");
				tempResponse = tempResponse.substring(start+72, end-19);
				name = tempResponse;
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
     *  envia un GET request al servidor psipred
     *  recibe los resultados del proceso psipred
     *  escribe los resultados en el archivo Secondary.txt
     */
	private static void sendDefinitiveGET() throws IOException 
	{		
		// declaracion url
		
		String getUrl = GET_URL+name+".horiz";
		
		//parametrizacion de conexion
		
		URL obj = new URL(getUrl);
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
			
			//define file
			
			OutputStream os = null;
			os = new FileOutputStream(new File("data/EstructuraSecundaria.txt"));
			
			//compile response

			while ((inputLine = in.readLine()) != null) 
			{
				inputLine = inputLine+"\n";
				os.write(inputLine.getBytes(), 0, inputLine.length());
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
