package com.ctm;

public class Medicao {
	private double ValorMedicao;
	private String TipoSensor;
	private String DataHoraMedicao;
	public Medicao(double valorMedicao, String tipoSensor, String dataHoraMedicao) {
		super();
		ValorMedicao = valorMedicao;
		TipoSensor = tipoSensor;
		DataHoraMedicao = dataHoraMedicao;
	}
	
	public double getValorMedicao() {
		return ValorMedicao;
	}
	public String getTipoSensor() {
		return TipoSensor;
	}
	public String getDataHoraMedicao() {
		return DataHoraMedicao;
	}
	
	
	
	

}
