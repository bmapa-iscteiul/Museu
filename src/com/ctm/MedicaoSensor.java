package com.ctm;

public class MedicaoSensor{
	public double ValorMedicao;
	public String TipoSensor;
	public String DataHoraMedicao;
	
	public MedicaoSensor(double valorMedicao, String tipoSensor, String dataHoraMedicao) {
		this.ValorMedicao = valorMedicao;
		this.TipoSensor = tipoSensor;
		this.DataHoraMedicao = dataHoraMedicao;
	}
}