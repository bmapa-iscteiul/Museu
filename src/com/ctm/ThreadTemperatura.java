
package com.ctm;

import java.util.List;

import com.mongodb.DBObject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.mongodb.DBObject;


public class ThreadTemperatura extends MedicaoThread {
	
	int sensorMaxSemValor = Integer.parseInt(MainMongoToMySql.getMysqlProperty("SensorMaxSemValor"));
	int sensorMaxDadosInvalidos = Integer.parseInt(MainMongoToMySql.getMysqlProperty("SensorMaxDadosInvalidos"));
	double limiteTemperatura = Double.parseDouble(MainMongoToMySql.getMysqlProperty("LimiteTemperatura"));
	
		
	public ThreadTemperatura(ShareResourceMedicoes shareresource,ShareResourceRegisto shareResourceReg) {
		super(shareresource, shareResourceReg);
		setName("tmp");
	}
	
	public void run() {
		while(isRunning()) {
			try {
				DBObject next = getLastMeasurement();
				MedicaoSensor medicao = dbObjectToMedicao(next);
				
				if(!(medicao==null)) {
					addValue(medicao.getValorMedicao());
					Alerta alerta = checkForAlert(medicao);
				
					if(alerta != null) {
						System.out.println(alerta.getDescricao());
						setAlertaToShareResource(alerta);
					}
					setMedicaoToShareResource(medicao);
				} else {
					Alerta alerta = checkForSensorAlert();
					if(alerta != null) {
						System.out.println(alerta.getDescricao());
						setAlertaToShareResource(alerta);
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
/*ALERTA DE PROBLEMAS NO SENSOR*/
	public Alerta checkForSensorAlert() {
		if(getNoValue()>=sensorMaxSemValor && podeEnviarAlerta1(0)) {
			Alerta alerta = makeAlerta("Sensor temperatura em baixo!", null, null);
			setNoValue(0);
			setPodeEnviarAlerta(0,false);
			alerta.setIndex(0);
			return alerta;
		}else if(numberOfErrors()==sensorMaxDadosInvalidos && podeEnviarAlerta1(1)) {
			Alerta alerta = makeAlerta("Sensor temperatura com problemas!", null, null);
			cleanErrorList();
			setPodeEnviarAlerta(1,false);
			alerta.setIndex(1);
			return alerta;
		}
		return null;
	}

/*ALERTA DE SUBIDA DE TEMP*/
	public Alerta checkForAlert(MedicaoSensor medicao) {
		List<Double> medicoes = getMeasurements();
		if(medicoes.size() < 3) {
			return null;
		}
		double lastMedicao = medicoes.get(medicoes.size()-1);
		if(allAboveZona1Seguranca() && oneAboveLimite() && podeEnviarAlerta1(3)) {
			Alerta alerta = makeAlerta("Incendio", medicao, medicoes);
			setPodeEnviarAlerta(3,false);
			setPodeEnviarAlerta(2,false);
			alerta.setIndex(3);
			return alerta;
		}else if(allAboveZona2Seguranca() && oneAboveZona1Seguranca() && podeEnviarAlerta1(2)) {
			Alerta alerta = makeAlerta("Temperatura a subir rapidamente!", medicao, medicoes);
			setPodeEnviarAlerta(2,false);
			alerta.setIndex(2);
			return alerta;
		}
		getMeasurements().remove(0);
		return null;
	}

/*FUNCAO GENERICA PARA CRIACAO ALERTA*/
	public Alerta makeAlerta(String descricao, MedicaoSensor medicao, List<Double> medicoes) {
		String tipoSensor = "tmp";
		int controlo = 0;
		double limite = limiteTemperatura;
		String dataHora;
		String valor;
		
		if(descricao.equals("Temperatura a subir rapidamente!")
			|| descricao.equals("Incendio")) {
			valor = medicoes.get(medicoes.size()-3).toString()+"; "+medicoes.get(medicoes.size()-2).toString()+"; "+medicoes.get(medicoes.size()-1).toString();
			dataHora = medicao.getDataHoraMedicao();
			
			if (descricao.equals("Temperatura a subir rapidamente!")){
				controlo=0;
			} else {
				controlo=1;
			}
			
		}else {/*(descricao.equals("Sensor temperatura em baixo!") || descricao.equals("Sensor temperatura com problemas!"))*/
			valor = "";
			dataHora = LocalDate.now().toString()+" "+LocalTime.now().toString().substring(0,8);
			controlo = 0;
		}
		Alerta alerta = new Alerta(dataHora, tipoSensor, valor, controlo, limite, descricao);
		//System.out.println("Foi criado um alerta: "+descricao);
		return alerta;
	}
	
	public boolean allAboveZona2Seguranca() {
		double zona_2_seguranca = Double.parseDouble(MainMongoToMySql.getMysqlProperty("Zona2Seguranca"))/100;
		double limite2= limiteTemperatura - (limiteTemperatura * zona_2_seguranca);
		List<Double> medicoes = getMeasurements();
		for(int i = medicoes.size() - 1; i > medicoes.size() - 4; i-- ) {
			if(medicoes.get(i) < limite2) {
				return false;
			}
		}
		return true;
	}
	
	public boolean oneAboveZona1Seguranca() {
		double zona_1_seguranca = Double.parseDouble(MainMongoToMySql.getMysqlProperty("Zona1Seguranca"))/100;
		double limite1= limiteTemperatura - (limiteTemperatura * zona_1_seguranca);
		List<Double> medicoes = getMeasurements();
		for(int i = medicoes.size() - 1; i > medicoes.size() - 4; i-- ) {
			if(medicoes.get(i) > limite1) {
				return true;
			}
		}
		return false;
	}
	
	public boolean allAboveZona1Seguranca() {
		double limite_temperatura = Double.parseDouble(MainMongoToMySql.getMysqlProperty("LimiteTemperatura"));
		double zona_1_seguranca = Double.parseDouble(MainMongoToMySql.getMysqlProperty("Zona1Seguranca"))/100;
		double limite1= limite_temperatura - (limite_temperatura * zona_1_seguranca);
		List<Double> medicoes = getMeasurements();
		for(int i = medicoes.size() - 1; i > medicoes.size() - 4; i-- ) {
			if(medicoes.get(i) < limite1) {
				return false;
			}
		}
		return true;
	}
	
	public boolean oneAboveLimite() {
		double limite_temperatura = Double.parseDouble(MainMongoToMySql.getMysqlProperty("LimiteTemperatura"));
		List<Double> medicoes = getMeasurements();
		for(int i = medicoes.size() - 1; i > medicoes.size() - 4; i-- ) {
			if(medicoes.get(i) >= limite_temperatura) {
				return true;
			}
		}
		return false;
	}


}

