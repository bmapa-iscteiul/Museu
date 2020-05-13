package com.ctm;

import com.mongodb.DBObject;


public class Temperatura extends MedicaoThread {
	
	public boolean running = true;
		
	public Temperatura(ShareResourceMedicoes shareresource) {
		super(shareresource);
		setName("tmp");
	}
	
	public void run() {
		while(running) {
			try {
				DBObject next = getNextMeasurement();
				System.out.println(getName()+"->"+next.get(getName()).toString());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
