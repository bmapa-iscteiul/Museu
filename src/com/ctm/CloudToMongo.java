package com.ctm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.swing.JOptionPane;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.util.JSON;

/*
 * 
 * 1) utilizar o message.contains para caso sesnor inativo=> nao aparecer (evitar nullpointerexception)
 * 
 */

public class CloudToMongo implements MqttCallback {
    MqttClient mqttclient;
    static MongoClient mongoClient;
    static DB db;
    static DBCollection mongocol_sensor;
    static DBCollection mongocol_invalidas;
    static String cloud_server = new String();
    static String cloud_topic = new String();
    static String mongo_host = new String();
    static String mongo_database = new String();
    static String mongo_collection_invalidas = new String();
    static String mongo_collection_sensor = new String();
 //   static MongoToMysql mysql = new MongoToMysql();
    static Properties CloudToMongoIni = new Properties();
    
    //Atualizador ini
    static double TMP_MAX;
    static double TMP_MIN;
    static double CELL_MAX;
       
    ArrayList<MedicaoSensor> medicoesSensorQueue = new ArrayList<MedicaoSensor>(); 

    public static void main(String[] args) {
    	loadIni();
        new CloudToMongo().connecCloud();
        new CloudToMongo().connectMongo();
    }
    
  //Loads all the info of the file into variables
    public static void loadIni() {
    	try {
            CloudToMongoIni = getIniFile();
            TMP_MAX = Integer.parseInt(CloudToMongoIni.getProperty("MaxValidoTemperatura"));   
            TMP_MIN = Integer.parseInt(CloudToMongoIni.getProperty("MinValidoTemperatura"));
            CELL_MAX = Integer.parseInt(CloudToMongoIni.getProperty("MaxValidoLuminosidade"));
            
            cloud_server = CloudToMongoIni.getProperty("cloud_server");
            cloud_topic = CloudToMongoIni.getProperty("cloud_topic");
            mongo_host = CloudToMongoIni.getProperty("mongo_host");
            mongo_database = CloudToMongoIni.getProperty("mongo_database");
            mongo_collection_invalidas = CloudToMongoIni.getProperty("mongo_collection_invalidas");
            mongo_collection_sensor = CloudToMongoIni.getProperty("mongo_collection_sensor");
            System.out.println("Loaded the ini file");
        } catch (Exception e) {

            System.out.println("Error reading CloudToMongo.ini file " + e);
            JOptionPane.showMessageDialog(null, "The CloudToMongo.ini file wasn't found.", "CloudToMongo", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static Properties getIniFile() {
        try {
			CloudToMongoIni.load(new FileInputStream("CloudToMongo.ini"));
		} catch (FileNotFoundException  e) { e.printStackTrace();
		} catch (IOException e2) { e2.printStackTrace(); }
        return CloudToMongoIni;
    }

    public void connecCloud() {
		int i;
        try {
			i = new Random().nextInt(100000);
            mqttclient = new MqttClient(cloud_server, "CloudToMongo_"+String.valueOf(i)+"_"+cloud_topic);
            mqttclient.connect();
            mqttclient.setCallback(this);
            mqttclient.subscribe(cloud_topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void connectMongo() {
		mongoClient = new MongoClient(new MongoClientURI(mongo_host));
		db = mongoClient.getDB(mongo_database);
        mongocol_sensor = db.getCollection(mongo_collection_sensor);
        mongocol_invalidas = db.getCollection(mongo_collection_invalidas);
    }

    @SuppressWarnings("deprecation")
	@Override
    public void messageArrived(String topic, MqttMessage c) throws Exception {
        try {
                DBObject message = (DBObject) JSON.parse(clean(c.toString()));
                DBObject original_msg =  (DBObject) JSON.parse(clean(c.toString()));
                
                if(!correct_invalidChar(message).toString().equals(original_msg.toString())) {
                		mongocol_invalidas.insert(original_msg);
                		System.out.println("Inserida na colecao invalidas");
                		System.out.println(message);
                }else if(message_hasValidValue(message)) {
                	message = java_replaceDateTime(message);
                	message = correct_invalidChar(message);
                	mongocol_sensor.insert(message);
                	System.out.println("Inserida na colecao sensor");
                }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    public DBObject correct_invalidChar(DBObject message) {
    	String tmp = (String) message.get("tmp");
    	String hum = (String) message.get("hum");
    	String cell = (String) message.get("cell");
    	String mov = (String) message.get("mov");
    	
    	if(!tmp.equals("")) {
    		try {
    			double value = Double.parseDouble(tmp);
    			correct_outOfBoundaries(message, "tmp", value);
    		} catch (NumberFormatException e) {
    			message.put(tmp, "NA");
    		}
    	}if(!hum.equals("")) {
    		try {
    			double value = Double.parseDouble(hum);
    			correct_outOfBoundaries(message, "hum", value);
    		} catch (NumberFormatException e) {
    			message.put(hum, "NA");
    		}
    	}if(!cell.equals("")) {
    		try {
    			double value = Double.parseDouble(cell);
    			correct_outOfBoundaries(message, "cell", value);
    		} catch (NumberFormatException e) {
    			message.put(cell, "NA");
    		}
    	}if(!mov.equals("")) {
    		try {
    			double value = Double.parseDouble(mov);
    			correct_outOfBoundaries(message, "mov", value);
    		} catch (NumberFormatException e) {
    			message.put(mov, "NA");
    		}
    	}
    	return message;  	
    }
    
    public void correct_outOfBoundaries(DBObject message, String type, Double value) {
    	switch(type) {
    	case "tmp":
    		if(value < TMP_MIN || value > TMP_MAX) {message.put("tmp", "NA");}
    		break;
    	case "hum":
    		if(value < 0 || value > 100) {message.put("hum", "NA");}
    		break;
    	case "cell":
    		if(value < 0 || value > CELL_MAX) {message.put("cell", "NA");}
    		break;
    	case "mov":
    		if(value!=0.0 && value!=1.0) {message.put("mov", "NA");}
    		break;
    	}
    }
    
    
    public DBObject java_replaceDateTime(DBObject message) throws ParseException {
    	String time = LocalTime.now().toString().substring(0,8);
    	String date = LocalDate.now().toString();
    	message.put("tim", time);
    	message.put("dat", time);
    	return message;
    }
    
    @Override
    public void connectionLost(Throwable cause) {
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
  
    public String clean(String message) {
    	String old = "\""+"mov"+"\""+":"+"\""+"0"+"\"";
    	message = message.replace(old,",");
		return message.replace("\""+"\"", "\""+","+"\"");// (message.replaceAll("\"\"", "\","));   
    }	 
    
    //Verificar se mensagem tem pelo menos 1 valor aceitavel => colocar nas validas
    public boolean message_hasValidValue(DBObject message) {
    	String tmp = message.get("tmp").toString();
    	String hum = message.get("hum").toString();
    	String cell = message.get("cell").toString();
        String mov = message.get("mov").toString();
        if((!tmp.equals("") && !tmp.equals("NA"))
        	|| (!hum.equals("") && !hum.equals("NA"))
        	|| (!cell.equals("") && !cell.equals("NA"))
			|| (!mov.equals("") && !mov.equals("NA"))){
        	return true;
        }
        return false;
    }
    
     
}