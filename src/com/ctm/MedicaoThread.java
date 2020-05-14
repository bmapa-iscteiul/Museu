package com.ctm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.mongodb.DBObject;


public abstract class MedicaoThread extends Thread {
	private List<Double> values= new ArrayList<Double>();
	private ShareResourceMedicoes shareResource;
	private DBObject lastMessage;
	static Properties MongoToMysqlIni = new Properties();
	
	
	public MedicaoThread(ShareResourceMedicoes shareresource) {
		loadIni();
		this.shareResource=shareresource;
	}
	
	public DBObject getLastMeasurement() throws InterruptedException {
		this.lastMessage=shareResource.getLastMedicao(lastMessage);
		return lastMessage;
	}
	
	public List<Double> getMeasurements(){
		return this.values;
	}
	
	public void addValue(Double value) {
		this.values.add(value);
	}
	
	public static void loadIni() {
        try {
			MongoToMysqlIni.load(new FileInputStream("MongoToMysql.ini"));
		} catch (FileNotFoundException  e2) {
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	public String getProperty(String key) {
		return MongoToMysqlIni.getProperty(key);
	}
	
	public String changeDateFormat(String date) {
		try {
      	  date = date.replace('/', '-');
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			date = sdf2.format(sdf.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		return date;
	}
	
	public String joinDateWithTime() {
		try {
			DBObject lastMeasurement = getLastMeasurement();
			String dat = lastMeasurement.get("dat").toString();
			String tim = lastMeasurement.get("tim").toString();
			dat = dat.replace('/', '-');
			return dat + " " + tim;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public String getDataHora() {
		String dataHora = joinDateWithTime();
		dataHora = changeDateFormat(dataHora);
		return dataHora;
	}
}
