package com.hamaksoftware.eztvdroid.utils;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;


import com.hamaksoftware.eztvdroid.R;
import com.hamaksoftware.eztvdroid.fragments.IAsyncTaskListener;
import com.hamaksoftware.eztvdroid.models.EZTVRow;
import com.hamaksoftware.eztvdroid.models.EZTVShowItem;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EZTVScraper {
	private HashMap<String,String> cookies = new HashMap<String,String>();
	
	//private Document doc;
	
	//public String URL_DOMAIN = "http://hamaksoftware.com/myeztv/api-beta.php";
    public String URL_DOMAIN = "http://eztv.it/";

	public String URL_LATEST_EPISODE = "sort/100/";
	public String URL_LATEST_EPISODE_NAVI = "page_";
	public String URL_LOGIN = "login/";
	public String URL_MYSHOWS = "myshows/";
	public String URL_SEARCH = "search/";
	public String URL_SHOWLIST = "showlist/";
	public String URL_ADD_SHOW = "myshows/$/add/";
	
	public String username;
	public String password;
	public String hashedpwd;
	public String sesId;
	public ArrayList<EZTVRow> items = new ArrayList<EZTVRow>(0);
	public ArrayList<EZTVRow> itemsCopy = new ArrayList<EZTVRow>(0);
	public ArrayList<EZTVShowItem> showsCopy = new ArrayList<EZTVShowItem>(0);
	public ArrayList<EZTVShowItem> shows = new ArrayList<EZTVShowItem>(0);
	
	public ArrayList<String> header;
	
	private static final int COL_TITLE = 1;
	private static final int COL_LINKS = 2;
	private static final int COL_ELAPSED = 3;
	
	private Context _context;
	private AppPref pref;
    private ShowHandler sh;
    
	private IAsyncTaskListener l;
	private final int MAX_SEARCH_RESULT = 200;
	


	public EZTVScraper(Context context){
		_context = context;
		pref = new AppPref(context);
		sh = new ShowHandler(context);
		
		if(pref.getUseProxy()) URL_DOMAIN = pref.getProxySite();

		header = new ArrayList<String>(0);
		
		username = pref.getEZTVUsername();
		password = pref.getEZTVPassword();
		hashedpwd = pref.getEZTVHashedPassword();
		sesId = pref.getEZTVRequestSesID();

		if(!username.equals("") && !hashedpwd.equals("") && !sesId.equals("")){
			cookies.put("username", username);
			cookies.put("password", hashedpwd);
			cookies.put("PHPSESSID", sesId);
		}
	}
	
	public void setAsyncTaskListener(IAsyncTaskListener l){
		this.l = l;
	}
	
	public void cloneItems(){
		itemsCopy = new ArrayList<EZTVRow>(items);
	    Collections.copy(itemsCopy, items);
	}
	
	public void cloneShowItems(){
		showsCopy = new ArrayList<EZTVShowItem>(shows);
	    Collections.copy(showsCopy, shows);
	}
	
	
	private void extractCookie(String cookie){
		String[] keypairs = cookie.split(";");
		
		for(int i = 0; i < keypairs.length;i++){
			String[] kp = keypairs[i].split("=");
			if(!cookies.containsKey(kp[0])) {
				cookies.put(kp[0], kp[1]);
				if(kp[0].equals("username")) pref.setEZTVUsername(kp[1]);
				if(kp[0].equals("password")) pref.setEZTVHashedPassword(kp[1]);
				if(kp[0].equals("PHPSESSID")) pref.setEZTVRequestSesID(kp[1]);
			}
		}
	}
	
	public boolean login(){
		Log.i("uri",URL_LOGIN);
		try{
			//Log.i("cookie size", cookies.size()+" - "+cookies.toString());
			if(cookies.size() > 1) return true;



			l.onTaskUpdateMessage("Logging in to EZTV...");
			
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, pref.getConnectionTimeout()*1000);
			HttpConnectionParams.setSoTimeout(httpParameters, pref.getRequestTimeout()*1000);
			
			HttpClient httpclient = new DefaultHttpClient(httpParameters);
			HttpPost post = new HttpPost(URL_DOMAIN + URL_LOGIN);
			
			List<NameValuePair> params = new ArrayList<NameValuePair>(3);
			params.add(new BasicNameValuePair("loginname", username));
			params.add(new BasicNameValuePair("password", password));
			params.add(new BasicNameValuePair("submit", "Login"));
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

			post.addHeader("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 5.1; en-GB; rv:1.8.1.6) Gecko/20070725 Firefox/3.5.0.1");

			HttpResponse response = httpclient.execute(post);
			l.onTaskUpdateMessage("Logging in to EZTV...done!");
	    	Header[] headersPost = response.getHeaders("Set-Cookie");
			for(Header h: headersPost) extractCookie(h.getValue());
			
			return (response.getStatusLine().getStatusCode() == 200);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return false;
		
	}
	

	
	public void search(String query,boolean byId){
        //Log.i("uri",URL_DOMAIN + URL_SEARCH);
		l.onTaskUpdateMessage(_context.getResources().getString(R.string.loader_working));
		String response = "";
		try{
            ArrayList<NameValuePair> param = new ArrayList<NameValuePair>(4);
            if(byId) param.add(new BasicNameValuePair("show_id", query));
            param.add(new BasicNameValuePair("byid", byId+""));
            param.add(new BasicNameValuePair("query", query));
            param.add(new BasicNameValuePair("method", "search"));
            response = Utility.getInstance(_context).doPostRequest(param);
            //Log.i("search",response);
            JSONObject jResponse = new JSONObject(response);
            if(jResponse.getInt("err") == 0){
            	JSONArray latest = jResponse.getJSONArray("data");
            	for(int i = 0; i < latest.length();i++){
            		JSONObject item = latest.getJSONObject(i);
            		EZTVRow row = new EZTVRow();
            		row.title = item.getString("title");
            		row.filesize = Utility.getFancySize(item.getLong("size"));
            		row.elapsed = Utility.getElapsed(item.getString("pubdate"));
            		row.showId = Integer.parseInt(item.getString("show_id"));
            		
            		JSONArray jLinks = item.getJSONArray("links");
            		for(int j = 0; j < jLinks.length();j++){
            			row.links.add(jLinks.getString(j));
            		}
            		row.isFavorite = isFavorite(row.showId);
            		items.add(row);
            	}
            }else{
            	Log.i("err",response);
            }
		}catch (Exception e) {
			Log.i("err",response);
		}
	}
	

	
	
	public void getShows(boolean forced){
		Log.i("uri",URL_DOMAIN + URL_SHOWLIST);
		 shows = new ArrayList<EZTVShowItem>(0);
		//l.onTaskUpdateMessage(_context.getResources().getString(R.string.loader_working));
		 l.onTaskUpdateMessage("Reloading/Caching shows...");
        //ShowHandler sh = new ShowHandler(_context);
		int count = sh.getCount();
		try {
			
            if(forced || count<= 0){
            	int ctr = 0;
                ArrayList<NameValuePair> param = new ArrayList<NameValuePair>(1);
                param.add(new BasicNameValuePair("method", "getShows"));
                String response = Utility.getInstance(_context).doPostRequest(param);
                JSONArray jShows = new JSONArray(response);
                Log.i("api", "ttl shows:"+ jShows.length());
                //my shows
                param = new ArrayList<NameValuePair>(3);
                param.add(new BasicNameValuePair("dev_id",pref.getDeviceId()));
                param.add(new BasicNameValuePair("method", "getMyshows"));
                response = Utility.getInstance(_context).doPostRequest(param);
                JSONArray myShows = new JSONArray(response);
                Log.i("api", "ttl my shows:"+ myShows.length());
                
                l.onTaskProgressMax(jShows.length());
                sh.deleteAll();
                for(int i = 0;  i < jShows.length();i++){
                	EZTVShowItem show = new EZTVShowItem();
                	JSONObject item = jShows.getJSONObject(i);
                	show.title = item.getString("title");
                	show.status = item.getString("status");
                	show.showId = Integer.parseInt(item.getString("id"));
                	
                    String append = null;
                    for(int j = 0; j < myShows.length();j++){
                        JSONObject obj = myShows.getJSONObject(j);
                        int id = obj.getInt("id");
                        if(id == show.showId){
                            append = _context.getResources().getString(R.string.tab_show_myshow);
                            show.isSubscribed = true;
                            break;
                        }else{
                            append = "";
                        }
                    }
                	
                	sh.addShow(show);
                	shows.add(show);
                    l.onTaskProgressUpdate(ctr);
                    ctr++;
                }
                                
            }else{
                shows = sh.getAllShows();
            }
			cloneShowItems();
		} catch (JSONException e) {
			Log.e("err", e.getMessage());
		}finally{
			sh.close();
		}

	}
	
	public boolean isFavorite(int showId){
		boolean isFav = false;
		if(showId==187) return false;
		try{
			EZTVShowItem row = sh.getShow(showId);
			isFav = row.isSubscribed;
		}catch(Exception e){
			return false;
		}
		
		return isFav;
	}
	
	public void loadLatestEpisodes(boolean forced, int page){
		try{
            ArrayList<NameValuePair> param = new ArrayList<NameValuePair>(2);
            param.add(new BasicNameValuePair("page", page+""));
            param.add(new BasicNameValuePair("method", "getLatest"));
            String response = Utility.getInstance(_context).doPostRequest(param);
            JSONObject jResponse = new JSONObject(response);
            if(jResponse.getInt("err") == 0){
            	JSONArray latest = jResponse.getJSONArray("data");
            	for(int i = 0; i < latest.length();i++){
            		JSONObject item = latest.getJSONObject(i);
            		if(!item.getString("show_id").equals("add")){
                		EZTVRow row = new EZTVRow();
                		row.title = item.getString("title");
                		row.filesize = Utility.getFancySize(item.getLong("size"));
                		row.elapsed = Utility.getElapsed(item.getString("pubdate"));
                		row.showId = Integer.parseInt(item.getString("show_id"));
                		
                		JSONArray jLinks = item.getJSONArray("links");
                		for(int j = 0; j < jLinks.length();j++){
                			row.links.add(jLinks.getString(j));
                		}
                		
                		row.isFavorite = isFavorite(row.showId);
                		items.add(row);
            		}
            	}
            	//cloneItems();
            }else{
            	Log.i("err",response);
            }
		}catch (Exception e) {
			Log.e("err",e.getMessage());
		}
	}


}
