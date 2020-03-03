package main;

public class NodeRegular extends Node
{
	//declaracion id reg
	
	private static final String REGULARID = "DELETE";
	
	//declaracion de caracter regular en secuencia
	
	@SuppressWarnings("unused")
	private char character; 	
	
	/**
     *  constructor default
     */
	public NodeRegular(char ch) 
	{
		super();
		id = REGULARID;
		character = ch;
	}
	
}
