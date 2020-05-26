package com.ctm;

import java.util.List;

import com.mongodb.DBObject;

public class ThreadMovimento extends MedicaoThread {
	private static int MOVIMENTO = 1;

	public ThreadMovimento(ShareResourceMedicoes shareresource,ShareResourceRegisto shareResourceReg) {
		super(shareresource,shareResourceReg);
		setName("mov");
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
		String descricao = "Alerta de Movimento!";
		for(int i = 0; i < medicoes.size(); i++ ) {
			if(medicoes.get(i) == MOVIMENTO) {
				Alerta alerta = new Alerta(medicao.getDataHoraMedicao(), "mov", medicoes.get(i), 0, 1, descricao );
				return alerta;
			}
		}
		return null;
	}
	

}
