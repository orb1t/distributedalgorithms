package nl.tudelft.distributed.birmanschiperstephenson;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;

public class DefaultEndpointBuffer extends UnicastRemoteObject implements IEndpointBuffer{
	
	private static final long serialVersionUID = -7906377175640753915L;
	private List<Tuple3<Object, Integer, int[]>> buffer;
	private IEndpoint endpoint;
	
	public DefaultEndpointBuffer(IEndpoint endpoint) throws RemoteException {
		buffer = new LinkedList<Tuple3<Object,Integer, int[]>>();
		this.endpoint = endpoint;
	}
	
	@Override
	public void receive(Object message, int sender, int[] vector) throws RemoteException {
		if(passesCondition(sender, vector)){
			buffer.add(Tuple3.create(message, sender, vector));
			int foundAmount;
			do{
				foundAmount = 0;
				for(int i = 0 ; i < buffer.size() ; i++){
					Tuple3<Object, Integer, int[]> entry = buffer.get(i);
					if(passesCondition(entry._2, entry._3)){
						endpoint.deliver(message);
						endpoint.vectorClock()[sender]++;
						buffer.remove(i);
						foundAmount++;
						i--;
					}
				}
			} while(foundAmount != 0);
		} else{
			buffer.add(Tuple3.create(message, sender, vector));
		}
	}
	
	private boolean passesCondition(int sender, int[] vector){
		int[] localClock = endpoint.vectorClock().clone();
		localClock[sender] += 1;
		for(int i = 0 ; i < localClock.length && i < vector.length ; i++){
			if(localClock[sender] < vector[i]){
				return false;
			}
		}
		return true;
	}
}
