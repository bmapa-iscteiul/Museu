package com.ctm;

import java.util.List;

import com.mongodb.DBObject;

public class ThreadTemperatura extends MedicaoThread {
	
	public boolean running = true;
		
	public ThreadTemperatura(ShareResourceMedicoes shareresource,ShareResourceRegisto shareResourceReg) {
		super(shareresource, shareResourceReg);
		setName("tmp");
	}
	
	public void run() {
		while(running) {
			try {
				DBObject next = getLastMeasurement();
				MedicaoSensor medicao = dbObjectToMedicao(next);
				addValue(medicao.getValorMedicao());
				Alerta alerta = checkForAlert(medicao);
				if(podeEnviarAlerta() && alerta != null)
					setAlertaToshareReource(alerta);
				setMedicaoToShareResource(medicao);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public Alerta checkForAlert(MedicaoSensor medicao) {
		List<Double> medicoes = getMeasurements();
		if(medicoes.size() < 3) {
			return null;
		}
		double limite_temperatura = Double.parseDouble(MainMongoToMySql.getMysqlProperty("limitetemperatura"));
		double lastMedicao = medicoes.get(medicoes.size()-1);
		if(allAboveZona2Seguranca() && risingTemperature() && oneAboveZona1Seguranca()) {
			//String dataHora, String tipoSensor, double valor, int controlo, double limit, String descricao
		//	String datahora = getDataHora();
			String tipoSensor = "tmp";
			double valor = lastMedicao;
			int controlo = 0;
			double limite = limite_temperatura;
			String descricao = "Alerta de inc�ndio!";
			Alerta alerta = new Alerta(medicao.getDataHoraMedicao(), tipoSensor, valor, controlo, limite, descricao);
			System.out.println("Foi criado um alerta!");
			//ENVIAR ALERTA PARA LISTA DE ALERTAS.
			return alerta;
		}
		return null;
	}
	
	public boolean allAboveZona2Seguranca() {
		double limite_temperatura = Double.parseDouble(MainMongoToMySql.getMysqlProperty("limitetemperatura"));
		double zona_2_seguranca = Double.parseDouble(MainMongoToMySql.getMysqlProperty("zona2seguranca"));
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
		double limite_temperatura = Double.parseDouble(MainMongoToMySql.getMysqlProperty("limitetemperatura"));
		double zona_1_seguranca = Double.parseDouble(MainMongoToMySql.getMysqlProperty("zona1seguranca"));
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
