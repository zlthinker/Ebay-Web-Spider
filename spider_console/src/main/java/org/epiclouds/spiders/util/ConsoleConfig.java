package org.epiclouds.spiders.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ConsoleConfig {
	private static Map<String,Para> paras=new ConcurrentHashMap<String,Para>();
	

	
	private volatile static String mongo_host="127.0.0.1";
	private volatile static Integer mongo_port=27017;
	private volatile static String mongo_user="yuanshuju";
	private volatile static String mongo_pass="123Yuanshuju456";
	private volatile static String mongo_authticateDatabase="admin";
	
	private volatile static String store_database="spider_console";
	private volatile static String meta_table="spider_meta";
	
	

	private volatile static  String rabbit_host="106.3.38.50";
	private volatile static  String rabbit_username="yuanshuju";
	private volatile static  String rabbit_password="123Yuanshuju456";
	private volatile static  String receive_queue_name="console";
	


	private volatile static  Integer jettyport = 8001;
	
	private volatile static Integer receive_timeout=20000;

	
	public static String getStore_database() {
		return store_database;
	}
	public static void setStore_database(String store_database) {
		ConsoleConfig.store_database = store_database;
	}
	
	public static String getMeta_table() {
		return meta_table;
	}
	public static void setMeta_table(String meta_table) {
		ConsoleConfig.meta_table = meta_table;
	}
	public static String getMongo_host() {
		return mongo_host;
	}
	public static void setMongo_host(String mongo_host) {
		ConsoleConfig.mongo_host = mongo_host;
	}
	public static Integer getMongo_port() {
		return mongo_port;
	}
	public static void setMongo_port(Integer mongo_port) {
		ConsoleConfig.mongo_port = mongo_port;
	}
	public static void setMongo_port(String mongo_port) {
		ConsoleConfig.mongo_port = Integer.parseInt(mongo_port);
	}

	public static String getRabbit_host() {
		return rabbit_host;
	}
	public static void setRabbit_host(String rabbit_host) {
		ConsoleConfig.rabbit_host = rabbit_host;
	}
	public static String getRabbit_username() {
		return rabbit_username;
	}
	public static void setRabbit_username(String rabbit_username) {
		ConsoleConfig.rabbit_username = rabbit_username;
	}
	public static String getRabbit_password() {
		return rabbit_password;
	}
	public static void setRabbit_password(String rabbit_password) {
		ConsoleConfig.rabbit_password = rabbit_password;
	}
	public static String getReceive_queue_name() {
		return receive_queue_name;
	}
	public static void setReceive_queue_name(String receive_queue_name) {
		ConsoleConfig.receive_queue_name = receive_queue_name;
	}
	public static Map<String,Para> getParas() {
		return paras;
	}
	public static void setParas(Map<String,Para> paras) {
		ConsoleConfig.paras = paras;
	}
	
	public static String getMongo_user() {
		return mongo_user;
	}
	public static void setMongo_user(String mongo_user) {
		ConsoleConfig.mongo_user = mongo_user;
	}
	public static String getMongo_pass() {
		return mongo_pass;
	}
	public static void setMongo_pass(String mongo_pass) {
		ConsoleConfig.mongo_pass = mongo_pass;
	}
	public static String getMongo_authticateDatabase() {
		return mongo_authticateDatabase;
	}
	public static void setMongo_authticateDatabase(String mongo_authticateDatabase) {
		ConsoleConfig.mongo_authticateDatabase = mongo_authticateDatabase;
	}
	public static Integer getJettyport() {
		return jettyport;
	}
	public static void setJettyport(Integer jettyport) {
		ConsoleConfig.jettyport = jettyport;
	}
	public static void setJettyport(String jettyport) {
		ConsoleConfig.jettyport = Integer.parseInt(jettyport);
	}
	public static Integer getReceive_timeout() {
		return receive_timeout;
	}
	public static void setReceive_timeout(Integer receive_timeout) {
		ConsoleConfig.receive_timeout = receive_timeout;
	}
	public static void setReceive_timeout(String receive_timeout) {
		ConsoleConfig.receive_timeout = Integer.parseInt(receive_timeout);
	}

	
}
