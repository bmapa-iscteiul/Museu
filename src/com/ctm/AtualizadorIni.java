package com.ctm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.Properties;

public class AtualizadorIni {

	static Properties MongoToMysqlIni = new Properties();
	static Properties CloudToMongoIni = new Properties();
	
	private Connection mySqlConnection;
	private Statement mySqlstatements;
	
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
	
	public void atualizaMysql(String key, String value) {
		connectToMysql();
		String sql_query = "UPDATE sistema SET " + key + " = " + "'" + value + "';";
		try {
			mySqlstatements.executeUpdate(sql_query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
