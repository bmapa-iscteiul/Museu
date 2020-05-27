package com.ctm;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.mongodb.DBObject;

public class ThreadLuminosidade extends MedicaoThread {

	int sensorMaxSemValor = Integer.parseInt(MainMongoToMySql.getMysqlProperty("SensorMaxSemValor"));
	int sensorMaxDadosInvalidos = Integer.parseInt(MainMongoToMySql.getMysqlProperty("SensorMaxDadosInvalidos"));
	double limiteHumidade = Double.parseDouble(MainMongoToMySql.getMysqlProperty("LimiteLuminosidade"));
	
	
	public ThreadLuminosidade(ShareResourceMedicoes shareresource,ShareResourceRegisto shareResourceReg) {
		super(shareresource,shareResourceReg);
		setName("cell");
	}
	
	public void run() {
		while(isRunning()) {
			try {
				DBObject next = getLastMeasurement();
				if(!dbObjectToMedicao(next).equals(null)) {
					MedicaoSensor medicao = dbObjectToMedicao(next);
					addValue(medicao.getValorMedicao());
					Alerta alerta = checkForAlert(medicao);
					if(podeEnviarAlerta() && alerta != null) {
						setAlertaToShareResource(alerta);
					}
					setMedicaoToShareResource(medicao);
				} else {
					Alerta alerta = checkForSensorAlert();
					if(podeEnviarAlerta() && alerta != null)
						setAlertaToShareResource(alerta);
				}
			}catch(Exception e) {

			}
		}
	}
	
	public Alerta checkForSensorAlert() {
		if(getNoValue()==sensorMaxSemValor) {
			Alerta alerta = makeAlerta("Sensor luminosidade em baixo!", null, null);
			setNoValue(0);
			return alerta;
		}else if(numberOfErrors()==sensorMaxDadosInvalidos) {
			Alerta alerta = makeAlerta("Sensor luminosidade com problemas!", null, null);
			cleanErrorList();
			return alerta;
		}
		return null;
	}

	public Alerta checkForAlert(MedicaoSensor medicao) {
		List<Double> medicoes = getMeasurements();
		double limite_luminosidade = Double.parseDouble(MainMongoToMySql.getMysqlProperty("LimiteLuminosidade"));
		String descricao = "Alerta de Luminosidade!";
		for(int i = 0; i < medicoes.size(); i++ ) {
			if(medicoes.get(i) > limite_luminosidade) {
				Alerta alerta = makeAlerta("Luz Detetada!", medicao, medicoes);
				return alerta;
			}
		}
		return null;
	}
	
	/*FUNCAO GENERICA PARA CRIACAO ALERTA*/
	public Alerta makeAlerta(String descricao, MedicaoSensor medicao, List<Double> medicoes) {
		String tipoSensor = "cell";
		int controlo = 0;
		double limite = limiteHumidade;
		String dataHora;
		String valor;

		if(descricao.equals("Luz Detetada!")) {
			valor = medicoes.get(medicoes.size()-1).toString()+"; "+medicoes.get(medicoes.size()-2).toString()+"; "+medicoes.get(medicoes.size()-3).toString();
			dataHora = medicao.getDataHoraMedicao();

		}else {/*(descricao.equals("Sensor luminosidade em baixo!") || descricao.equals("Sensor luminosidade com problemas!"))*/
			valor = "";
			dataHora = LocalDate.now().toString()+" "+LocalTime.now().toString().substring(0,8);
		}
		Alerta alerta = new Alerta(dataHora, tipoSensor, valor, controlo, limite, descricao);
		System.out.println("Foi criado um alerta: "+descricao);
		return alerta;
	}

}
