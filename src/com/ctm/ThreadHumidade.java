package com.ctm;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.mongodb.DBObject;

public class ThreadHumidade extends MedicaoThread {
	
	int sensorMaxSemValor = Integer.parseInt(MainMongoToMySql.getMysqlProperty("SensorMaxSemValor"));
	int sensorMaxDadosInvalidos = Integer.parseInt(MainMongoToMySql.getMysqlProperty("SensorMaxDadosInvalidos"));
	double limiteHumidade = Double.parseDouble(MainMongoToMySql.getMysqlProperty("LimiteHumidade"));
	

	public ThreadHumidade(ShareResourceMedicoes shareresource,ShareResourceRegisto shareResourceReg) {
		super(shareresource,shareResourceReg);
		setName("hum");
	}
	
	
	public void run() {
		while(isRunning()) {
			try {
				DBObject next = getLastMeasurement();
				if(!dbObjectToMedicao(next).equals(null)) {
					MedicaoSensor medicao = dbObjectToMedicao(next);
					addValue(medicao.getValorMedicao());
					Alerta alerta = checkForAlert(medicao);
					if(podeEnviarAlerta() && alerta != null)
						setAlertaToShareResource(alerta);
					setMedicaoToShareResource(medicao);
				} else {
					Alerta alerta = checkForSensorAlert();
					if(podeEnviarAlerta() && alerta != null)
						setAlertaToShareResource(alerta);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public Alerta checkForSensorAlert() {
		if(getNoValue()==sensorMaxSemValor) {
			Alerta alerta = makeAlerta("Sensor temperatura em baixo!", null, null);
			setNoValue(0);
			return alerta;
		}else if(numberOfErrors()==sensorMaxDadosInvalidos) {
			Alerta alerta = makeAlerta("Sensor temperatura com problemas!", null, null);
			cleanErrorList();
			return alerta;
		}
		return null;
	}
	
	public Alerta checkForAlert(MedicaoSensor medicao) {
		List<Double> medicoes = getMeasurements();
		if(medicoes.size() < 3) {
			return null;
		}
		double lastMedicao = medicoes.get(medicoes.size()-1);
		if(allAboveZona2Seguranca() && oneAboveZona1Seguranca()) {
			Alerta alerta = makeAlerta("Humidade elevada!", medicao, medicoes);
			return alerta;
		}else if(allAboveZona1Seguranca() && oneAboveLimite()) {
			Alerta alerta = makeAlerta("Humidade ultrapassou o limite !", medicao, medicoes);
			return alerta;
		}
		return null;
	}
	
	public boolean oneAboveLimite() {
		double limite_humidade = Double.parseDouble(MainMongoToMySql.getMysqlProperty("LimiteHumidade"));
		List<Double> medicoes = getMeasurements();
		for(int i = medicoes.size() - 1; i > medicoes.size() - 4; i-- ) {
			if(medicoes.get(i) > limite_humidade) {
				return true;
			}
		}
		return false;
	}

/*FUNCAO GENERICA PARA CRIACAO ALERTA*/
	public Alerta makeAlerta(String descricao, MedicaoSensor medicao, List<Double> medicoes) {
		String tipoSensor = "hum";
		int controlo = 0;
		double limite = limiteHumidade;
		String dataHora;
		String valor;

		if(descricao.equals("Humidade elevada!")) {
			valor = medicoes.get(medicoes.size()-1).toString()+"; "+medicoes.get(medicoes.size()-2).toString()+"; "+medicoes.get(medicoes.size()-3).toString();
			dataHora = medicao.getDataHoraMedicao();

		}else {/*(descricao.equals("Sensor humidade em baixo!") || descricao.equals("Sensor humidade com problemas!"))*/
			valor = "";
			dataHora = LocalDate.now().toString()+" "+LocalTime.now().toString().substring(0,8);
		}
		Alerta alerta = new Alerta(dataHora, tipoSensor, valor, controlo, limite, descricao);
		System.out.println("Foi criado um alerta: "+descricao);
		return alerta;
	}
	
	public boolean allAboveZona2Seguranca() {
		double limite_temperatura = Double.parseDouble(MainMongoToMySql.getMysqlProperty("LimiteHumidade"));
		double zona_2_seguranca = Double.parseDouble(MainMongoToMySql.getMysqlProperty("Zona2Seguranca"));
		double limite2= limite_temperatura - (limite_temperatura * zona_2_seguranca / 100);
		List<Double> medicoes = getMeasurements();
		for(int i = medicoes.size() - 1; i > medicoes.size() - 4; i-- ) {
			if(medicoes.get(i) < limite2) {
				return false;
			}
		}
		return true;
	}
	
	public boolean allAboveZona1Seguranca() {
		double limite_humidade = Double.parseDouble(MainMongoToMySql.getMysqlProperty("LimiteHumidade"));
		double zona_1_seguranca = Double.parseDouble(MainMongoToMySql.getMysqlProperty("Zona1Seguranca"))/100;
		double limite1= limite_humidade - (limite_humidade * zona_1_seguranca);
		List<Double> medicoes = getMeasurements();
		for(int i = medicoes.size() - 1; i > medicoes.size() - 4; i-- ) {
			if(medicoes.get(i) < limite1) {
				return false;
			}
		}
		return true;
	}
	
	public boolean oneAboveZona1Seguranca() {
		double limite_temperatura = Double.parseDouble(MainMongoToMySql.getMysqlProperty("LimiteHumidade"));
		double zona_1_seguranca = Double.parseDouble(MainMongoToMySql.getMysqlProperty("Zona1Seguranca"));
		double limite1= limite_temperatura - (limite_temperatura * zona_1_seguranca / 100);
		List<Double> medicoes = getMeasurements();
		for(int i = medicoes.size() - 1; i > medicoes.size() - 4; i-- ) {
			if(medicoes.get(i) > limite1) {
				return true;
			}
		}
		return false;
	}
	
}
