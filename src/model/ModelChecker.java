package model;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jpf.JPFRunner;
import kodkod.KodkodRunner;

public class ModelChecker {
	
	/*
	 * Reference to model to be checked.
	 */
	static LinkedQueue<Integer> lQueue;
	
	/*
	 * Flag to determine if do we need to create wrong manipulated model of Kodkod for
	 * negative test case.
	 */
	static boolean _correctModelMode;

	
	/*
	 * Create executor thread pools for executing the tasks of running JPF and Kodkod
	 */
	private static final ExecutorService _JPFPool = Executors.newSingleThreadExecutor();
	private static final ExecutorService _KodKodPool = Executors.newSingleThreadExecutor();


	public static void main(String args[])
	{
		//Get the command-line argument to determine the type of Kodkod model to be created...
		
		String strCorrectModelMode = args[0];		
		System.out.println("strCorrectModelMode : "+strCorrectModelMode);
		_correctModelMode = Boolean.parseBoolean(strCorrectModelMode);
		
		//Create initial model...
		createLinkedQueue();
		
				
		//Create an instance of JPFRunner...		
		JPFRunner jpfRun = new JPFRunner(_KodKodPool, lQueue, _correctModelMode);
		
		
		//Execute the JPF Runner thread...
		_JPFPool.execute(jpfRun);
		
		
		//_KodKodPool.shutdown();
		//_JPFPool.shutdown();
	}
	
	   
	/*
	 * Method to create an instance of linked-queue.
	 */
	private static void createLinkedQueue()
	{
		/*
		 * Build a queue of linked-list.
		 */
		lQueue = new LinkedQueue<Integer>();//Queue for 'Integer' elements..

		//Add the test data to queue
		int max = 60;
		for(int i = 10; i < max; i+=10)
			lQueue.put(i);		
  }
}
