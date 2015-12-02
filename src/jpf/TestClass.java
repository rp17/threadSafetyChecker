package jpf;


public class TestClass{
	
   public static void main(String[] args){
	   /*
	    * JPF CONSTRAINT: The "test" JPF class cannot implement "Runnable" , 
	    * JPF looks for "main" method by default.
	    * 
	    */

		//TestClass tc = new TestClass();		  
	    //tc.fakeMethod();   
		  
	    //OR : Execute in multiple iterations
		//   for(int i=0; i <2; i++)
	   	//  		tc.fakeMethod();      
		   
	   
	    //OR: Add interleaving of threads

	   	Checker chk1 = new Checker("Checker1");
		Checker chk2 = new Checker("Checker2");
		Checker chk3 = new Checker("Checker3");

		Thread t1 = new Thread(chk1);
		Thread t2 = new Thread(chk2);
		//Thread t3 = new Thread(chk2);
		t1.setName(chk1.getName());		
		t2.setName(chk2.getName());
		//t3.setName(chk3.getName());
		t1.start();
		t2.start();
	//	t3.start();
		try {
			t1.join();
			t2.join();	
		//	t3.join();		

		}catch(Exception ex){
				ex.printStackTrace();
		}
	  
	  }
	
   

	  
	  static class Checker implements Runnable {
			private String name;
			
			public Checker(String name){
				this.name = name;
			}
			
			public String getName() {
				return new String(name);
			}
			
			@Override
			public void run() {
				try {
					for(int i=0; i <1; i++)
					{
						//Dummy method to indicate the listener to update Original Model (LinkedQueue)
						updateQueue();
						
						//Dummy method to indicate listener to rebuild kodkod model and check well-formedness property..
						checkKodKodModel();	
					}
					
				}catch(Exception e){
					throw new RuntimeException(e);
				}
			}
			
			void updateQueue() 
			{
				//This is a dummy method.
			}
			
			void checkKodKodModel() 
			{
				//This is a dummy method.
			}
		}
  
	    /*	
   	  	void fakeMethod() {
 			//This is a dummy method.
 	  	}
	    */
 }
