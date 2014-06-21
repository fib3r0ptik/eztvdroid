package com.hamaksoftware.tvbrowser.utils;

import android.content.Context;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class FeedParser {
    private String url;
    private Context ctx;

    public FeedParser(Context ctx, String url) {
        this.url = url;
        this.ctx = ctx;
    }


    public RSSFeed getFeed() {
        try {
            String rawContent = "";
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader xmlreader = parser.getXMLReader();
            RSSHandler rsshandler = new RSSHandler();
            //rsshandler.setAsyncTaskListener(l);
            xmlreader.setContentHandler(rsshandler);

            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 3000);
            HttpConnectionParams.setSoTimeout(params, 10000);
            DefaultHttpClient cn = new DefaultHttpClient(params);
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.215 Safari/535.1");
            //httpget.addHeader("Accept-Charset","utf-8");
            httpget.addHeader("Accept-Encoding", "gzip");

            HttpResponse response = cn.execute(httpget);
            InputStream is = response.getEntity().getContent();
            Header contentEncoding = response.getFirstHeader("Content-Encoding");
            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                is = new GZIPInputStream(is);
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(is), 8 * 1024);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) sb.append(line);
            rawContent = sb.toString();
            rd.close();

            InputStream iss = new ByteArrayInputStream(rawContent.getBytes("UTF-8"));
            InputStream icss = new ByteArrayInputStream(rawContent.getBytes("UTF-8"));
            InputSource isrc = new InputSource(iss);
            int count = Utility.getInstance(ctx).countElement("//item", new InputSource(icss));
            rsshandler.calculatedItemCount = count;
            xmlreader.parse(isrc);

            return rsshandler.getFeed();

        } catch (Exception e) {
            e.printStackTrace();
            return new RSSFeed();
        }
    }
}
