package main;

public class HHpred 
{   
	//declarar estados
	
	private static final String STARTING = "starting";	
	
	//delcarar variables
	
    private static String id;    
    private static String status;

    /**
     *  @param args[0] valor-e para la busqueda PSI-BLAST
     *  @param args[1] secuencia a analizar
     *  
     *  genera una prediccion de estructura proteica terciaria
     */
	public static void main(String[] args) throws InterruptedException 
	{
		// PARAMETRIZAR 
		
		id = "df.rengifo";
		status = STARTING;
		
		// COMENZAR LA CONSTRUCCION DEL HMM
		
		Thread HMM = new Thread(new HMM(args[1]), "HMM");		
		HMM.start();
		
		// COMENZAR EL PROCESO BLAST
		
		Thread psiBlast = new Thread(new Blast(args), "psiBlast");		
		psiBlast.start();
		
		//COMENZAR EL PROCESO PSIPRED
		
		Thread psipred = new Thread(new Psipred(args[1]), "psipred");		
		psipred.start();
		
		//Esperar a terminacion
		
		psiBlast.join();
		HMM.join();
		psipred.join();		
		
		System.out.println("TRABAJO: "+id+" STATUS: "+status);
	}	
}
