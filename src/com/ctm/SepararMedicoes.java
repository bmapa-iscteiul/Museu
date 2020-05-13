package com.ctm;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class SepararMedicoes extends Thread {
	static DBCollection mongocol;
	public boolean running = true;
	
	public SepararMedicoes(DBCollection mongocol) {
		this.mongocol = mongocol;
	}

	public void run() {
		while(running) {
			try {
				sleep(2000);
				//Coloca um ponteiro no primeiro objeto resultante do find()
				DBCursor cursor = mongocol.find();
				System.out.println(mongocol.count());
			    //while(cursor.hasNext()) {
			       //DBObject obj = cursor.next();
			       //System.out.println(obj);
			    //}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
