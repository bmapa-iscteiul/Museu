package com.ctm;

public class MedicaoSensor {
	private double valorMedicao;
	private String tipoSensor;
	private String dataHoraMedicao;
	private int tipoMedicao;
	
	private double limiteTemperatura = Double.parseDouble(MainMongoToMySql.getMysqlProperty("LimiteTemperatura"));
	double zona_2_seguranca = Double.parseDouble(MainMongoToMySql.getMysqlProperty("Zona2Seguranca"))/100;
	double limite2 = limiteTemperatura - (limiteTemperatura * zona_2_seguranca);
	double zona_1_seguranca = Double.parseDouble(MainMongoToMySql.getMysqlProperty("Zona1Seguranca"))/100;
	double limite1= limiteTemperatura - (limiteTemperatura * zona_1_seguranca);
	
	public MedicaoSensor(double valorMedicao, String tipoSensor, String dataHoraMedicao) {
		super();
		this.valorMedicao = valorMedicao;
		this.tipoSensor = tipoSensor;
		this.dataHoraMedicao = dataHoraMedicao;
		setTipoMedicao();
	}
	
	public double getValorMedicao() {
		return valorMedicao;
	}
	public String getTipoSensor() {
		return tipoSensor;
	}
	public String getDataHoraMedicao() {
		return dataHoraMedicao;
	}
	public void setTipoMedicao() {
		if(valorMedicao>=limiteTemperatura) {
			tipoMedicao = 2;
		} else if(valorMedicao>limite2) {
			tipoMedicao = 1;
		} else {
			tipoMedicao = 0;
		}
	}
	public int getTipoMedicao() {
		return tipoMedicao;
	}
	
	
	
	

}
