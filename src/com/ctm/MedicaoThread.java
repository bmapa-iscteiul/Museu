package com.ctm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


import com.mongodb.DBObject;

public abstract class MedicaoThread extends Thread {
	private boolean running = true;
	private List<Double> values= new ArrayList<Double>();
	private ShareResourceMedicoes shareResource;
	private DBObject lastMessage;
	private ShareResourceRegisto shareResourceReg;
	private boolean podeEnviarAlerta = true;
	
	
	public MedicaoThread(ShareResourceMedicoes shareresource, ShareResourceRegisto shareResourceReg) {
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
	
	public boolean podeEnviarAlerta() {
		return podeEnviarAlerta;
	}
	
	public void setAlertaToshareReource(Alerta alerta) {
		shareResourceReg.setAlerta(alerta);
		podeEnviarAlerta=false;
		int sleepTime=Integer.parseInt(MainMongoToMySql.getMysqlProperty("TempoDeRecuperacao"));
		System.out.println(sleepTime);
		new WaitForSendAlert(podeEnviarAlerta, sleepTime).start();
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
		return new MedicaoSensor(valor, getName(), date+" "+time);
	}
	
	
	class WaitForSendAlert extends Thread{
		boolean canSendAlert;
		int sleepTime;
		
		public WaitForSendAlert(boolean canSend,int sleepTime) {
			this.sleepTime=sleepTime;
			this.canSendAlert=canSend;
		}
		
		public void run() {
			try {
				sleep(sleepTime);
				this.canSendAlert=true;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	public boolean isRunning() {
		return running;
	}
	
}
