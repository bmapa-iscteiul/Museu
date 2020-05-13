package com.ctm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class SepararMedicoesThread extends Thread {
	private Properties initParametersMongo;
	private com.ctm.ShareResourceMedicoes shareResource;
	private static final int sleepTime=1700;
	
	private MongoClient mongoClient;
	private DB clientDB;
	private DBCollection  sensorCollection;
	
	public SepararMedicoesThread(ShareResourceMedicoes sh) {
		try {
			this.shareResource=sh;
			initParametersMongo = new Properties();
			initParametersMongo.load(new FileInputStream("CloudToMongo.ini"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void connectToMongo() {
        String mongo_host = initParametersMongo.getProperty("mongo_host");
        String mongo_database = initParametersMongo.getProperty("mongo_database");
        String mongo_collection = initParametersMongo.getProperty("mongo_collection_sensor");
		mongoClient = new MongoClient(new MongoClientURI(mongo_host));
		clientDB =    (mongoClient).getDB(mongo_database);
       sensorCollection =  (DBCollection) clientDB.getCollection(mongo_collection);
	}
	
	public void run() {
		connectToMongo();
		while(true) {
			try {
				sleep(sleepTime);
				DBCursor medicoes = sensorCollection.find();
				if(medicoes.size()!=0) {
				for(DBObject medicao:medicoes)
					shareResource.addMedicao(medicao);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}
	}
	
	
	
	public static void main(String[] args) {
		ShareResourceMedicoes share = new ShareResourceMedicoes();
		SepararMedicoesThread link = new SepararMedicoesThread(share);
		MedicaoThread tmp = new Temperatura(share);
		link.start();
		tmp.start();
	}
	
	
	

}
