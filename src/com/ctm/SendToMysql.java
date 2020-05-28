package com.ctm;

import java.io.FileInputStream;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
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
	private static String emailMuseu = "email.museu.sid@gmail.com";
	private static String passwordEmailMuseu = "passwordmuseu123";
	
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
		}catch (Exception e){
		System.out.println("Error quering  the database . Now sending emails. " + e);
			for(Alerta a: alertas) {
				String to = MainMongoToMySql.getMysqlProperty("emergencyEmail");
				String subject = "Alerta:" + a.getDescricao() + " " + a.getDataHora();
				String text = "Hora:" + a.getDataHora()+"\nTipo Sensor: "+a.getTipoSensor()+"\nValores: "+a.getValor()+"\nLimite: "+a.getLimit()+"\nDescrição: "+a.getDescricao()+"\nControlo: "+a.getControlo();
				sendEmail(to, subject, text);
			}
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
	
	public void sendEmail(String to, String subject, String text) {
	     String from = emailMuseu;
	     String host = "smtp.gmail.com";
	     Properties properties = System.getProperties();
	     properties.put("mail.smtp.auth", "true");
	     properties.put("mail.smtp.starttls.enable", "true");
	     properties.setProperty("mail.smtp.host", host);
	     
	     Session session = Session.getInstance(properties, new Authenticator() {
	 		@Override
	 		protected PasswordAuthentication getPasswordAuthentication() {
	 			return new PasswordAuthentication(emailMuseu, passwordEmailMuseu);
	 		}
	 	});
	     session.getProperties().put("mail.smtp.ssl.trust", "smtp.gmail.com");

	     try {
	         MimeMessage message = new MimeMessage(session);

	         message.setFrom(new InternetAddress(from));
	         message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
	         message.setSubject(subject);
	         message.setText(text);

	         Transport.send(message);
	         System.out.println("Sent message successfully....");
	      } catch (MessagingException mex) {
	         mex.printStackTrace();
	      }
	     
	}
	

}
