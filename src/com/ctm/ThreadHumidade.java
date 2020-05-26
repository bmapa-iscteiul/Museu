package com.ctm;

import java.util.List;

import com.mongodb.DBObject;

public class ThreadHumidade extends MedicaoThread {

	public ThreadHumidade(ShareResourceMedicoes shareresource,ShareResourceRegisto shareResourceReg) {
		super(shareresource,shareResourceReg);
		setName("hum");
	}
	
	
	public void run() {
		while(isRunning()) {
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
		double limite_temperatura = Double.parseDouble(MainMongoToMySql.getMysqlProperty("LimiteHumidade"));
		double lastMedicao = medicoes.get(medicoes.size()-1);
		if(allAboveZona2Seguranca() && risingHumidity() && oneAboveZona1Seguranca()) {
			String tipoSensor = "hum";
			double valor = lastMedicao;
			int controlo = 0;
			double limite = limite_temperatura;
			String descricao = "Alerta de humidade!";
			Alerta alerta = new Alerta(medicao.getDataHoraMedicao(), tipoSensor, valor, controlo, limite, descricao);
			System.out.println("Foi criado um alerta!");
			return alerta;
		}
		return null;
	}
	
	public boolean allAboveZona2Seguranca() {
		double limite_temperatura = Double.parseDouble(MainMongoToMySql.getMysqlProperty("LimiteHumidade"));
		double zona_2_seguranca = Double.parseDouble(MainMongoToMySql.getMysqlProperty("Zona2Seguranca"));
		double limite2= limite_temperatura - (limite_temperatura * zona_2_seguranca);
		List<Double> medicoes = getMeasurements();
		for(int i = medicoes.size() - 1; i > medicoes.size() - 4; i-- ) {
			if(medicoes.get(i) < limite2) {
				return false;
			}
		}
		return true;
	}
	
	public boolean risingHumidity() {
		List<Double> medicoes = getMeasurements();
		for(int i = medicoes.size() - 1; i > medicoes.size() - 3; i-- ) {
			if(medicoes.get(i) < medicoes.get(i - 1)) {
				return false;
			}
		}
		return true;
	}
	public boolean oneAboveZona1Seguranca() {
		double limite_temperatura = Double.parseDouble(MainMongoToMySql.getMysqlProperty("LimiteHumidade"));
		double zona_1_seguranca = Double.parseDouble(MainMongoToMySql.getMysqlProperty("Zona1Seguranca"));
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
