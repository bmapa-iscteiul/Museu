package com.ctm;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.mongodb.DBObject;

public class ThreadMovimento extends MedicaoThread {
	private static int MOVIMENTO = 1;
	int sensorMaxSemValor = Integer.parseInt(MainMongoToMySql.getMysqlProperty("SensorMaxSemValor"));
	int sensorMaxDadosInvalidos = Integer.parseInt(MainMongoToMySql.getMysqlProperty("SensorMaxDadosInvalidos"));
	

	public ThreadMovimento(ShareResourceMedicoes shareresource,ShareResourceRegisto shareResourceReg) {
		super(shareresource,shareResourceReg);
		setName("mov");
	}
	
	public void run() {
		while(isRunning()) {
			/*try {
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

			}*/

		}
	}
	
	public Alerta checkForSensorAlert() {
		if(getNoValue()==sensorMaxSemValor) {
			Alerta alerta = makeAlerta("Sensor movimento em baixo!", null, null);
			setNoValue(0);
			return alerta;
		}else if(numberOfErrors()==sensorMaxDadosInvalidos) {
			Alerta alerta = makeAlerta("Sensor movimento com problemas!", null, null);
			cleanErrorList();
			return alerta;
		}
		return null;
	}


	public Alerta checkForAlert(MedicaoSensor medicao) {
		List<Double> medicoes = getMeasurements();
		String descricao = "Movimento detetado!";
		for(int i = 0; i < medicoes.size(); i++ ) {
			if(medicoes.get(i) == MOVIMENTO) {
				Alerta alerta = makeAlerta(descricao, medicao, medicoes);
				return alerta;
			}
		}
		return null;
	}
	
	/*FUNCAO GENERICA PARA CRIACAO ALERTA*/
	public Alerta makeAlerta(String descricao, MedicaoSensor medicao, List<Double> medicoes) {
		String tipoSensor = "mov";
		int controlo = 0;
		String dataHora;
		String valor;

		if(descricao.equals("Movimento detetado!")) {
			dataHora = medicao.getDataHoraMedicao();
			valor = "1";

		}else {/*(descricao.equals("Sensor movimento em baixo!") || descricao.equals("Sensor movimento com problemas!"))*/
			valor = "";
			dataHora = LocalDate.now().toString()+" "+LocalTime.now().toString().substring(0,8);
		}
		Alerta alerta = new Alerta(dataHora, tipoSensor, valor, controlo, MOVIMENTO, descricao);
		System.out.println("Foi criado um alerta: "+descricao);
		return alerta;
	}
	

}
