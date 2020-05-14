package com.ctm;

import java.util.ArrayList;
import java.util.List;

public class ShareResourceRegisto {
	private List<MedicaoSensor> medicoes = new ArrayList<MedicaoSensor>();
	private List<Alerta> alertas = new ArrayList<Alerta>();
	private SendToMysql tomysql;
	
	public synchronized void setMedicao(MedicaoSensor medicao) {
		medicoes.add(medicao);	
	}
	
	
	public synchronized void setAlerta(Alerta alerta) {
		alertas.add(alerta);
		tomysql.interrupt();
	}
	
	public synchronized List<MedicaoSensor> getMedicoes(){
		List<MedicaoSensor> result= new ArrayList<MedicaoSensor>(medicoes);
		medicoes.clear();
		return result;
	}
	
	public synchronized List<Alerta> getAlertas(){
		List<Alerta> result= new ArrayList<Alerta>(alertas);
		alertas.clear();
		return result;
	}
	
	public void setSendTomysql(SendToMysql toMysql) {
		this.tomysql=toMysql;
	}
	
	





}
