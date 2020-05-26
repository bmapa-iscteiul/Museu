package com.ctm;

import java.util.List;

import com.mongodb.DBObject;

public class ThreadLuminosidade extends MedicaoThread {

	
	public ThreadLuminosidade(ShareResourceMedicoes shareresource,ShareResourceRegisto shareResourceReg) {
		super(shareresource,shareResourceReg);
		setName("cell");
	}
	
	public void run() {
		while(isRunning()) {
			try {
				DBObject next = getLastMeasurement();
				MedicaoSensor medicao = dbObjectToMedicao(next);
				addValue(medicao.getValorMedicao());
				Alerta alerta = checkForAlert(medicao);
				if(podeEnviarAlerta() && alerta != null) {
					setAlertaToshareReource(alerta);
				}
				setMedicaoToShareResource(medicao);
			}catch(Exception e) {
				
			}
		}
	}
	
	public Alerta checkForAlert(MedicaoSensor medicao) {
		List<Double> medicoes = getMeasurements();
		double limite_luminosidade = Double.parseDouble(MainMongoToMySql.getMysqlProperty("LimiteLuminosidade"));
		String descricao = "Alerta de Luminosidade!";
		for(int i = 0; i < medicoes.size(); i++ ) {
			if(medicoes.get(i) > limite_luminosidade) {
				Alerta alerta = new Alerta(medicao.getDataHoraMedicao(), "cell", medicoes.get(i), 0, limite_luminosidade, descricao );
				return alerta;
			}
		}
		return null;
	}

}
