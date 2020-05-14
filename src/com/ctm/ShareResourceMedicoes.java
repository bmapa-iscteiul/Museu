package com.ctm;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.DBObject;

public class ShareResourceMedicoes {
	private List<DBObject> medicoes = new ArrayList<DBObject>();
	private int lastObjectIndex=-1;
	private static final int max= 5;
	
	public synchronized void addMedicao(DBObject medicao) {
		if(lastObjectIndex+1 >= max) {
			removeLastMedicao();	
		}
		medicoes.add(medicao);
		this.lastObjectIndex=medicoes.size()-1;
		notifyAll();
	}
	
	public synchronized DBObject getLastMedicao(DBObject medicao) throws InterruptedException {
		while( lastObjectIndex == -1 || medicoes.get(lastObjectIndex).equals(medicao)) {
			wait();
		}
		return medicoes.get(lastObjectIndex);
	}
	
	private void removeLastMedicao() {
		medicoes.remove(0);
		lastObjectIndex=medicoes.size()-1;
	}
	
}
