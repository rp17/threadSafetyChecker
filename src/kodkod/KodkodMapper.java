package kodkod;

import kodkod.instance.*;
import kodkod.ast.Formula;
import kodkod.ast.Relation;
import kodkod.ast.Variable;
import kodkod.engine.*;
import kodkod.engine.satlab.SATFactory;
import model.LinkedQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class KodkodMapper<E> {

	private LinkedQueue<E> queue;
	private final Relation Node, next, head, tail;
	
	private Universe u;
	private TupleFactory f;
	private Bounds b;
	
	public KodkodMapper(LinkedQueue<E> queue){
		Node = Relation.unary("Node");
		head = Relation.unary("head");
		tail = Relation.unary("tail");
		next = Relation.binary("next");
		
		this.queue = queue;
	}
	
	public Formula declarations() {
			/* Node */
		final Formula f0 = next.partialFunction(Node, Node);
		return f0;
	}
	
	public final Formula facts() {
		final Variable v = Variable.unary("v");
		//final Variable w = Variable.unary("w");
	   
		
		final Formula f0 = v.in(head.join(next.reflexiveClosure()));
	 //   final Formula f1 = tail.in(head.join(next.closure())).not();
		final Formula f = f0.forAll(v.oneOf(Node));
		
		return f;
		
	}
	
	public final Formula empty() {
		//return declarations();
		return declarations().and(facts());
	}
	
	public final synchronized Bounds buildKodkodQueue(){
		
		int size = queue.size();
		final List<String> atoms = new ArrayList<String>(size + 1);
		Iterator<LinkedQueue.Node<E>> iterNodes = queue.getNodes();
		//getAtoms synchronized lock
		
		while(iterNodes.hasNext()){
			
			LinkedQueue.Node<E> node = iterNodes.next();
			atoms.add(node.getName());
		}
		
		
		/*
		 * PATCH-FIX CODE START - (For Incremental Solver only.)
		 * CREATE UNIVERSE WITH ALL ITEMS ADDED INITIALLY ITESELF.		  
		 */
		
		/*int val = 60;
		for(int i=0; i <200; i++)
		{
			atoms.add(String.valueOf(val));
			val+=10;
			
		}	*/
		/* PATCH-FIX CODE END */
		
		
		atoms.add("null");
		u = new Universe(atoms);
		f = u.factory();
		b = new Bounds(u);
		//final int max = size - 1;
		System.out.println("Head name = " + queue.getHeadName());
		System.out.println("Tail name = " + queue.getTailName());
		
		//Set the lower and upper bounds for the relation...
		b.bound(Node, f.range(f.tuple(queue.getHeadName()), f.tuple( queue.getTailName())));
		b.bound(next, b.upperBound(Node).product(b.upperBound(Node)));
		
		final TupleSet nextTupleSet = f.noneOf(2);
		iterNodes = queue.getNodes();
		final List<LinkedQueue.Node<E>> nodesList = new ArrayList<LinkedQueue.Node<E>>(size);
		while(iterNodes.hasNext()){
			
			LinkedQueue.Node<E> node = iterNodes.next();
			nodesList.add(node);
			System.out.println(node.getName());
		}
		
		
		// bind next relation
		
		for(int i = 0; i < nodesList.size(); i++) {
			if(i == nodesList.size() -1) {
				LinkedQueue.Node<E> node1 = nodesList.get(i);
				
				System.out.println("Tuple "+i+" = ( "+node1.getName() + ", dummy)" );
	
				nextTupleSet.add(f.tuple(node1.getName(), "dummy"));
			}
			else {
				LinkedQueue.Node<E> node1 = nodesList.get(i);				
				
				if(node1 != null)
				{
					if(node1.next != null)
					{
						LinkedQueue.Node<E> nextNode = node1.next.get();
						
						if(nextNode != null)
						{
							String nextNodeName = nextNode.getName();
							
							System.out.println("Tuple "+i+" = ( "+node1.getName() + ", " + nextNodeName + ")" );
							
							nextTupleSet.add(f.tuple(node1.getName(), nextNodeName));
							
						}					
					}
					else
					{
						System.out.println(node1.getName() + ".next == null, No tuple generated!");
					}
				}
				
			}
		}
		
	
		b.boundExactly(next, nextTupleSet);	
		
		// bind head relation
		final TupleSet start = f.noneOf(1);		
		start.add(f.tuple(queue.getHeadName()));										
		b.boundExactly(head , start);										
		
		// bind tail relation
		final TupleSet end = f.noneOf(1);									
		end.add(f.tuple(queue.getTailName()));										
		b.boundExactly(tail , end);	
		
		return b;
	}
	
		
	/*
	 * Build a formula to check the reachability of a linked-list.
	 */
	public Formula getReachabilityFormula()
	{
		final Variable v = Variable.unary("v");
	
		final Formula f0 = v.in(head.join(next.reflexiveClosure()));
		final Formula f = f0.forAll(v.oneOf(Node));
		final Formula f1 = f.not();
		
		return f1;
	}
	
	public Formula getTailReachabilityFormula()
	{
		final Variable v = Variable.unary("v");
	
		final Formula f0 = v.in(head.join(next.reflexiveClosure()));
		final Formula f = f0.forAll(v.oneOf(tail));
		final Formula f1 = f.not();
		
		return f1;
	}
	
	
	public final Bounds updateKodkodQueue(LinkedQueue.Node<E> oldTail, List<E> newNodeData){
		if(b!=null)
		{
			
			/*
			 * The new node data is not being used for now, only it's size is used to 
			 * construct and add the new tuples in the existing model.
			 * We may change it later.			 * 
			 */
			int newDataSize = newNodeData.size();
						
			//Set the lower and upper bounds for the relation...
			b.bound(Node, f.range(f.tuple(queue.getHeadName()), f.tuple( queue.getTailName())));
			b.bound(next, b.upperBound(Node).product(b.upperBound(Node)));
					
			//Get the next tuple set
			TupleSet nextTupleSet = f.noneOf(2);
			
			//Initialize the current node pointer to old tail node...
			LinkedQueue.Node<E> currNode = oldTail;
			System.out.println("\nOld Tail => "+oldTail.getName());

			// bind next relation		
			for(int i = 0; i <= newDataSize; i++) {
				
				LinkedQueue.Node<E> node1;
				
				if(i==0)//Start from the old tail node...
					node1=oldTail;
				else
				{
					currNode = currNode.next.get();
					node1 = currNode;
				}
					
				System.out.println("\nNode1 => "+node1.getName());

				if(i == newDataSize) 
				{
					System.out.println("Tuple "+i+" = ( "+node1.getName() + ", dummy)" );

					nextTupleSet.add(f.tuple(node1.getName(), "dummy"));
					
					System.out.println("This was a last tuple!!!");
				}
				else 
				{
					if(node1 != null)
					{
						if(node1.next != null)
						{
							LinkedQueue.Node<E> nextNode = node1.next.get();
							
							System.out.println("Next Node => "+nextNode.getName());

							if(nextNode != null)
							{
								String nextNodeName = nextNode.getName();
								
								System.out.println("Tuple "+i+" = ( "+node1.getName() + ", " + nextNodeName + ")" );
								
								nextTupleSet.add(f.tuple(node1.getName(), nextNodeName));
							}					
						}
						else
						{
							System.out.println(node1.getName() + ".next == null, No tuple generated!");
						}
					}
				}
			}
			
			b.boundExactly(next, nextTupleSet);	
			
			// bind head relation
			final TupleSet start = f.noneOf(1);		
			start.add(f.tuple(queue.getHeadName()));										
			b.boundExactly(head , start);										
			
			// bind tail relation
			final TupleSet end = f.noneOf(1);									
			end.add(f.tuple(queue.getTailName()));										
			b.boundExactly(tail , end);
		}
		
		//System.out.println("f : "+f.tuple(b.universe()));
		
		return b;
	}
	
	public Bounds getBounds()
	{
		return b;
	}
	
	public TupleFactory getTupleFactory()
	{
		return f;
	}

	public Bounds buildWrongKodkodQueue() 
	{
		//TEST CODE : SATISFIABLE INSTANCE
		queue.getHead().next = null;
		
		return buildKodkodQueue();
	}
	
}
