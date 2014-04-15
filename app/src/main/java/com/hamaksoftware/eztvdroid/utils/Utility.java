package com.hamaksoftware.eztvdroid.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.provider.ContactsContract.Profile;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

public class Utility {
	private static Utility obj = null;
	
	
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private final static String URL_BACKEND = "http://hamaksoftware.com/myeztv/api-beta.php";
    private final static String SENDER_ID = "759831429666";//"574692807042"; 
    private final static String TAG = "gcm";


    private static Context _context;
    private static AppPref pref;
	
	private Utility(){}
	
	public static Utility getInstance(Context context){
		if(obj==null){
			obj = new Utility();
			pref = new AppPref(context);
		}
		_context = context;
		return obj;
	}
	
			
	public Drawable drawable_from_url(String url) throws 
	   java.net.MalformedURLException, IOException {
	   return Drawable.createFromStream(((InputStream)
	      new java.net.URL(url).getContent()), null);
	}
	
	private static int getAppVersion() {
	    try {
	        PackageInfo packageInfo = _context.getPackageManager()
	                .getPackageInfo(_context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        //throw new RuntimeException("Could not get package name: " + e);
	    }
	    return 0;
	}
	
	public String getRegistrationId() {
	    String registrationId = pref.getDeviceRegId();
	    if (registrationId.equals("")) {
	        Log.i(TAG, "Registration not found.");
	        return "";
	    }
	    // Check if app was updated; if so, it must clear the registration ID
	    // since the existing regID is not guaranteed to work with the new
	    // app version.
	    int registeredVersion = pref.getAppRegVersion();
	    int currentVersion = getAppVersion();
	    if (registeredVersion != currentVersion) {
	        Log.i(TAG, "App version changed.");
	        pref.setDeviceRegId("");
	        return "";
	    }
	    return registrationId;
	}
	
	
	public void markDownload(String title,int showId){
		Pattern p = Pattern.compile("(.*?)S?(\\d{1,2})E?(\\d{2})(.*)",Pattern.DOTALL);
		Matcher matcher = p.matcher(title);
		String s = "";
		String e = "";
		String t = "";
		if(matcher.find()){
			t = matcher.group(1);
			s = matcher.group(2);
			e = matcher.group(3);
		}
		
		
		ArrayList<NameValuePair> param = new ArrayList<NameValuePair>(6);
        param.add(new BasicNameValuePair("dev_id", pref.getDeviceId()));
        param.add(new BasicNameValuePair("show_id", showId+""));
        param.add(new BasicNameValuePair("title", t));
        param.add(new BasicNameValuePair("s", s));
        param.add(new BasicNameValuePair("e", e));
        param.add(new BasicNameValuePair("method", "markDownload"));
        
        Log.i("uri",doPostRequest(param));
        
	}
	
	public String doPostRequest(ArrayList<NameValuePair> params){
		try{
			
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, pref.getConnectionTimeout()*1000);
			HttpConnectionParams.setSoTimeout(httpParameters, pref.getRequestTimeout()*1000);
			
			HttpClient httpclient = new DefaultHttpClient(httpParameters);
			HttpPost post = new HttpPost(URL_BACKEND);
			
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			
			//new String(MessageDigest.getInstance("MD5").digest((getAppVersion()+SENDER_ID).getBytes("UTF-8")));

			HttpResponse response = httpclient.execute(post);
			InputStream is = response.getEntity().getContent();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is), 8 * 1024);
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) sb.append(line);

            System.out.println(sb.toString());

			return sb.toString();

		}catch(UnsupportedEncodingException e){
			Log.i("UE",e.getMessage());
		}catch(ClientProtocolException e){
			Log.i("CP",e.getMessage());
		}catch(IOException e){
			Log.i("IO",e.getMessage()+params.get(params.size()-1).getValue());
		}
		
		return "";
	}
	
	public void registerInBackground() {
		
		new AsyncTask<Void, Void, String>(){
			@Override
			protected String doInBackground(Void... params) {
				GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(_context);
	            String msg = "";
	            try {
	                String regId = gcm.register(SENDER_ID);
	                //msg = "Device registered, registration ID=" + regId;
	                
	                ArrayList<NameValuePair> data = new ArrayList<NameValuePair>(3);
	                data.add(new BasicNameValuePair("dev_id", pref.getDeviceId()));
	        		data.add(new BasicNameValuePair("reg_id", regId));
	        		data.add(new BasicNameValuePair("method", "regDevice"));
	        		Utility.getInstance(_context).doPostRequest(data);
	                pref.setDeviceRegId(regId);
	            } catch (IOException ex) {
	                msg = "Error :" + ex.getMessage();
	            }
	            return msg;
			}



		}.execute();

	}
	
	
	public void saveProfiles() {
    	ProfileHandler phandler = new ProfileHandler(_context);
    	final ArrayList<ClientProfile> profiles = phandler.getAllProfiles();
		for(int i = 0; i < profiles.size();i++){
			final ClientProfile p = profiles.get(i);
			new AsyncTask<Void, Void, String>(){
				@Override
				protected String doInBackground(Void... params) {
					String msg = "";
		            ArrayList<NameValuePair> data = new ArrayList<NameValuePair>(8);
					data.add(new BasicNameValuePair("dev_id", pref.getDeviceId()));
					data.add(new BasicNameValuePair("method", "saveProfile"));
					data.add(new BasicNameValuePair("name", p.name));
					data.add(new BasicNameValuePair("host", p.host));
					data.add(new BasicNameValuePair("port", p.port+""));
					data.add(new BasicNameValuePair("use_auth", p.useAuth+""));
					data.add(new BasicNameValuePair("uid", p.username));
					data.add(new BasicNameValuePair("pwd", p.password));
					Utility.getInstance(_context).doPostRequest(data);
		            return msg;
				}
			}.execute();
		}
		


	}
	
	
	public void unRegisterInBackground() {
		
		new AsyncTask<Void, Void, String>(){
			@Override
			protected String doInBackground(Void... params) {
				GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(_context);
	            String msg = "";
	            try {
	                gcm.unregister();	                
	                ArrayList<NameValuePair> data = new ArrayList<NameValuePair>(2);
	                data.add(new BasicNameValuePair("dev_id", pref.getDeviceId()));
	        		data.add(new BasicNameValuePair("method", "unregDevice"));
	        		Utility.getInstance(_context).doPostRequest(data);
	                pref.setDeviceRegId("");
	            } catch (IOException ex) {
	                msg = "Error :" + ex.getMessage();
	            }
	            return msg;
			}



		}.execute();

	}
	
	public String getProperTitle(String desc) {
		Pattern p = Pattern.compile("(.*?)S?(\\d{1,2})E?(\\d{2})(.*)",Pattern.DOTALL);
		Matcher matcher = p.matcher(desc);
		if(matcher.find()){
			return matcher.group(1);
		}else{
			p = Pattern.compile("^(19|20)\\d{2}$",Pattern.DOTALL);
			matcher = p.matcher(desc);
			return matcher.find()?matcher.group(1): desc.substring(0,15);
		}
		
	}
	
	public long[] getTimeDifference(Date d1, Date d2) {
		long[] result = new long[3];
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("EST"));
		cal.setTime(d1);

		long t1 = cal.getTimeInMillis();
		cal.setTime(d2);

		long diff = Math.abs(cal.getTimeInMillis() - t1);
		final int ONE_DAY = 1000 * 60 * 60 * 24;
		final int ONE_HOUR = ONE_DAY / 24;
		final int ONE_MINUTE = ONE_HOUR / 60;
		// final int ONE_SECOND = ONE_MINUTE / 60;

		long d = diff / ONE_DAY;
		diff %= ONE_DAY;

		long h = diff / ONE_HOUR;
		diff %= ONE_HOUR;

		long m = diff / ONE_MINUTE;
		diff %= ONE_MINUTE;
		/*
		 * long s = diff / ONE_SECOND; long ms = diff % ONE_SECOND;
		 */
		result[0] = d;
		result[1] = h;
		result[2] = m;
		// result[3] = s;
		// result[4] = ms;

		return result;
	}
	
	
	public String getFancySizeText(double ttlUL){
		final long KILOBYTE = 1024L;
		final long MEGABYTE = 1024L * 1024L;
		final long GIGABYTE = 1024L * 1024L * 1024L;
		
		String sd = "";
		DecimalFormat Currency = new DecimalFormat("#0.00");
		if(ttlUL / KILOBYTE < KILOBYTE){
			sd = Currency.format((double) (ttlUL / KILOBYTE)) + " KB";
		}else if ((ttlUL / MEGABYTE) > KILOBYTE) {
			sd = Currency.format((double) (ttlUL / GIGABYTE)) + " GB";
		} else {
			sd = Currency.format((double) (ttlUL / MEGABYTE)) + " MB";
		}
		
		return sd + "/s";
	}
	
	public int countElement(String xpath, InputSource source){
		try{
			DocumentBuilderFactory domFactory =  DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true); 
		    DocumentBuilder builder = domFactory.newDocumentBuilder();
		    Document doc = builder.parse(source);
		    XPath xp = XPathFactory.newInstance().newXPath();
		       // XPath Query for showing all nodes value
		    XPathExpression expr = xp.compile(xpath);
		    NodeList result = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		    int len = result.getLength();
		    return len;
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;
	}


    public static void showDialog(Context c,String title, String msg, String positiveBtnCaption,
                                  String negativeBtnCaption, boolean isCancelable,
                                  final iDialog target) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);

        builder.setTitle(title).setMessage(msg).setCancelable(isCancelable)
                .setPositiveButton(positiveBtnCaption, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        if(target !=null) target.PositiveMethod(dialog, id);
                    }

                }).setNegativeButton(negativeBtnCaption, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if(target !=null) target.NegativeMethod(dialog, id);
            }
        });

        AlertDialog alert = builder.create();
        alert.setCancelable(isCancelable);
        alert.show();
        if (isCancelable) {
            alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface arg0) {
                    if(target !=null) target.NegativeMethod(null, 0);
                }
            });
        }
    }



    public static String getFancySize(double ttl){
        final long KILOBYTE = 1024L;
        final long MEGABYTE = 1024L * 1024L;
        final long GIGABYTE = 1024L * 1024L * 1024L;

        String sd = "";
        DecimalFormat Currency = new DecimalFormat("#0.00");
        if(ttl / KILOBYTE < KILOBYTE){
            sd = Currency.format((double) (ttl / KILOBYTE)) + " KB";
        }else if ((ttl / MEGABYTE) > KILOBYTE) {
            sd = Currency.format((double) (ttl / GIGABYTE)) + " GB";
        } else {
            sd = Currency.format((double) (ttl / MEGABYTE)) + " MB";
        }
        return sd;
    }

    public static String getElapsed(String date){
        Date d1 = new Date(date);
        if (d1 instanceof Date) {
            long[] d = Utility.getInstance(_context).getTimeDifference(d1, new Date());
            String sd = "";
            if (d[0] > 0) {
                if (d[0] > 1) {
                    sd += Long.toString(d[0]) + " days";
                } else {
                    sd += Long.toString(d[0]) + " day";
                }
            }

            if (!sd.equals(""))
                sd += " &";

            if (d[1] > 0) {
                if (d[1] > 1) {
                    sd += " " + Long.toString(d[1]) + " hours";
                } else {
                    sd += " " + Long.toString(d[1]) + " hour";
                }
            }

            if (d[2] > 0) {
                if (d[2] > 1) {
                    sd += " " + Long.toString(d[2]) + " mins";
                } else {
                    sd += " " + Long.toString(d[2]) + " min";
                }
            }
            if(d[0]==8 && d[1]==1){
                sd="> 1 Week";
            }
            sd += " ago";
            return sd;
        }else{
            return "-";
        }
    }



}
