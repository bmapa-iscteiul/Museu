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
	private List<String> errorSensor = new ArrayList<String>();
	private int noValue = 0;
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
		int sleepTime=getRecoveryTime();
		System.out.println(sleepTime);
		new WaitForSendAlert(podeEnviarAlerta, sleepTime).start();
	}
	
/*LISTA VALORES*/
	public List<Double> getMeasurements(){
		return this.values;
	}
	
	public void addValue(Double value) {
		this.values.add(value);
	}
	
/*LISTA ERROS*/
	public List<String> getErrorList(){
		return this.errorSensor;
	}
	
	public int numberOfErrors() {
		int number = 0;
		for(String x: errorSensor) {
			if(x.equals("NA")) {
				number++;
			}
		}
		return number;
	}
	
	public void addErrorList(String s) {
		if(errorSensor.size()>=100) {
			errorSensor.remove(0);
		}
		errorSensor.add(s);
	}
	
	public void cleanErrorList() {
		errorSensor.removeAll(errorSensor);
	}
	
/*NO VALUE SENT FROM SENSOR*/
	public int getNoValue() {
		return this.noValue;
	}
	public void setNoValue(int valor) {
		noValue = valor;
	}
	
/**/
	public MedicaoSensor dbObjectToMedicao(DBObject ob) {
		String valorString = ob.get(this.getName()).toString();
		if(valorString.equals("NA")) {
			addErrorList("NA");
			return null;
		}else if(valorString.equals("")) {
			addErrorList("");
			noValue++;
			return null;
		}
		addErrorList("");
		double valor = Double.parseDouble(ob.get(this.getName()).toString());
		String date = ob.get("dat").toString();
		String time = ob.get("tim").toString();
		return new MedicaoSensor(valor, getName(), date+" "+time);
	}
	
	public int getRecoveryTime() {
		String concatTime = MainMongoToMySql.getMysqlProperty("TempoDeRecuperacao");
		int hours = Integer.parseInt(concatTime.substring(0,concatTime.indexOf(":")));
		int minutes = Integer.parseInt(concatTime.substring(concatTime.indexOf(":")+1, concatTime.indexOf(":")+3));
		int seconds = Integer.parseInt(concatTime.substring(concatTime.indexOf(":")+4));
		int recoveryTime = ((hours*3600)+(minutes*60)+seconds)*1000;
		return recoveryTime;
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
