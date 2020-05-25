package com.ctm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;

public class SendToMysql extends Thread {
	
	private Connection mySqlConnection;
	private Statement mySqlstatements;
	private ShareResourceRegisto shareresource;
	private static  int sleepTime=6000;
	
	public SendToMysql(ShareResourceRegisto sh) {
			this.shareresource=sh;
	}
	
	private void connectToMysql() {
		String database_password = MainMongoToMySql.getMysqlProperty("database_password");
		String database_user = MainMongoToMySql.getMysqlProperty("database_user");
		String database_connection = MainMongoToMySql.getMysqlProperty("mysql_host");
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			mySqlConnection =  DriverManager.getConnection(database_connection,database_user,database_password);
			mySqlstatements = this.mySqlConnection.createStatement();
		} catch (Exception e) {
			System.out.println("Server down, unable to make the connection. ");
		}
	} 
	
	public void run() {
		connectToMysql();
		while(true) {
			try {
				sleepTime=getMigrationInterval();
				System.out.println("sleep time->"+sleepTime);
				sleep(sleepTime);
				List<MedicaoSensor> medicoes = shareresource.getMedicoes();
				sendMedicoes(medicoes);
			} catch (InterruptedException e) {
				//alerta
				List<Alerta> alertas = shareresource.getAlertas();
				sendALerta(alertas);
			}
			
		}
	}

	private void sendMedicoes(List<MedicaoSensor> medicoes) {
		try{
			for(MedicaoSensor med:medicoes) {
				String SqlCommando = "Insert into medicoessensores ( ValorMedicao, TipoSensor,DataHoraMedicao" + 
					") values ("+med.getValorMedicao()+",'"+med.getTipoSensor()+"','"+med.getDataHoraMedicao()+"');";
				mySqlstatements.executeUpdate(SqlCommando);
			}
		}catch (Exception e)
		{
		System.out.println("Error quering  the database . " + e);
		//enviar email
		}
	}
	
	private void sendALerta(List<Alerta> alertas) {
		try{
			for(Alerta a:alertas) {
				
				String SqlCommando = "Insert into alerta ( DataHoraMedicao, TipoSensor,ValorMedicao,Limite,Descricao,Controlo" + 
						") values ('"+a.getDataHora()+"','"+a.getTipoSensor()+"','"+a.getValor()+"','"+a.getLimit()+"','"+a.getDescricao()+"','"+a.getControlo()+"');";
				mySqlstatements.executeUpdate(SqlCommando);
			}
		}catch (Exception e)
		{
		System.out.println("Error quering  the database . " + e);
		//enviar email
		}
	}
	
	public int getMigrationInterval() {
		String concatTime = MainMongoToMySql.getMysqlProperty("IntervaloMigracaoMysql");
		int hours = Integer.parseInt(concatTime.substring(0,concatTime.indexOf(":")));
		int minutes = Integer.parseInt(concatTime.substring(concatTime.indexOf(":")+1, concatTime.indexOf(":")+3));
		int seconds = Integer.parseInt(concatTime.substring(concatTime.indexOf(":")+4));
		int migrationTime = ((hours*3600)+(minutes*60)+seconds)*1000;
		return migrationTime;
	}
	
	
	

}
