package model;



import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
import java.util.Iterator;

public class LinkedQueue <E>{
 
	Queue<Node<E>> auxQueue = new ConcurrentLinkedQueue<Node<E>>();
	StringBuffer trace = new StringBuffer();
	Hashtable<String, Integer> structMap = new Hashtable();
	
	static Object lock = new String("Dummy_Lock_Object");
	
	private final Node<E> dummy = new Node<E>(null, null);
	private final AtomicReference<Node<E>> head = new AtomicReference<Node<E>>(dummy);
	private final AtomicReference<Node<E>> tail = new AtomicReference<Node<E>>(dummy);
	
    public static class Node <E> {
		final E item;
		final String name;
		public AtomicReference<Node<E>> next;
	
		public String getName(){
			return name;
		}
		public Node(E item, Node<E> next) {
			this.item = item;
			if(item == null){
				name = "dummy";
			}
			else{
				this.name = item.toString();
			}
			
			this.next = new AtomicReference<Node<E>>(next);
		}
	}

	public void clearTrace(){
		trace.delete(0, trace.length());
	}
	public String getTrace(){
		//System.out.println("calling trace");
		return trace.toString();
	}
		
	public LinkedQueue(){
		structMap.put("Head", 1);
		structMap.put("Tail", 0);
		structMap.put("Dummy", 0);
		
		auxQueue.add(dummy);
	}
	
	
	//Add clone method
	//put synchronized block
	public LinkedQueue<E> cloneQueue()
	{
		LinkedQueue<E> clonedQueue = new LinkedQueue<E>();
		
		Iterator<Node<E>> iterNodes = getNodes();		
		while(iterNodes.hasNext()){
			
			LinkedQueue.Node<E> node = (Node<E>) iterNodes.next();
			System.out.println("cloning : "+node.item);

			clonedQueue.put(node.item);
			
		}
		
		return clonedQueue;
	}
	
	// Check structural and data integrity
	public boolean isConsistent(){
		Enumeration names; 
		Integer curSize = 0;
		Node<E> curNode = head.get().next.get();

		try {
				// Display Map contents
//				names = structMap.keys(); 
//				while(names.hasMoreElements()) { 
//					String str = (String) names.nextElement(); 
//					String value = structMap.get(str).toString();
//					System.out.println(str + ": " + value); 
//				} 
//				System.out.println();
				
				// Gather data from actual queue
				while(curNode.next.get() != null){
//					System.out.println("Current item:" + curNode.item);
					// If an item was meant to be recorded but was not
					if (!structMap.containsKey(curNode.item.toString())){
						System.out.println("Fail by missing item:" + curNode.item);
						return false;
					}
					curSize++;
					curNode = curNode.next.get();
				}
				// If the sizes of suitable items are different
				if ((curSize + 1) != (structMap.size() - 3)){
					System.out.println("Fail by Missing data: CurSize" + curSize + " & struct Size: " + structMap.size());
					return false;
				}
				// If tail is not at the last node (as recorded by previous traversal)
				if (structMap.get("Tail") == 0 || structMap.get("Tail") != curNode.item){
					System.out.println("Fail by Misdirected Tail pointer");
					return false;
				}
		return true;

		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public Iterator<Node<E>> getNodes(){
		return auxQueue.iterator();
	}
	public String getHeadName(){
		return head.get().getName();
	}
	
	public Node<E> getHead(){
		return head.get();
	}
	
	public String getTailName(){
		return tail.get().getName();
	}
	
	public Node<E> getTail(){
		return tail.get();
	}
	
	public int size(){
		return auxQueue.size();
	}
	public List<String> getAtoms()
	{
		synchronized(lock)
		{
			int size = size();
			final List<String> atoms = new ArrayList<String>(size + 1);
			Iterator<LinkedQueue.Node<E>> iterNodes = getNodes();
			//getAtoms synchronized lock
			
			while(iterNodes.hasNext()){
				
				LinkedQueue.Node<E> node = iterNodes.next();
				atoms.add(node.getName());
			}
			
			return atoms;
		}
	}
	public boolean put(E item) {
		
		synchronized(lock)
		{
			Node<E> newNode = new Node<E>(item, null);
			
			System.out.println("auxQueue.size() == "+auxQueue.size());
			
			/*if(auxQueue.size() == 1)
			{			
				head.compareAndSet(dummy, newNode);
				tail.compareAndSet(dummy, newNode);
				dummy.next.compareAndSet(null, newNode);
			}*/
			
			auxQueue.add(newNode);
			Integer success = 0;
			
					
			while(true) {
				Node<E> curTail = tail.get();
				Node<E> tailNext = curTail.next.get();
				
				if(curTail == tail.get()) {
					if(tailNext != null) {
						// advance tail
						tail.compareAndSet(curTail, tailNext);
					} else {
						// try inserting new node
						if(curTail.next.compareAndSet(null, newNode)) {
							if(newNode.item instanceof Integer) {
								String tName = Thread.currentThread().getName();
								trace.append("put inserted " + (Integer)newNode.item +
										" by " + tName + "\n");
								success = 1;
								structMap.put(newNode.item.toString(), success);
							}
							// insertion succeeded, try advancing tail
							tail.compareAndSet(curTail, newNode);
							structMap.remove("Tail");
							structMap.put("Tail", (Integer)newNode.item);
	
							return true;
						}
					}
				}
			}
		}
		
	}

}
