package com.tpm.mobile.android.exe.api.appium.utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;

import com.tpm.mobile.android.exe.api.appium.dao.AppiumResultUtility;
import com.tpm.mobile.android.utils.Reports;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

/**
 * 
 * @author raju.kondaru
 *
 */
public class AppiumProxy2 {
	private static final Logger log = Logger.getLogger(AppiumProxy2.class);
	private  boolean isAlive = true;
	private  boolean isStarted = false;
	private  boolean isError = false;
	private  String platformName;
	private  String platformVersion;
	private  String deviceName;
	private  String key;
	private  String deviceId;
	private  String session;
	private  AppiumResultUtility aru=null;
	private  String DEVICE_VIDEO_PATH;
	private  String command;
	private  String line;
	
	private  Reports reports;

	private  Integer imageNo = 1;
	private  Process pro = null;
	private  BufferedReader br = null;
	
	private  String  nodeConfig = null;
	private  String  appiumConfig = null;	
	private  ArrayList<String>  appiumLogs= null;	
/*	private  String  deviceUdid = null;
	private JSONObject app= null;*/
	private ArrayList<String> deviceLogs=null;
	private static boolean isStop= false;
	private static boolean logStop= false;
	private String appiumHost;
	private String appiumPort;
	
	
	
	
	/*public AppiumProxy(){
		
	}
	public AppiumProxy(String deviceUdid, JSONObject app){
		this.deviceUdid=deviceUdid;
		this.app=app;
	}*/
	@SuppressWarnings("unchecked")
	public void startProxy(final String deviceUdid, JSONObject app, String testName) throws UnknownHostException {
	//public  void main(String[] args) throws UnknownHostException {
		initialize() ;
		InetAddress inet=Inet4Address.getLocalHost();
		inet.getHostAddress();
		//--app D:\\apk\\instagram-9-5-0.apk --webhook localhost:9876 
		String appiumCommand= nodeConfig+" "+appiumConfig+" --address "+appiumHost+" --port "+appiumPort+"  --webhook localhost:9876 --session-override --platform-name Android --platform-version 23 --automation-name Appium --log-no-color";
		try {
			pro = new ProcessBuilder("cmd.exe", "/C", appiumCommand).start();
			//log.info(command);
			br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			line = br.readLine();
			
			appiumLogs = new ArrayList<String>();
			log.info("Appium logging Started");
		    while (isAlive) {
		    	if(line != null){
		    		if(!line.isEmpty()){
						//log.info(line);
						appiumLogs.add(line);
						if(line.contains("Appium REST http interface listener started on")){
							log.info(line);
							isStarted = true;
							aru= new AppiumResultUtility();
							
						} else if(!line.contains("desired") && !line.contains("null") ){
			            	//log.info(line);
			            	if(line.contains("Responding to client with success")  ){
			            		AppuimScreenshot screenshot = new AppuimScreenshot(deviceId, imageNo, session);
			            		screenshot.start();
			            		log.info(line);
			   					imageNo =1+imageNo;
			        
			      		 	} else if (line.contains("Responding to client with error")) {
			      		 		AppuimScreenshot screenshot = new AppuimScreenshot(deviceId, imageNo, session);
			            		screenshot.start();
			            		log.info(line);
			   				    isError = true;
			      		 	 	imageNo =1+imageNo;
							 }else if(line.contains("Creating new appium session")){
								log.info(line);
							 }
			            	
			            }else  if(line.contains("desired")  && line.contains("<-- GET")){ //
			            	//log.info(line);
			            	 String[] desiredResult = line.split(",");
			            	 for (int i = 0; i < desiredResult.length; i++) {
			            		 if(desiredResult[i].contains("platformVersion")){
			            			 String[] version=desiredResult[i].split(":");
			            			 if(platformVersion==null){
			            				 platformVersion = version[1].replace("\"", "");
				            			 //log.info("Platform Version :: "+platformVersion);
			            			 }
			            			 
			            		 } else if(desiredResult[i].contains("platformName")){
			            			 String[] platform=desiredResult[i].split(":");
			            			 if(platformName==null){
			            				 platformName = platform[1].replace("\"", "");
				            			 //log.info("Platform Name :: "+platformName);
			            			 }
			            			
			            		 } else if(desiredResult[i].contains("deviceName")){
			            			 String[] device=desiredResult[i].split(":");
			            			 if(deviceName==null){
			            				 deviceName =device[1].replace("\"", "");
				            			 //log.info("Device Name :: "+deviceName);
			            			 }
			            			
			            		 } else if(desiredResult[i].contains("api_key")){
			            			 String[] api_key=desiredResult[i].split(":");
			            			 if(key==null){
			            				 key= api_key[1].replace("\"", "");
				            			 log.info("API Key :: "+key);
			            			 }
			            			
			            		 } else if(desiredResult[i].contains("udid")){
			            			 String[] udid = desiredResult[i].split(":");
			            			 if(deviceId==null){
			            				 deviceId=udid[1].replace("\"", "");
				            			 log.info("UDID :: "+deviceId);
			            			 }
			            			
			            		 } else if(desiredResult[i].contains("sessionId")){
			            			 String[] sessionId=desiredResult[i].split(":");
			            			 if(session==null){
			            				 session=sessionId[1].replace("\"", "").replace("}", "");
				            			 log.info("Session ID :: "+session);
			            			 }
			            			
			            		 } 
								
							}
			            	
			  				if( isStarted && deviceId != null && deviceUdid.equalsIgnoreCase(deviceId) && session != null){
			  					try {
			  						reports = new Reports(deviceUdid, session);
				  					reports.startRecoding();
			  					}catch(Exception e){
			  						e.printStackTrace();
			  					}
			  					
			  					deviceLogs = new ArrayList<String>();
			  					new Thread(new Runnable() {
			  						public void run() {
			  							InputStream is = null;
		  						        InputStreamReader isr = null;
		  						        BufferedReader br =null;
		  						        Process p = null;
			  							 try {
			  								 	log.info("Device logging Started");
			  							        p = Runtime.getRuntime().exec("adb -s "+deviceUdid+" shell logcat");
			  							        is = p.getInputStream();
			  							        isr = new InputStreamReader(is);
			  							        br = new BufferedReader(isr);
			  							        String line;
			  							        while ((line = br.readLine()) != null) {
			  							        	if(!line.isEmpty()){
			  							        		//log.info(line);
			  							                deviceLogs.add(line);
			  								            if(isStop || isError){
			  								            	log.info("Device logging completed");
			  								            	break;
			  								            }
			  							        	}
			  							        }
			  							        
			  							    } catch (IOException  e) {
			  							        log.error(e);
			  							    } finally {
			  							    	try {
			  							    		if (br!=null) {
			  											br.close();
			  										}
			  										if (isr!=null) {
			  											isr.close();
			  										}
			  										if (is!=null){
			  											is.close();
			  										}
			  										if (p!=null){
			  											p.destroy();
			  										}
			  										
			  							    	} catch (IOException e) {
			  										// TODO Auto-generated catch block
			  										e.printStackTrace();
			  									}
			  							    	logStop = true;
			  								}
			  						}
			  					}).start();
	  							isStarted = false;
			  	  			}
			            	
			            	
			            }else  if(line.contains("<-- DELETE") ||  isError){
			            	
			            	String lines;
							DEVICE_VIDEO_PATH = "/sdcard/"+session+".mp4";//
							command = "adb -s "+deviceUdid+" pull "+DEVICE_VIDEO_PATH+" "+FileUtils.getUserDirectory()+"\\AppData\\Local\\Temp";
							try {
								if(reports!=null){
									//report.stop();
									reports.stopRecording();
									isStop= true;
								}
								Thread.sleep(5000);
								Process p = null;
								BufferedReader r= null;
								try{
									p = new ProcessBuilder("cmd.exe", "/C", command).start();
									log.info(command);
									r = new BufferedReader(new InputStreamReader(p.getInputStream()));
									
									while((lines=r.readLine()) != null ){
										if(!lines.isEmpty()){
											if(lines.contains("100%")){
												//log.info(lines);
												String dalateCommand= "adb -s "+deviceUdid+" shell rm -f /sdcard/"+session+".mp4";
												Process deleteProcess = null;
												BufferedReader deleteBufferedReader=null;
												String deleteResults=null;    
												try {
													deleteProcess = new ProcessBuilder("cmd.exe", "/C", dalateCommand).start();
													log.info(dalateCommand);
													deleteBufferedReader = new BufferedReader(new InputStreamReader(deleteProcess.getInputStream()));
													
													while((deleteResults=deleteBufferedReader.readLine()) != null ){
														if(!deleteResults.isEmpty()){
															if(deleteResults.contains("100%")){
																//log.info(lines);
																
																break;
																
															}
															
														}
														
													}
													
												} catch ( IOException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												} finally {
													
													if(deleteBufferedReader != null){
														deleteBufferedReader.close();
													}
													if(deleteProcess != null){
														deleteProcess.destroy();
													}
													
													
												}
												break;
											}
											
										}
										
									}
								}catch(Exception e){
									log.error(e);
								}finally {
									
									if(r!=null){
										r.close();
									}
									if(p!=null){
										p.destroy();
									}
									Process killProcess=null;
									BufferedReader killBuffer=null;
									String killResults=null;  
									try{
										killProcess=new ProcessBuilder("cmd.exe", "/C", "taskkill /F /IM node.exe").start();
										killBuffer = new BufferedReader(new InputStreamReader(killProcess.getInputStream()));
										while((killResults=killBuffer.readLine()) != null ){
											if(!killResults.isEmpty()){
												if(killResults.contains("SUCCESS")){
													log.info("Appium Server Closed Normally");
													break;
												}
											}
										}
									}catch(Exception e){
										log.error(e);
									}finally {
										
										if(killBuffer != null){
											killBuffer.close();	
										}
										if(killProcess != null){
											killProcess.destroy();
										}
										
									}
									
								}
						
							} catch ( IOException | InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}finally {
								isAlive = false;
								log.info("Appium logging completed");
							}
						
			            	
			            } 
						
					}
		    	}
		     	
		    	
		     	 line = br.readLine();	
				
			}
			if(!appiumLogs.isEmpty()){
				JSONObject logs= new JSONObject();
				while(!logStop){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				logs.put("devicelog", deviceLogs);
				logs.put("appiumLog", appiumLogs);
				app.put("logs", logs);
				if(reports!=null){
					aru.saveVideo(FileUtils.getUserDirectory()+"\\AppData\\Local\\Temp\\"+session+".mp4",deviceId ,app, session, isError, testName);
					//report.resume();
				}
				
				/*for (int i=0; i<appiumLogs.size();i++) {
					if(appiumLogs.get(i).contains("Device launched! Ready for commands")){
					}else if(appiumLogs.get(i).contains("Pushing command to appium work queue") && !appiumLogs.get(i).contains("wake") && !appiumLogs.get(i).contains("getDataDir") && !appiumLogs.get(i).contains("compressedLayoutHierarchy")){
						int j=1;
						ArrayList<String> stepLogs= new ArrayList<String>();
						try{
							while(!appiumLogs.get(i+j).contains("Responding to client with success") || !appiumLogs.get(i+j).contains("Responding to client with error")){
								//log.info(appiumLogs.get(i+j));
								stepLogs.add(appiumLogs.get(i+j));
								if(j<appiumLogs.size()){
									j++;
								}
								
							}
						}catch(IndexOutOfBoundsException e){
							
						}
						for (int a=0; a<stepLogs.size();a++) {
							
							String action=stepLogs.get(a).replace("info: [debug] [BOOTSTRAP] [debug] ", "");
						
								if(action!=null){
								//	log.info("Step: "+step+":: "  +action);
									
								}
						}
					}
					
				}*/
				
			}
		} catch ( IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(pro!=null){
					pro.destroy();
				}
				if(br!=null){
					br.close();
				}
				File fin = new File(FileUtils.getUserDirectory()+"\\AppData\\Local\\Temp");

				for (File file : fin.listFiles()) {
					try{
						FileDeleteStrategy.FORCE.delete(file);
					} catch(Exception e){
						
					}
				    
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	
	private void initialize() {
		Properties prop = new Properties();
		try {
			prop.load(AppiumProxy2.class.getClassLoader().getResourceAsStream("config.properties"));
				
			nodeConfig = prop.getProperty("NODE_CONFIG");
			appiumConfig = prop.getProperty("APPIUM_CONFIG");
			appiumHost = prop.getProperty("APPIUM_HOST");
			appiumPort = prop.getProperty("APPIUM_PORT") ;
			
		} catch (IOException e) {
			

		}
	}

/*
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			startProxy(deviceUdid , app);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
//
}
