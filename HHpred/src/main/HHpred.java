package main;

public class HHpred 
{   
	private static final String STARTING = "starting";	
	
    private static String id;    
    private static String status;

	public static void main(String[] args) throws InterruptedException 
	{
		// PARAMETRIZAR 
		
		id = "df.rengifo";
		status = STARTING;
		
		// COMENZAR EL PROCESO BLAST
		
		Thread psiBlast = new Thread(new Blast(args), "psiBlast");		
		psiBlast.start();
		
		//COMENZAR EL PROCESO PSIPRED
		
		Thread psipred = new Thread(new Psipred(), "psipred");		
		psipred.start();
		
		//Esperar a terminacion
		
		psiBlast.join();
		psipred.join();
	}	
}
