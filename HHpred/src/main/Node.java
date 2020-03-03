package main;

import java.util.ArrayList;

public class Node 
{
	//declarar atributos no especificos
	
	protected ArrayList<Node> sons;
	protected String id; 
	
	//declarar tipod de nodos
	
	private NodeDelete deletion; 
	private NodeInsert insertion;
	private NodeRegular regular;
	private Node end; 
	
	/**
     *  añadir un nodo del
     */
	public void addDeletion (NodeDelete del)
	{
		deletion = del; 
		sons.add(del); 
	}
	
	/**
     *  añadir un nodo ins
     */
	public void addInsertion (NodeInsert ins)
	{
		insertion = ins; 
		sons.add(ins); 
	}
	
	/**
     *  añadir un nodo reg
     */
	public void addRegular (NodeRegular reg)
	{
		regular = reg; 
		sons.add(reg); 
	}
	
	/**
     *  añadir un nodo end
     */
	public void addEnd (Node end)
	{
		this.end = end; 
		sons.add(end); 
	}
	
	/**
     *  añadir un nodo generico
     */	
	public void addNode(Node n)
	{
		sons.add(n); 
	}
	
	/**
     *  constructor default
     */
	public Node ()
	{
		sons = new ArrayList<Node>();

		deletion = null; 
		insertion = null;
		regular = null; 
	}
	
	/**
     *  @ret lista generica de nodods
     */
	public ArrayList<Node> getNodes ()
	{
		return sons;
	}
	
	/**
     *  @ret nodo reg asociado
     */
	public NodeRegular getRegular ()
	{
		return regular; 		
	}
	
	/**
     *  @ret nodo ins asociado
     */
	public NodeInsert getInsert ()
	{
		return insertion; 		
	}
	
	/**
     *  @ret nodo del asociado
     */	
	public NodeDelete getDelete ()
	{
		return deletion; 		
	}
	
	/**
     *  @ret nodo end asociado
     */
	public Node getEnd ()
	{
		return end; 		
	}
}
