package com.ctm;



public class Alerta {
	private String DataHora;
	private String TipoSensor;
	private String valor;
	private int controlo;
	private double limit;
	private String descricao;
	public Alerta(String dataHora, String tipoSensor, String valor, int controlo, double limit, String descricao) {
		DataHora = dataHora;
		TipoSensor = tipoSensor;
		this.valor = valor;
		this.controlo = controlo;
		this.limit = limit;
		this.descricao = descricao;
	}
	
	public String getDataHora() {
		return DataHora;
	}

	public double getLimit() {
		return limit;
	}

	public String getTipoSensor() {
		return TipoSensor;
	}


	public String getValor() {
		return valor;
	}


	public String getDescricao() {
		return descricao;
	}
	
	public int getControlo() {
		return controlo;
	}
	
	

}
