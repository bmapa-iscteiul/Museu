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
	private ShareResourceRegisto shareResourceReg;
	
	
	public MedicaoThread(ShareResourceMedicoes shareresource, ShareResourceRegisto shareResourceReg) {
		loadIni();
		this.shareResource=shareresource;
		this.shareResourceReg=shareResourceReg;
	}
	
	public DBObject getLastMeasurement() throws InterruptedException {
		this.lastMessage=shareResource.getLastMedicao(lastMessage);
		return lastMessage;
	}
	
	public void setMedicaoToShareResource(MedicaoSensor medicao) {
		shareResourceReg.setMedicao(medicao);
	}
	
	public void setAlertaToshareReource(Alerta alerta) {
		shareResourceReg.setAlerta(alerta);
	}
	
	public List<Double> getMeasurements(){
		return this.values;
	}
	
	public void addValue(Double value) {
		this.values.add(value);
	}
	
	public MedicaoSensor dbObjectToMedicao(DBObject ob) {
		double valor = Double.parseDouble(ob.get(this.getName()).toString());
		String date = ob.get("dat").toString();
		String time = ob.get("tim").toString();
		String datetime= getDataHora(date, time);
		return new MedicaoSensor(valor, getName(), datetime);
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
	
	public String joinDateWithTime(String date, String time) {
	//	try {
			//DBObject lastMeasurement = getLastMeasurement();
		//	String dat = lastMeasurement.get("dat").toString();
		//	String tim = lastMeasurement.get("tim").toString();
			date = date.replace('/', '-');
			return date + " " + time;
	//	} catch (InterruptedException e) {
			// TODO Auto-generated catch block
	//		e.printStackTrace();
	//	}
	//	return null;
	}
	
	public String getDataHora(String date, String time) {
		String dataHora = joinDateWithTime(date,time);
		dataHora = changeDateFormat(dataHora);
		return dataHora;
	}
}
