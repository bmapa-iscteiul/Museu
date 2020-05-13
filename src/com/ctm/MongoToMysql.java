package com.ctm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JOptionPane;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;


public class MongoToMysql {
	//Mysql
	static Connection conn;
	static Statement s;
	static ResultSet rs;
	static String mysql_database_password= new String();
	static String mysql_database_user= new String();
	static String mysql_database_connection= new String();
	
	//Mongo
	static MongoClient mongoClient;
    static DB db;
    static DBCollection mongocol;
    static String cloud_server = new String();
    static String cloud_topic = new String();
    static String mongo_host = new String();
    static String mongo_database = new String();
    static String mongo_collection_invalidas = new String();
    static String mongo_collection_sensor = new String();
    static MongoToMysql mysql = new MongoToMysql();
    
	//ini file
    static Properties p = new Properties();
	
	public MongoToMysql() {
		mysql_database_password = "";
		mysql_database_user = "root";
		mysql_database_connection = "jdbc:mysql://localhost/museu";
	}
	public static void main(String[] args) {
		loadIni();
	    new MongoToMysql().connectToMongo();
	    new MongoToMysql().connectToMysql();
	    new SepararMedicoes(mongocol).start();
	    while(!Thread.interrupted()) {
	    	
	    }
		
	}
	
	public void connectToMysql() {
		try{ 	
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn =  DriverManager.getConnection(mysql_database_connection+
												"?user="+
												mysql_database_user+
												"&password="+
												mysql_database_password);
			System.out.println("Connection successfull to Mysql");
		}catch (Exception e){
			System.out.println("Failed to connect to Mysql ");
		}	
		
	}
	
	public void connectToMongo() {
		try {
			mongoClient = new MongoClient(new MongoClientURI(mongo_host));
			db = mongoClient.getDB(mongo_database);
	        mongocol = db.getCollection(mongo_collection_sensor);
	        System.out.println("Connection successfull to Mongo");
		}catch(Exception e) {
			System.out.println("Failed to connect to MongoDB");
		}
	}
	
	public void addDataToMedicoesSensores(ArrayList<MedicaoSensor> data) {
		for(MedicaoSensor line : data) {
			String SqlCommando;
			SqlCommando = "insert into medicoessensores(ValorMedicao, TipoSensor, DataHoraMedicao) values(" + line.ValorMedicao + ",'" + line.TipoSensor + "','" + line.DataHoraMedicao +"');";
			try {
				s = conn.createStatement();
				int result = s.executeUpdate(SqlCommando);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Migração completa");
	}
	
	public String changeDateFormat(String date) {
        try {
       	 	date = date.replace('/', '-');
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
			date = sdf2.format(sdf.parse(date));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return date;
   }
	

	public static void loadIni() {
    	try {
            p = getIniFile();
            
            cloud_server = p.getProperty("cloud_server");
            cloud_topic = p.getProperty("cloud_topic");
            mongo_host = p.getProperty("mongo_host");
            mongo_database = p.getProperty("mongo_database");
            mongo_collection_invalidas = p.getProperty("mongo_collection_invalidas");
            mongo_collection_sensor = p.getProperty("mongo_collection_sensor");
        } catch (Exception e) {

            System.out.println("Error reading CloudToMongo.ini file " + e);
            JOptionPane.showMessageDialog(null, "The CloudToMongo.ini file wasn't found.", "CloudToMongo", JOptionPane.ERROR_MESSAGE);
        }
    }
	
	public static Properties getIniFile() {
    	Properties p = new Properties();
        try {
			p.load(new FileInputStream("C:\\Users\\Bruno\\eclipse-workspace\\CTMongo\\src\\com\\ctm\\CloudToMongo.ini"));
		} catch (FileNotFoundException  e2) {
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return p;
    }
	/*// data vem no formato DD/MM/YYYY queremos YYYY-MM-DD
                String date = (String)document_json.get("dat");
                date = changeDateFormat(date);
                
                String time = (String)document_json.get("tim");
                
                String timestamp = date + ' ' + time;
                    		
                double tmp = Double.parseDouble(document_json.get("tmp").toString());
                MedicaoSensor mTmp = new MedicaoSensor(tmp, "tmp", timestamp);
                
                double hum = Double.parseDouble(document_json.get("hum").toString());
                MedicaoSensor mHum = new MedicaoSensor(hum, "hum", timestamp);
                
                int cell = Integer.parseInt(document_json.get("cell").toString());
                MedicaoSensor mCell = new MedicaoSensor(cell, "cell", timestamp);
                
                int mov = Integer.parseInt(document_json.get("mov").toString());
                MedicaoSensor mMov = new MedicaoSensor(mov, "mov", timestamp);
                
                if(medicoesSensorQueue.size() >= LIST_MAX_SIZE ) {
                	mysql.connect();
                	mysql.addDataToMedicoesSensores(medicoesSensorQueue);
                	
                	medicoesSensorQueue.clear();
                }
                	medicoesSensorQueue.add(mTmp);
 	                medicoesSensorQueue.add(mHum);
 	                medicoesSensorQueue.add(mCell);
 	                medicoesSensorQueue.add(mMov);
                	
             */
	
	
}

