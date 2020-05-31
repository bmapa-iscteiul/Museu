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
//	private Properties initParametersMongo;
	private com.ctm.ShareResourceMedicoes shareResource;
	private static final int sleepTime=1700;
	
	private MongoClient mongoClient;
	private DB clientDB;
	private DBCollection  sensorCollection;
	
	public SepararMedicoesThread(ShareResourceMedicoes sh) {
			this.shareResource=sh;
	}
	private void connectToMongo() {
        String mongo_host = MainMongoToMySql.getMongoProperty("mongo_host");
        String mongo_database = MainMongoToMySql.getMongoProperty("mongo_database");
        String mongo_collection = MainMongoToMySql.getMongoProperty("mongo_collection_sensor");
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
					shareResource.addMedicao(medicoes.toArray().get(medicoes.size()-1));
				}
				removeCollection();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}
	}
	
	
	private void removeCollection() {
		DBCursor datas = sensorCollection.find();
		while (datas.hasNext()) {
		   sensorCollection.remove(datas.next());
		}	
	}
	
	
	
	
	
	

}
