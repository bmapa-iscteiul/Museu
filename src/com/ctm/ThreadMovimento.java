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
			Alerta alerta = makeAlerta("Sensor movimento em baixo!", null);
			setNoValue(0);
			setPodeEnviarAlerta(0,false);
			alerta.setIndex(0);
			return alerta;
		}else if(numberOfErrors()==sensorMaxDadosInvalidos && podeEnviarAlerta1(1)) {
			Alerta alerta = makeAlerta("Sensor movimento com problemas!", null);
			cleanErrorList();
			setPodeEnviarAlerta(1,false);
			alerta.setIndex(1);
			return alerta;
		}
		return null;
	}


	public Alerta checkForAlert(MedicaoSensor medicao) {
		List<Double> medicoes = getMeasurements();
		//for(int i = 0; i < medicoes.size(); i++ ) {
			//if(medicoes.get(i) == MOVIMENTO) {
			if(medicao.getValorMedicao()==MOVIMENTO) {
				Alerta alerta = makeAlerta("Movimento detetado!", medicao);
				setPodeEnviarAlerta(2,false);
				alerta.setIndex(2);
				return alerta;
		}
		getMeasurements().remove(0);
		return null;
	}
	
	/*FUNCAO GENERICA PARA CRIACAO ALERTA*/
	public Alerta makeAlerta(String descricao, MedicaoSensor medicao) {
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
		//System.out.println("Foi criado um alerta: "+descricao);
		return alerta;
	}
	

}
