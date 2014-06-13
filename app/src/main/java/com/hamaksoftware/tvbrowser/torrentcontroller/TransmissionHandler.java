package com.hamaksoftware.tvbrowser.torrentcontroller;


import android.content.Context;

import com.hamaksoftware.tvbrowser.utils.AppPref;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;



public class TransmissionHandler {
	private static final String headerText = "X-Transmission-Session-Id:";
	private String _host = "";
	private String _username = "";
	private String _password = "";
	private int _port = 0;
	private boolean _isUsingAuth = false;
	private String _sessionCode = "";
	private ArrayList<TorrentItem> torrents;
	public ViewFilter currentFilter= ViewFilter.ALL;
	public boolean lastStatusResult;
	private Context _context;
	private AppPref pref;
	
	public enum STATUSCODE{
		STOPPED, CHECK_WAITING, CHECKING, DOWNLOAD_WAITING, DOWNLOADING, SEED_WAITING, SEEDING
	}
	
	public TransmissionHandler(Context context){
		_context = context;
		pref = new AppPref(context);
		torrents = new ArrayList<TorrentItem>();
	}
	
	public void setOptions(String host, String username, String password,
			int port, boolean isUsingAuth) {
		_host = host;
		_username = username;
		_password = password;
		_port = port;
		_isUsingAuth = isUsingAuth;

	}

	public boolean addTorrent(String fileURI) throws IOException,
			JSONException {
		try {
			String code = getCode();
			HttpParams params = new BasicHttpParams();
		    HttpConnectionParams.setConnectionTimeout(params, pref.getConnectionTimeout()*1000); //make this a settings
		    HttpConnectionParams.setSoTimeout(params, pref.getRequestTimeout()*1000);		 // make this a settings
			DefaultHttpClient httpclient = new DefaultHttpClient(params);
			if (_isUsingAuth)
				httpclient.getCredentialsProvider().setCredentials(
						new AuthScope(_host, _port),
						new UsernamePasswordCredentials(_username, _password));
			String _endpointURI = "http://" + _host + ":" + _port
					+ "/transmission/rpc";
			String data = "{\"method\":\"torrent-add\",\"arguments\":{\"filename\":\""
					+ fileURI + "\"}}";
			HttpPost httppost = new HttpPost(_endpointURI);
			httppost.addHeader("X-Transmission-Session-Id", code);
			StringEntity se = new StringEntity(data);
			httppost.setEntity(se);
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			int statusCode = response.getStatusLine().getStatusCode();
			entity.consumeContent();
			lastStatusResult=(statusCode == 200);
			return lastStatusResult;
		} catch (Exception e) {
			return false;
		}

	}

