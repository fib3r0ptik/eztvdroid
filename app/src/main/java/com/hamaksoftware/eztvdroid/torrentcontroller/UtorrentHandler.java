package com.hamaksoftware.eztvdroid.torrentcontroller;


import android.content.Context;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;



public class UtorrentHandler {
	String _host;
	String _username;
	String _password;
	int _port;
	boolean _useAuth;
	//private String _sessionCode;
	//private boolean _forceRenewSession;
	ArrayList<TorrentItem> torrents;
	public ViewFilter currentFilter= ViewFilter.ALL;
	
	public boolean lastStatusResult;

	final int FIELD_HASH = 0;
	final int FIELD_STATUS = 1;
	final int FIELD_NAME = 2;
	final int FIELD_SIZE = 3;
	final int FIELD_PERCENT = 4;
	final int FIELD_DOWNLOADED = 5;
	final int FIELD_UPLOADED = 6;
	final int FIELD_UPLOAD_SPEED = 8;
	final int FIELD_DOWNLOAD_SPEED = 9;
	final int FIELD_ETA = 10;
	final int FIELD_PEERS_CON = 12;
	final int FIELD_PEERS_ALL = 13;
	final int FIELD_SEEDERS_CON = 14;
	final int FIELD_SEEDERS_ALL = 15;
	final int FIELD_ORDER = 17;
	final int FIELD_REMAINING = 18;

	final int STATUS_STARTED = 1;
	final int STATUS_CHECKING=2;
	final int STATUS_STARTAFTERCHECK=4;
	final int STATUS_CHECKED=8;
	final int STATUS_ERROR=16;
	final int STATUS_PAUSED = 32;
	final int STATUS_QUEUED=64;
	final int STATUS_LOADED=128;
	
	
	
	final int PERCENT_COMPLETED = 1000;
	DefaultHttpClient cn;
	public String token;
	private Context _context;
	
	public enum STATUSCODE{
		STOPPED, CHECK_WAITING, CHECKING, DOWNLOAD_WAITING, DOWNLOADING, SEED_WAITING, SEEDING
	}
	
	
	public UtorrentHandler(Context context) {
		torrents = new ArrayList<TorrentItem>();
		_context = context;
	}

	public void setOptions(String host, String username, String password,
			int port, boolean useAuth) {
		_host = host;
		_username = username;
		_password = password;
		_port = port;
		_useAuth = useAuth;
		
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 5000);
		HttpConnectionParams.setSoTimeout(params, 5000); 
		cn = new DefaultHttpClient(params);
		
