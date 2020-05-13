package com.ctm;

import com.mongodb.DBObject;


public class Temperatura extends Measurement {
		
	public Temperatura(ShareResourceMongoToVerificationThread shareresource) {
		super(shareresource);
		setName("tmp");
	}
	
	public void run() {
		try {
			DBObject next = getNextMeasurement();
			System.out.println(getName()+"->"+next.get(getName()).toString());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