	public String getCode() throws IOException,ConnectTimeoutException {
		if (_sessionCode.length() <= 0) {
			String _endpointURI = "http://" + _host + ":" + _port
					+ "/transmission/rpc";
			HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(params, 10000);
			DefaultHttpClient httpclient = new DefaultHttpClient(params);

			if (_isUsingAuth) {
				httpclient.getCredentialsProvider().setCredentials(
						new AuthScope(_host, _port),
						new UsernamePasswordCredentials(_username,
								_password));
			}
			HttpGet httpget = new HttpGet(_endpointURI);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					entity.getContent()), 8 * 1024);
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			rd.close();
			String result = sb.toString();
			entity.consumeContent();
			int indexFirst = result.indexOf("<code>");
			int indexLast = result.indexOf("</code>");
			String extract = result.substring(indexFirst + 6, indexLast);
			return extract.substring(headerText.length()).trim();
		} else {
			return _sessionCode;
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
	
	
	
	public class CompareByQueueOrder implements Comparator<TorrentItem> {
	    @Override
	    public int compare(TorrentItem o1, TorrentItem o2) {
	    	return o1.getOrder() - o2.getOrder();
	    }
	}
	
	public ArrayList<TorrentItem> getTorrents() throws IOException, JSONException {
		boolean toAdd=false;

			String code = getCode();
			String data = "{\"arguments\":{\"fields\":[\"hashString\",\"status\",\"name\",\"totalSize\",\"percentDone\",\"downloadedEver\",\"uploadedEver\",\"rateUpload\",\"rateDownload\",\"eta\",\"peersConnected\",\"peersSendingToUs\",\"id\"]},";
			data += "\"method\":\"torrent-get\"}";
			DefaultHttpClient httpclient = new DefaultHttpClient();
			if (_isUsingAuth) {
				httpclient.getCredentialsProvider().setCredentials(
						new AuthScope(_host, _port),
						new UsernamePasswordCredentials(_username, _password));
			}
			String _endpointURI = "http://" + _host + ":" + _port
					+ "/transmission/rpc";

			HttpPost httppost = new HttpPost(_endpointURI);
			httppost.addHeader("X-Transmission-Session-Id", code);
			StringEntity se = new StringEntity(data);
			httppost.setEntity(se);

			// Execute
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						entity.getContent()), 8 * 1024);
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = rd.readLine()) != null)
					sb.append(line);
				rd.close();
				String result = sb.toString();
				JSONObject json = new JSONObject(result);
				JSONObject args = json.getJSONObject("arguments");
				JSONArray torrentList = args.getJSONArray("torrents");
				entity.consumeContent();
				
				if (torrentList.length() > 0) {
					torrents.clear();
					for (int i = 0; i < torrentList.length(); i++) {
						JSONObject jitem = torrentList.getJSONObject(i);
						

                        switch(currentFilter){
                            case COMPLETED:
                               toAdd=(jitem.getInt("status")==6 || jitem.getInt("status")==5);
                               break;
                            case SEEDING:
                                toAdd=(jitem.getInt("status")==6);
                                break;  
                            case PAUSED:
                            	toAdd=(jitem.getInt("status")==16);
                                break;
                            case RUNNING:
                            	toAdd=(jitem.getInt("status")==4);
                                break;
                            case QUEUED:
                            	toAdd=(jitem.getInt("status")==2);
                                break;
                            case ALL:
                            	toAdd=true;
                                break;
                        }

						if (toAdd) {
                            TorrentItem item=new TorrentItem();
                            item.setname(jitem.getString("name"));
                            item.setHash(jitem.getString("hashString"));
                            item.setStatus(jitem.getInt("status"));
                            item.setSize(jitem.getInt("totalSize"));
                            int percent=(int)(jitem.getDouble("percentDone")*1000);
                            item.setPercent(percent);
                            item.setDownloaded(jitem.getInt("downloadedEver"));
                            item.setUploaded(jitem.getInt("uploadedEver"));
                            item.setDownloadSpeed(jitem.getInt("rateDownload"));
                            item.setETA(jitem.getInt("eta"));
                            item.setUploadSpeed(jitem.getInt("rateUpload"));
                            //item.setSeedersCon(jitem.getInt("seeders"));
                            item.setSeedersCon(0);
                            item.setSeedersAll(0);
                            //item.setSeedersAll(jitem.getInt("seeders_all"));
                            item.setPeersCon(jitem.getInt("peersSendingToUs"));
                            item.setPeersAll(jitem.getInt("peersConnected"));
                            item.setOrder(0);
                            int remaining=jitem.getInt("totalSize")-jitem.getInt("downloadedEver");
                            item.setRemaining(remaining);
                            torrents.add(item);
						}

						toAdd = false;
					}
					if(torrents.size()>0) Collections.sort(torrents,new CompareByQueueOrder());
				}else{
					torrents=new ArrayList<TorrentItem>();
				}

			}else{
				torrents=new ArrayList<TorrentItem>();
			}
			return torrents;

	}

	public boolean setSettings(Map<String,String> args) throws ConnectTimeoutException, IOException, JSONException{
		String code=getCode();
		DefaultHttpClient httpclient = new DefaultHttpClient();
		if (_isUsingAuth) {
			httpclient.getCredentialsProvider().setCredentials(
					new AuthScope(_host, _port),
					new UsernamePasswordCredentials(_username, _password));
		}
		String _endpointURI = "http://" + _host + ":" + _port
				+ "/transmission/rpc";
		
		
		Iterator<Entry<String, String>> it=args.entrySet().iterator();
		String temp="";
		while(it.hasNext()){
			Map.Entry<String,String> me=it.next();
			temp+="\""+me.getKey()+"\":"+me.getValue()+",";
		}
		temp=temp.substring(0,temp.length()-1);
		
		//String data = "{\"method\":\"torrent-set\",\"arguments\":{\"ids\":[],"+temp+"}}";
		String data="{\"method\":\"session-set\",\"arguments\":{"+temp+"}}";
		
		System.out.println(data);
		
		HttpPost httppost = new HttpPost(_endpointURI);
		httppost.addHeader("X-Transmission-Session-Id", code);
		StringEntity se = new StringEntity(data);
		httppost.setEntity(se);

		// Execute

		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					entity.getContent()), 8 * 1024);
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null)
				sb.append(line);
			rd.close();
			String result = sb.toString();
			JSONObject json = new JSONObject(result);
			entity.consumeContent();
			return json.getString("result").equals("success");
		}
		return false;
	}
	
	public boolean sendAction(TorrentAction action, String hash) throws IOException,
			JSONException {
		String code = getCode();
		String data = "";
		//hash = hash.substring(1,hash.length());
		switch (action) {
		case VERIFY:
			data = "{\"method\":\"torrent-verify\",\"arguments\":{\"ids\":["
					+ hash + "]}}";
			break;
		case START:
			data = "{\"method\":\"torrent-start\",\"arguments\":{\"ids\":["
					+ hash + "]}}";
			break;
		case STOP:
			data = "{\"method\":\"torrent-stop\",\"arguments\":{\"ids\":["
					+ hash + "]}}";
			break;
		case REANNOUNCE:
			data = "{\"method\":\"torrent-reannounce\",\"arguments\":{\"ids\":["
					+ hash + "]}}";
			break;
		case REMOVE:
			data = "{\"method\":\"torrent-remove\",\"arguments\":{\"ids\":["
					+ hash + "]}}";
			break;
		case REMOVEDATA:
			data = "{\"method\":\"torrent-remove\",\"arguments\":{\"ids\":["
					+ hash + "]},\"delete-local-data\":true}";
			break;
		case FORCE_START:
			break;
		case PAUSE:
			break;
		case RECHECK:
			break;
		case SET_SETTINGS:
			break;
		case UNPAUSE:
			break;
		default:
			break;
		}

		DefaultHttpClient httpclient = new DefaultHttpClient();
		if (_isUsingAuth) {
			httpclient.getCredentialsProvider().setCredentials(
					new AuthScope(_host, _port),
					new UsernamePasswordCredentials(_username, _password));
		}
		String _endpointURI = "http://" + _host + ":" + _port
				+ "/transmission/rpc";

		HttpPost httppost = new HttpPost(_endpointURI);
		httppost.addHeader("X-Transmission-Session-Id", code);
		StringEntity se = new StringEntity(data);
		httppost.setEntity(se);

		// Execute
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					entity.getContent()), 8 * 1024);
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null)
				sb.append(line);
			rd.close();
			String result = sb.toString();
			JSONObject json = new JSONObject(result);
			entity.consumeContent();
			return json.getString("result").equals("success");
		}
		return false;
	}

}
