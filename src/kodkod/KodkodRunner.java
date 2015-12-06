package kodkod;

import java.util.Iterator;

import kodkod.ast.Formula;
import kodkod.engine.Solution;
import kodkod.engine.Solver;
import kodkod.engine.satlab.SATFactory;
import kodkod.instance.Bounds;
import model.LinkedQueue;

public class KodkodRunner implements Runnable {
	
	/*
	 * Reference to model to be checked.
	 */
	static LinkedQueue<Integer> lQueue;
	
	/*
	 * Flag to determine if do we need to create wrong manipulated model of Kodkod for
	 * negative test case.
	 */
	static boolean _correctModelMode;
	
		
	public KodkodRunner(LinkedQueue<Integer> lQ, boolean correctModelMode) {
		lQueue = lQ;
		_correctModelMode = correctModelMode;
	}

	@Override
	public void run() {
		
		/*
		 * Print the test linked-list.
		 */
		/*System.out.println("\nCurrent linked-queue: ");
		
		Iterator<LinkedQueue.Node<Integer>> iterNodes = lQueue.getNodes();		
		while(iterNodes.hasNext()){
			
			LinkedQueue.Node<Integer> node = iterNodes.next();
			System.out.println(node.getName());
		}*/
		
		/*
		 * Test reachability of linked-list.
		 * 
		 * Every node should be reachable from head by "next" relationship.
		 */
		
		//Instantiate the KodkodMaper class...
		//Make it singleton - kMapper
		KodkodMapper<Integer> kMapper = new KodkodMapper<Integer>(lQueue);
		

		//Create a solver instance
		Solver solver = new Solver();
		solver.options().setSolver(SATFactory.DefaultSAT4J);
		
		
		
		//Build kodkod queue...
		Bounds bQueue;
		
		if(_correctModelMode)
		{
			bQueue = kMapper.buildKodkodQueue();
		}
		else
		{
			bQueue = kMapper.buildWrongKodkodQueue();
		}
		
		//Get the formula to check tail reachability...	
		Formula f = kMapper.getTailReachabilityFormula();			
		
		//Inverse the formula and solve on bounded Kodkod queue...
		Solution sol = solver.solve(f , bQueue);
		
		//Print the solution..
		System.out.println("\nSolution : \n"+sol);		
	}
}
