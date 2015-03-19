package nl.tudelft.distributed.afekgafni.process;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import nl.tudelft.distributed.afekgafni.message.AckMessage;
import nl.tudelft.distributed.afekgafni.message.CandidateMessage;

public class Candidate extends AbstractProcess<AckMessage> {
	
	private static final long serialVersionUID = 7632450835597789028L;
	private String[] remotes;

	public Candidate(int nodeId, String[] remotes) throws RemoteException {
		super(nodeId);
		this.remotes = remotes;
	}
	
	private List<AckMessage> acknowledgements = new ArrayList<AckMessage>();
	

	public void startElection() {
		// Having a linked list is easier later on
		LinkedList<String> remotesCopy = new LinkedList<>();
		Collections.addAll(remotesCopy, remotes);

		int level = -1;
		int subsetSize = 0;

		while (true) {
			level += 1;
			if (level % 2 == 0) {
				if (remotesCopy.size() == 0) {
					// elected
					elected();
					return;
				}
				subsetSize = (int) Math.min(Math.pow(2, level / 2), remotesCopy.size());

				for (int i = subsetSize; i > 0; i--) {
					String first = remotesCopy.pop();
					try {
						Object o = Naming.lookup(first);
						// should be true...
						if (o instanceof Ordinary) {
							Ordinary that = (Ordinary) o;
							that.receive(new CandidateMessage(level, nodeId, getRemote(nodeId)));
						}
					} catch (NotBoundException | MalformedURLException | RemoteException e) {
						e.printStackTrace();
						return;
					}
				}
			} else {
				synchronized(acknowledgements){
					if(acknowledgements.size() < subsetSize){
						notElected();
						return;
					}
				}
			}
		}
	}
	
	@Override
	public void receive(AckMessage msg) {
		synchronized(acknowledgements){
			acknowledgements.add(msg);
		}
	}

	/**
	 * Called when elected
	 */
	public void elected() {
		System.out.println("ME SO HAPPY; I AM ALLOWED TO DO STUFF!");
	}
	
	public void notElected(){
		System.out.println("Nope, I suck");
	}
}