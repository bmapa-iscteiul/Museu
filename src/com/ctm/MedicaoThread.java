package com.ctm;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.DBObject;


public abstract class MedicaoThread extends Thread {
	private List<Double> values= new ArrayList<Double>();
	private ShareResourceMedicoes shareResource;
	private DBObject lastMessage;
	
	
	public MedicaoThread(ShareResourceMedicoes shareresource) {
		this.shareResource=shareresource;
	}
	
	public DBObject getNextMeasurement() throws InterruptedException {
		this.lastMessage=shareResource.getLastMedicao(lastMessage);
		return lastMessage;
	}
	
	public List<Double> getMeasurements(){
		return this.values;
	}
	
	public void addValue(Double value) {
		this.values.add(value);
	}
	
	
	
	
	
	
	
	

}
