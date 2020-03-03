package main;

public class HMM implements Runnable
{	
	//declarar estados
	
	private static final String STARTING = "starting";
	private static final String SUCCESS = "success";
	
	//declarar variables
    
    private static String status;
    private static String sequence;
    private static Node start;
    private static Node end;     
    
    /**
     *  constructor default
     */
    public HMM(String seq) 
	{
		status = STARTING;
		start = new Node(); 
		end = new Node(); 
		sequence = seq;
	}

    /**
     *  run: ejecuta la construccion del HMM    
     */
	public void run()
	{	
		System.out.println("HHM: STARTING CONSTRUCTION");
		
		char[] arr = sequence.toCharArray();	
		
		//Crear el primer nivel del HMM 
		//	*	creacion nodos del ins y reg
		//	*	creacion conexiones reg->reg reg->ins y reg->del		
		
		start.addRegular(new NodeRegular(arr[0]));
		start.addInsertion(new NodeInsert());
		start.addDeletion(new NodeDelete());
	
		start.getRegular().addDeletion(start.getDelete());
		start.getRegular().addInsertion(start.getInsert());
		start.getRegular().addRegular(start.getRegular());
		
		//Instanciar e inicializar los indices de nivel
		
		NodeRegular tempReg = start.getRegular();
		NodeInsert tempIns = start.getInsert();
		NodeDelete tempDel = start.getDelete();
		
		System.out.println("HHM: CREATING STEPS");
		
		//crear los niveles intermedios del HMM, saltando el primero (ya creado) y 
		//el ultimo (por crear) al ser excepciones
		
		for(int i = 1; i<arr.length-1; i++)
		{
			// crear los nuevos nodos reg del e ins
			// conectarlos con el anterior nodo ins
			
			tempIns.addRegular(new NodeRegular(arr[i]));		
			tempIns.addDeletion(new NodeDelete());
			tempIns.addInsertion(new NodeInsert());
			
			//conectar nodos con el anterior nodo del
			
			tempDel.addRegular(tempIns.getRegular());
			tempDel.addDeletion(tempIns.getDelete());
			tempDel.addInsertion(tempIns.getInsert());
			
			//instanciar y conectar el nuevo nodo reg con los nuevos nodos ins y del
			
			tempReg = tempDel.getRegular(); 
			
			tempReg.addRegular(tempIns.getRegular());
			tempReg.addDeletion(tempIns.getDelete());
			tempReg.addInsertion(tempIns.getInsert());
			
			//instanciar indices ind y del
			
			tempIns = tempReg.getInsert();
			tempDel = tempReg.getDelete();
		}
		
		System.out.println("HMM: ENDING CONSTRUCTION");
		
		//Construccion de nivel final de HMM 
		//crear nuevo nodo reg y conectarlo con anteriores nodos ins y del
		
		tempIns.addRegular(new NodeRegular(arr[arr.length-1]));
		tempDel.addRegular(tempIns.getRegular());
		
		//instanciar indice reg y conectar a nuevo nodo reg
		
		tempReg = tempIns.getRegular(); 		
		tempReg.addRegular(tempDel.getRegular());
		
		//conectar HMM a nodo final
		
		tempReg.addEnd(end);
		tempIns.addEnd(end);
		tempDel.addEnd(end);
		
		//SUCCESS		
		
		status = SUCCESS;		
		System.out.println("HMM: " + status.toUpperCase());		
	}
	
}
