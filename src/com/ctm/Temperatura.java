package com.ctm;



import java.util.List;

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
				DBObject next = getLastMeasurement();
				double valorDaUltimaMedicao = Double.parseDouble(next.get(this.getName()).toString());
				addValue(valorDaUltimaMedicao);
				checkForAlert();
				System.out.println(getName()+"->"+next.get("tmp").toString());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public void checkForAlert() {
		List<Double> medicoes = getMeasurements();
		if(medicoes.size() < 3) {
			return;
		}
		double limite_temperatura = Double.parseDouble(getProperty("limitetemperatura"));
		double lastMedicao = medicoes.get(medicoes.size()-1);
		if(allAboveZona2Seguranca() && risingTemperature() && oneAboveZona1Seguranca()) {
			//String dataHora, String tipoSensor, double valor, int controlo, double limit, String descricao
			String datahora = getDataHora();
			String tipoSensor = "tmp";
			double valor = lastMedicao;
			int controlo = 0;
			double limite = limite_temperatura;
			String descricao = "Alerta de incêndio!  Ultima Temperatura: " + valor + " Limite: " + limite_temperatura ;
			Alerta alerta = new Alerta(datahora, tipoSensor, valor, controlo, limite, descricao);
			System.out.println("Foi criado um alerta!");
			//ENVIAR ALERTA PARA LISTA DE ALERTAS.
		}
		
	}
	
	public boolean allAboveZona2Seguranca() {
		double limite_temperatura = Double.parseDouble(getProperty("limitetemperatura"));
		double zona_2_seguranca = Double.parseDouble(getProperty("zona2seguranca"));
		double limite2= limite_temperatura - (limite_temperatura * zona_2_seguranca);
		List<Double> medicoes = getMeasurements();
		for(int i = medicoes.size() - 1; i > medicoes.size() - 4; i-- ) {
			if(medicoes.get(i) < limite2) {
				return false;
			}
		}
		return true;
	}
	
	public boolean risingTemperature() {
		List<Double> medicoes = getMeasurements();
		for(int i = medicoes.size() - 1; i > medicoes.size() - 3; i-- ) {
			if(medicoes.get(i) < medicoes.get(i - 1)) {
				return false;
			}
		}
		return true;
	}
	public boolean oneAboveZona1Seguranca() {
		double limite_temperatura = Double.parseDouble(getProperty("limitetemperatura"));
		double zona_1_seguranca = Double.parseDouble(getProperty("zona1seguranca"));
		double limite1= limite_temperatura - (limite_temperatura * zona_1_seguranca);
		List<Double> medicoes = getMeasurements();
		for(int i = medicoes.size() - 1; i > medicoes.size() - 4; i-- ) {
			if(medicoes.get(i) > limite1) {
				return true;
			}
		}
		return false;
	}

}
