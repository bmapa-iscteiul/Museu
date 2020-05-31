package com.ctm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class AtualizadorIni {

	static Properties MongoToMysqlIni = new Properties();
	static Properties CloudToMongoIni = new Properties();
	
	public AtualizadorIni() {
		loadMongoToMysqlIni();
	}

	public static void main(String[] args) {
 
	}
	
	public void atualizarCampoMongoToMysqlIni(String key, String value) {
		MongoToMysqlIni.setProperty(key, value);
	}
	
	public void atualizarCampoCloudToMongoIni(String key, String value) {
		CloudToMongoIni.setProperty(key, value);
	}

	public static void loadMongoToMysqlIni() {
		try {
				MongoToMysqlIni.load(new FileInputStream("MongoToMysql.ini"));
		} catch (FileNotFoundException  e) { e.printStackTrace();
		} catch (IOException e2) { e2.printStackTrace(); }
	}
	
	public static void loadCloudToMongoIni() {
		try {
				CloudToMongoIni.load(new FileInputStream("CloudToMongo.ini"));
		} catch (FileNotFoundException  e) { e.printStackTrace();
		} catch (IOException e2) { e2.printStackTrace(); }
	}
}
