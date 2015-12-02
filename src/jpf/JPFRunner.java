package jpf;

import java.util.concurrent.ExecutorService;

import kodkod.KodkodRunner;
import model.LinkedQueue;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFConfigException;
import gov.nasa.jpf.JPFException;



public class JPFRunner implements Runnable //Threads will be added in next part.
{
	
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
	 * Reference to KodkodPool.
	 */
	static ExecutorService kodkodPool;
	
	public JPFRunner(ExecutorService kPool, LinkedQueue<Integer> lQ, boolean correctModelMode ) {
		kodkodPool = kPool;
		lQueue = lQ;
		_correctModelMode = correctModelMode;
	}

	@Override
    public void run()
    {
		//Get the current project's classpath to add it later within the JPF config object...
		String _currentClasspath = System.getProperty("java.class.path");
		
		try 
		{
			
			/*
			 * Create the JPF Config object.
			 */
			String args[] = new String[0];
			Config conf = JPF.createConfig(args);
					      
			/*
			 * Set the properties required by JPF.
			 */
			conf.setProperty("site","${jpf-core}../site.properties");
			conf.setProperty("classpath","${jpf-core}/build/main;${jpf-core}/build/examples;${jpf-core}/build/examples;"+_currentClasspath);
			
						
			/*
			 * The class under test within current java project.
			 */
			conf.setProperty("target", "jpf.TestClass");
			
			/*
			 * Instantiate the JPF.
			 */
			JPF jpf = new JPF(conf);
			
			/*
			 * Add listener to the class under test.
			 */
			
			if(_correctModelMode)
			{
				TestClassListenerCorrectModel tListener = new TestClassListenerCorrectModel(kodkodPool, lQueue);//Pass references...
				jpf.addVMListener(tListener);
			}
			else
			{
				TestClassListenerWrongModel tListener = new TestClassListenerWrongModel(kodkodPool, lQueue);//Pass references...
				jpf.addVMListener(tListener);
			}

			/*
			 * Run the JPF.
			 */
	
			jpf.run();		      
			
			/*
			 * Process property violations discovered by JPF, if any.
			 */
			if (jpf.foundErrors())
			{
				// Process the property violations discovered by JPF..
			}	
		
		} 
		catch (JPFConfigException cx)
		{
		  // ... handle configuration exception
		  // ...  can happen before running JPF and indicates inconsistent configuration data
		} 
		catch (JPFException jx)
		{
		  // ... handle exception while executing JPF, can be further differentiated into
		  // ...  JPFListenerException - occurred from within configured listener
		  // ...  JPFNativePeerException - occurred from within MJI method/native peer
		  // ...  all others indicate JPF internal errors
	    }
  }
   



}