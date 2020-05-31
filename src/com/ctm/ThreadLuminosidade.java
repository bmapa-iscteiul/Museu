package com.ctm;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.mongodb.DBObject;

public class ThreadLuminosidade extends MedicaoThread {

	int sensorMaxSemValor = Integer.parseInt(MainMongoToMySql.getMysqlProperty("SensorMaxSemValor"));
	int sensorMaxDadosInvalidos = Integer.parseInt(MainMongoToMySql.getMysqlProperty("SensorMaxDadosInvalidos"));
	double limiteLuminosidade = Double.parseDouble(MainMongoToMySql.getMysqlProperty("LimiteLuminosidade"));
	
	
	public ThreadLuminosidade(ShareResourceMedicoes shareresource,ShareResourceRegisto shareResourceReg) {
		super(shareresource,shareResourceReg);
		setName("cell");
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
			}catch(Exception e) {

			}
		}
	}
	
/*ALERTA DE PROBLEMAS NO SENSOR*/	
	public Alerta checkForSensorAlert() {
		if(getNoValue()>=sensorMaxSemValor && podeEnviarAlerta1(0)) {
			Alerta alerta = makeAlerta("Sensor luminosidade em baixo!", null);
			setNoValue(0);
			setPodeEnviarAlerta(0,false);
			alerta.setIndex(0);
			return alerta;
		}else if(numberOfErrors()==sensorMaxDadosInvalidos && podeEnviarAlerta1(1)) {
			Alerta alerta = makeAlerta("Sensor luminosidade com problemas!", null);
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
			//if(medicoes.get(i) > limiteLuminosidade) {
		if(medicao.getValorMedicao()>=limiteLuminosidade) {
			Alerta alerta = makeAlerta("Luz detetada!", medicao);
			setPodeEnviarAlerta(2,false);
			alerta.setIndex(2);
			return alerta;
		}
		getMeasurements().remove(0);
		return null;
	}
	
	/*FUNCAO GENERICA PARA CRIACAO ALERTA*/
	public Alerta makeAlerta(String descricao, MedicaoSensor medicao) {
		String tipoSensor = "cell";
		int controlo = 0;
		double limite = limiteLuminosidade;
		String dataHora;
		String valor;

		if(descricao.equals("Luz detetada!")) {
			valor = String.valueOf(medicao.getValorMedicao());
			dataHora = medicao.getDataHoraMedicao();
			controlo=1;

		}else {/*(descricao.equals("Sensor luminosidade em baixo!") || descricao.equals("Sensor luminosidade com problemas!"))*/
			valor = "";
			dataHora = LocalDate.now().toString()+" "+LocalTime.now().toString().substring(0,8);
			controlo=0;
		}
		Alerta alerta = new Alerta(dataHora, tipoSensor, valor, controlo, limite, descricao);
		//System.out.println("Foi criado um alerta: "+descricao);
		return alerta;
	}

}
