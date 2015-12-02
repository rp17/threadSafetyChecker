package jpf;

import java.util.concurrent.ExecutorService;

import model.LinkedQueue;
import kodkod.KodkodRunner;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.InstanceInvocation;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class TestClassListenerCorrectModel extends PropertyListenerAdapter {
	
	/*
	 * Reference to model to be checked.
	 */
	static LinkedQueue<Integer> lQueue;
	
	/*
	 * Store next element to be put into the Queue...
	 */
	static int nextElement = 60;
	
	/*
	 * Reference to KodkodPool.
	 */
	static ExecutorService kodkodPool;
	
	public TestClassListenerCorrectModel(ExecutorService kPool, LinkedQueue<Integer> lQ) {
		kodkodPool = kPool;
		lQueue = lQ;
	}

	@Override
	public void instructionExecuted(VM vm, ThreadInfo currentThread,
			Instruction nextInstruction, Instruction executedInstruction) {
		
	
		if (!vm.getSystemState().isIgnored()) {
			Instruction insn = executedInstruction;

			ThreadInfo ti = currentThread;

			//System.out.println("\ninsn==="+insn);
						
			if (insn instanceof JVMInvokeInstruction) {
				JVMInvokeInstruction md = (JVMInvokeInstruction) insn;
				

				MethodInfo mi = md.getInvokedMethod();
				String currMethodName = mi.getFullName();
				
				
				if (currMethodName.contains("updateQueue")) {				
					
					
					System.out.println("\nEncountered the method : "+currMethodName);					

					lQueue.put(nextElement);
					nextElement+=10;

				}
				

				if (currMethodName.contains("checkKodKodModel")) {				
					
					
					System.out.println("\nEncountered the method : "+currMethodName);
					
					LinkedQueue<Integer> lq = lQueue.cloneQueue();
					//Create an instance of KodKodRunner...
					KodkodRunner kodkodRun = new KodkodRunner(lq, true);
					kodkodPool.execute(kodkodRun);

				}
			}
		}
	}
}