		if(useAuth){
			cn.getCredentialsProvider().setCredentials(new AuthScope(_host, _port), new UsernamePasswordCredentials(_username,_password));
		}

	}

	private boolean hasStatus(int status, int haystack) {
		return status == (status & haystack);
	}

	public void getToken() throws ClientProtocolException, IOException, ParserConfigurationException, FactoryConfigurationError, SAXException{
		if(token==null){
			String uri = "http://" + _host + ":" + _port + "/gui/token.html";
			HttpGet httpget = new HttpGet(uri);
			HttpResponse response = cn.execute(httpget);
			InputStream is = response.getEntity().getContent();
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = (Document) builder.parse(is);
			NodeList div = doc.getElementsByTagName("div");
			is.close();
			token=div.item(0).getChildNodes().item(0).getNodeValue();
		}
	}
	
	
	public void addTorrent(String link) throws MalformedURLException,
			IOException, ParserConfigurationException, SAXException,
			JSONException {
		getToken();
		String uri = "http://" + _host + ":" + _port + "/gui/?token="+token+"&action=add-url&s="+ URLEncoder.encode(link);
		HttpGet httpget = new HttpGet(uri);
		HttpResponse response = cn.execute(httpget);
		InputStream is = response.getEntity().getContent();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is), 8 * 1024);
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) sb.append(line);
		String result = sb.toString();
		JSONObject json = new JSONObject(result);
		int buildNo = Integer.parseInt(json.get("build").toString());
		lastStatusResult = (buildNo > 0);
		rd.close();
		is.close();
		
	}

	
	public class CompareByQueueOrder implements Comparator<TorrentItem> {
	    @Override
	    public int compare(TorrentItem o1, TorrentItem o2) {
	    	return o1.getOrder() - o2.getOrder();
	    }
	}
	
	public ArrayList<TorrentItem> getTorrents() throws IOException,
			JSONException, ParserConfigurationException, SAXException {

		boolean toAdd = false;

		//String uTorrentURI = "http://" + _host + ":" + _port + "/gui/";


		getToken();
		String uri = "http://" + _host + ":" + _port + "/gui/?token="+token+"&list=1";
		HttpGet httpget = new HttpGet(uri);
		HttpResponse response = cn.execute(httpget);
		InputStream is = response.getEntity().getContent();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is), 8 * 1024);
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) sb.append(line);
		String result = sb.toString();
		JSONObject json = new JSONObject(result);
		int buildNo = Integer.parseInt(json.get("build").toString());
		lastStatusResult = (buildNo > 0);
		rd.close();
		is.close();

		JSONArray torrentList = (JSONArray) json.getJSONArray("torrents");

		if (torrentList.length() > 0) {
			torrents.clear();
			for (int i = 0; i < torrentList.length(); i++) {
				JSONArray jitem = torrentList.getJSONArray(i);
				switch (currentFilter) {
				case COMPLETED:
					toAdd = (jitem.getInt(FIELD_PERCENT) >= PERCENT_COMPLETED);
					break;
				case PAUSED:
					toAdd = hasStatus(STATUS_PAUSED, jitem.getInt(FIELD_STATUS));
					break;
				case RUNNING:
					toAdd = (hasStatus(STATUS_STARTED,
							jitem.getInt(FIELD_STATUS))
							&& (jitem.getInt(FIELD_SEEDERS_CON) > 0)
							&& !hasStatus(STATUS_PAUSED,
									jitem.getInt(FIELD_STATUS)) && (jitem
							.getInt(FIELD_PERCENT) < PERCENT_COMPLETED));
					break;
				case QUEUED:
					toAdd = (hasStatus(STATUS_STARTED,
							jitem.getInt(FIELD_STATUS))
							&& (jitem.getInt(FIELD_ETA) <= 0)
							&& !hasStatus(STATUS_PAUSED,
									jitem.getInt(FIELD_STATUS)) && (jitem
							.getInt(FIELD_PERCENT) < PERCENT_COMPLETED));
					break;
				case SEEDING:
					toAdd = (jitem.getInt(FIELD_PERCENT) >= PERCENT_COMPLETED && jitem.getInt(FIELD_UPLOAD_SPEED) > 0);
					break;
				case ALL:
					toAdd = true;
					break;
				}

				if (toAdd) {
					TorrentItem item = new TorrentItem();
					item.setname(jitem.getString(FIELD_NAME));
					item.setHash(jitem.getString(FIELD_HASH));
					item.setStatus(jitem.getInt(FIELD_STATUS));
					item.setSize(jitem.getInt(FIELD_SIZE));
					item.setPercent(jitem.getInt(FIELD_PERCENT));
					item.setDownloaded(jitem.getInt(FIELD_DOWNLOADED));
					item.setUploaded(jitem.getInt(FIELD_UPLOADED));
					item.setDownloadSpeed(jitem.getInt(FIELD_DOWNLOAD_SPEED));
					item.setETA(jitem.getInt(FIELD_ETA));
					item.setUploadSpeed(jitem.getInt(FIELD_UPLOAD_SPEED));
					item.setSeedersCon(jitem.getInt(FIELD_SEEDERS_CON));
					item.setSeedersAll(jitem.getInt(FIELD_SEEDERS_ALL));
					item.setPeersCon(jitem.getInt(FIELD_PEERS_CON));
					item.setPeersAll(jitem.getInt(FIELD_PEERS_ALL));
					item.setOrder(jitem.getInt(FIELD_ORDER));
					item.setRemaining(jitem.getInt(FIELD_REMAINING));
					torrents.add(item);
				}

				toAdd = false;
			}

			
		}else{
			torrents=new ArrayList<TorrentItem>();
		}
		//Collections.reverse(torrents);
		Collections.sort(torrents,new CompareByQueueOrder());
		return torrents;
	}

	public HashMap<String, String> getClientTaskInfo(){
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			currentFilter = ViewFilter.ALL;
			getTorrents();
			
			double ttlDL = 0.0;
			double ttlUL = 0.0;
			int ttlDone = 0;
			int ttlActive = 0;
			int ttlIdle = 0;
			
			for(int i = 0; i < torrents.size();i++){
				TorrentItem item = torrents.get(i);
				ttlDL +=item.getDownloadSpeed();
				ttlUL +=item.getUploadSpeed();
				if(item.getPercent()>=PERCENT_COMPLETED){
					ttlDone++;
				}
				
				if(item.getPercent() < PERCENT_COMPLETED && hasStatus(STATUS_STARTED, item.getStatus()) 
						&& item.getSeedersCon() > 0 && !hasStatus(STATUS_PAUSED, item.getStatus())){
					ttlActive++;
				}
				
				if(item.getPercent() < PERCENT_COMPLETED && (hasStatus(STATUS_PAUSED, item.getStatus()) 
						|| item.getDownloadSpeed() <= 0 || item.getSeedersCon() <=0) ){
					ttlIdle++;
				}
			}
			
			map.put("ttldl", ttlDL+"");
			map.put("ttlul", ttlUL+"");
			map.put("ttldone", ttlDone+"");
			map.put("ttlactive", ttlActive+"");
			map.put("ttlidle", ttlIdle+"");
			
			return map;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//return null;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		
		
		return null;
	}
	
	
	private String getStringAction(TorrentAction action) {
		switch (action) {
		case FORCE_START:
			return "forcestart";
		case START:
			return "start";
		case STOP:
			return "stop";
		case PAUSE:
			return "pause";
		case UNPAUSE:
			return "unpause";
		case RECHECK:
			return "recheck";
		case REMOVE:
			return "remove";
		case REMOVEDATA:
			return "removedata";
		case REANNOUNCE:
			break;
		case SET_SETTINGS:
			break;
		case VERIFY:
			break;
		case QUEUEUP:
			return "queueup";
		case QUEUEDOWN:
			return "queuedown";
		default:
			break;
		}
		return "";
	}

	private void sendRequest(String uri) throws ClientProtocolException, IOException, ParserConfigurationException, 
			FactoryConfigurationError, SAXException, JSONException{
		getToken();
		HttpGet httpget = new HttpGet(uri);
		HttpResponse response = cn.execute(httpget);
		InputStream is = response.getEntity().getContent();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is), 8 * 1024);
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) sb.append(line);
		String result = sb.toString();
        System.out.println(result);
		JSONObject json = new JSONObject(result);
		String bn = json.get("build").toString();
		if (bn.equals("")) bn = "0";
		int buildNo = Integer.parseInt(bn);
		lastStatusResult = (buildNo > 0);
	}
	
	public void sendAction(TorrentAction action, String hash) throws ParserConfigurationException, SAXException, IOException {
        getToken();
		String uri = "http://" + _host + ":" + _port + "/gui/";
		uri += "?token=" + token + "&action=" + getStringAction(action)+ hash;
		try{
			sendRequest(uri);
		}catch(Exception e){
			lastStatusResult=false;
			e.printStackTrace();
			System.out.println(uri);
		}
	}

	public void setSettings(Map<String,String> data){
		String uri = "http://" + _host + ":" + _port + "/gui/?action=setsetting&token="+token+"&";
		String temp="";
		Iterator<Entry<String, String>> it=data.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String,String> me=it.next();
			temp+="s="+me.getKey()+"&v="+me.getValue()+"&";
		}
		uri+=temp;
		System.out.println(uri);
		try{
			sendRequest(uri);
		}catch(Exception e){
			e.printStackTrace();
			lastStatusResult=false;
		}
	}
	
	public boolean hasCompleted() {
		if (torrents == null)
			return false;
		if (torrents.size() <= 0)
			return false;
		boolean found = false;
		for (int i = 0; i < torrents.size(); i++) {
			if (torrents.get(i).getPercent() >= 1000) {
				found = true;
				break;
			}
		}
		return found;
	}

}
