package com.ctm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.eclipse.paho.client.mqttv3.MqttClient;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class SendToMysql extends Thread {
	
	enum dayOfWeek {DOM,SEG,TER,QUA,QUI,SEX,SAB}
	
	//mongo em caso de mysql down
	MqttClient mqttclient;
    static MongoClient mongoClient;
    static DB db;
    static DBCollection mongocol_rondaplaneada;
    static String cloud_server = new String();
    static String cloud_topic = new String();
    static String mongo_host = new String();
    static String mongo_database = new String();
    static String mongo_collection_rondaplaneada = new String();
    static Properties CloudToMongoIni = new Properties();
    
    //mysql
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
		System.out.println(LocalTime.now().toString());
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
				String SqlCommando = "Insert into medicoessensores ( ValorMedicao, TipoSensor,DataHoraMedicao, Controlo" + 
					") values ("+med.getValorMedicao()+",'"+med.getTipoSensor()+"','"+med.getDataHoraMedicao()+"','"+med.getControlo()+"');";
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
			if(!e.getMessage().equals("ronda")) { // Não retirar, o trigger que impede alertas de movimento e luminosidade envia o erro, da ronda, se tirar pode tentar enviar email pois vai pensar que o mysql está em baixo
		System.out.println("Error quering  the database . Now sending emails. " + e);
			for(Alerta a: alertas) {
				if(a.getTipoSensor() == "mov" || a.getTipoSensor() == "cell") {
					if(isOnRondaPlaneada(a)) {
						continue;
					}
				}
				String to = MainMongoToMySql.getMysqlProperty("EmailEmergencia");
				String subject = "Alerta: " + a.getDescricao() + " " + a.getDataHora();
				String text = "Hora: " + a.getDataHora()+"\nTipo Sensor: "+a.getTipoSensor()+"\nValores: "+a.getValor()+"\nLimite: "+a.getLimit()+"\nDescriï¿½ï¿½o: "+a.getDescricao()+"\nControlo: "+a.getControlo();
				sendEmail(to, subject, text);
			}
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
	         System.out.println("Sent email successfully....");
	      } catch (MessagingException mex) {
	         mex.printStackTrace();
	      }
	     
	}
	
	public boolean isOnRondaPlaneada(Alerta alerta) {
		connectMongo();
		DBCursor rondaplaneada = mongocol_rondaplaneada.find();
		while(rondaplaneada.hasNext()) {
			int weekDayAlerta = getWeekDay();
			int weekDayRonda = dayOfWeek.valueOf(rondaplaneada.next().get("DiaSemana").toString()).ordinal() + 1;
			System.out.println(weekDayAlerta + " " + weekDayRonda);
			if(weekDayAlerta == weekDayRonda) {
				LocalTime rondaStart = LocalTime.parse(rondaplaneada.curr().get("HoraRonda").toString());
				LocalTime rondaDuration = LocalTime.parse(rondaplaneada.curr().get("Duracao").toString());
				long durationHour = rondaDuration.getLong(ChronoField.HOUR_OF_DAY);
				long durationMinutes = rondaDuration.getLong(ChronoField.MINUTE_OF_HOUR);
				LocalTime rondaEnd =  rondaStart.plusHours(durationHour);
				rondaEnd = rondaEnd.plusMinutes(durationMinutes);
				LocalTime now = LocalTime.now();
				System.out.println(now.toString() + " " + rondaStart.toString());
				if(now.isAfter(rondaStart) && now.isBefore(rondaEnd)) {
					
					System.out.println("Foi encontrada uma ronda na hora do alerta, FALSO ALARME");
					return true;
				}
			
			}
		}
		return false;
	} 
	
	public void connectMongo() {
		loadMongoIni();
		cloud_server = CloudToMongoIni.getProperty("cloud_server");
        cloud_topic = CloudToMongoIni.getProperty("cloud_topic");
        mongo_host = CloudToMongoIni.getProperty("mongo_host");
        mongo_database = CloudToMongoIni.getProperty("mongo_database");
        mongo_collection_rondaplaneada = "rondaplaneada";
        
		mongoClient = new MongoClient(new MongoClientURI(mongo_host));
		db = mongoClient.getDB(mongo_database);
		mongocol_rondaplaneada = db.getCollection(mongo_collection_rondaplaneada);
	}
	
	public void loadMongoIni() {
		 try {
				CloudToMongoIni.load(new FileInputStream("CloudToMongo.ini"));
			} catch (FileNotFoundException  e) { e.printStackTrace();
			} catch (IOException e2) { e2.printStackTrace(); }
	}
	
	public int getWeekDay() {
		Date actualDate = new Date(); 
		Calendar c = Calendar.getInstance();
		c.setTime(actualDate);
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		return dayOfWeek;
	}
}

