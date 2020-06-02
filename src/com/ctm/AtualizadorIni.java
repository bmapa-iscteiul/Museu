package com.ctm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class AtualizadorIni {

	static Properties MongoToMysqlIni = new Properties();
	static Properties CloudToMongoIni = new Properties();
	
	private static Connection mySqlConnection;
	private static Statement mySqlstatements;
	
	private static ArrayList<JTextField> textFields = new ArrayList<>();
	private static String[] not_parameterizaveis = {"database_user", "database_password", "cloud_server", "mysql_host", "cloud_topic","mongo_collection_invalidas","mongo_collection_sensor","mongo_database",
	"mongo_host","cloud_server","cloud_topic", "atualizador_user", "atualizador_pass"};
	
	static JFrame f=new JFrame();
	
	public AtualizadorIni() {
		loadMongoToMysqlIni();
	}

	public static void main(String[] args) {
		loadCloudToMongoIni();
		loadMongoToMysqlIni();
		initGUI();
	}
	
	public static void atualizarCampoMongoToMysqlIni(String key, String value) {
		MongoToMysqlIni.setProperty(key, value);
		try {
			MongoToMysqlIni.store(new FileOutputStream("MongoToMysql.ini"), null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void atualizarCampoCloudToMongoIni(String key, String value) {
		CloudToMongoIni.setProperty(key, value);
		try {
			CloudToMongoIni.store(new FileOutputStream("CloudToMongo.ini"), null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	private static void connectToMysql() {
		System.out.println(LocalTime.now().toString());
		String database_password = MainMongoToMySql.getMysqlProperty("atualizador_pass");
		String database_user = MainMongoToMySql.getMysqlProperty("atualizador_user");
		String database_connection = MainMongoToMySql.getMysqlProperty("mysql_host");
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			mySqlConnection =  DriverManager.getConnection(database_connection,database_user,database_password);
			mySqlstatements = mySqlConnection.createStatement();
		} catch (Exception e) {
			System.out.println("Server down, unable to make the connection. ");
		}
	} 
	
	public static void atualizaMysql(String key, String value) {
		connectToMysql();
		String sql_query = "UPDATE sistema SET " + key + " = " + "'" + value + "';";
		try {
			mySqlstatements.executeUpdate(sql_query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void initGUI() {
        Iterator it = MongoToMysqlIni.entrySet().iterator();
        int i  =  0;
		while(it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			if(pair.getKey().toString().startsWith("[") || pair.getKey().toString().startsWith("*") || !isParameterizavel(pair.getKey().toString())) {
				continue;
			}
	        JLabel label = new JLabel(pair.getKey().toString());
	        label.setBounds(20,25 + 30 * i, 180,30); 
	        i++;
	        f.add(label);
	        JTextField textField = new JTextField(pair.getValue().toString());
	        textField.setToolTipText(pair.getKey().toString());
	        textField.setBounds(220, 30*i, 250, 30);
	        textFields.add(textField);
	        f.add(textField);
		}
		JButton b=new JButton("Update");//creating instance of JButton  
		b.setBounds(130,100,80, 40);//x axis, y axis, width, height 
		b.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){  
			            for(JTextField jtf: textFields) {
			            	System.out.println("button pressed");
			            	String campo = jtf.getToolTipText();
			            	String valor = jtf.getText();
			            	atualizarCampoMongoToMysqlIni(campo, valor);
			            	atualizaMysql(campo, valor);
			            }  
			        }  
		});   
			 
		b.setLocation(300, 540);
		f.add(b);//adding button in JFrame  
		
		JButton b1 =new JButton("CloudToMongo");//creating instance of JButton  
		b1.setBounds(130,100,160, 40);//x axis, y axis, width, height 
		b1.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){  
						textFields.clear();
						f.getContentPane().removeAll();
						f.repaint();
						initCloudToMongoGUI();
			        }  
		});   
			 
		b1.setLocation(50,500);
		f.add(b1);        
		
		f.setSize(500,630);//400 width and 800 height  
		f.setLayout(null);//using no layout managers  
		f.setVisible(true);//making the frame visible  
	}
	
	public static boolean isParameterizavel(String parametro) {
		for(String p: not_parameterizaveis) {
			if(p.equals(parametro)) {
				return false;
			}
		}
		return true;
	}
	
	public static void initCloudToMongoGUI() {
		Iterator it = CloudToMongoIni.entrySet().iterator();
        int i  =  0;
		while(it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			if(pair.getKey().toString().startsWith("[") || pair.getKey().toString().startsWith("*") || !isParameterizavel(pair.getKey().toString())) {
				continue;
			}
	        JLabel label = new JLabel(pair.getKey().toString());
	        label.setBounds(20,25 + 30 * i, 180,30); 
	        i++;
	        f.add(label);
	        JTextField textField = new JTextField(pair.getValue().toString());
	        textField.setToolTipText(pair.getKey().toString());
	        textField.setBounds(220, 30*i, 250, 30);
	        textFields.add(textField);
	        f.add(textField);
		}
		JButton b=new JButton("Update");//creating instance of JButton  
		b.setBounds(130,100,80, 40);//x axis, y axis, width, height 
		b.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){  
			            for(JTextField jtf: textFields) {
			            	System.out.println("button pressed");
			            	String campo = jtf.getToolTipText();
			            	String valor = jtf.getText();
			            	atualizarCampoCloudToMongoIni(campo, valor);
			            	atualizaMysql(campo, valor);
			            }  
			        }  
		});   
			 
		b.setLocation(300, 540);
		f.add(b);//adding button in JFrame  
		
		JButton b1 =new JButton("MongoToMysql");//creating instance of JButton  
		b1.setBounds(130,100,160, 40);//x axis, y axis, width, height 
		b1.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){  
						textFields.clear();
						f.getContentPane().removeAll();
						f.repaint();
						initGUI();
			        }  
		});   
			 
		b1.setLocation(50,500);
		f.add(b1);        
		
		f.setSize(500,630);//400 width and 800 height  
		f.setLayout(null);//using no layout managers  
		f.setVisible(true);//making the frame visible  
	}
}
