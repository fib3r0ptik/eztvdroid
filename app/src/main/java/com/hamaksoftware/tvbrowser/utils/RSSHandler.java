package com.hamaksoftware.tvbrowser.utils;

import com.hamaksoftware.tvbrowser.fragments.IAsyncTaskListener;
import com.hamaksoftware.tvbrowser.models.RSSItem;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RSSHandler extends DefaultHandler {

    public IAsyncTaskListener listener;
    public int calculatedItemCount;
    public RSSFeed _feed;
    public RSSItem _item;
    public String _lastElementName = "";


    boolean bFoundChannel = false;
    final int RSS_TITLE = 1;
    final int RSS_LINK = 2;
    final int RSS_DESCRIPTION = 3;
    final int RSS_CATEGORY = 4;
    final int RSS_PUBDATE = 5;
    final int RSS_ENCLOSURE = 6;
    final int RSS_COMMENTS = 7;
    final int ATTR_FILESIZE = 8;
    final int ATTR_FILELINK = 9;
    final int RSS_GUID = 10;
    final int RSS_LINKS = 11;

    int depth = 0;
    int currentstate = 0;
    int count = 0;

    public RSSHandler() {
    }

    public RSSFeed getFeed() {
        return _feed;
    }

    public void setAsyncTaskListener(IAsyncTaskListener l) {
        listener = l;
    }

    public void startDocument() throws SAXException {
        _feed = new RSSFeed();
        _item = new RSSItem();

    }

    public void endDocument() throws SAXException {

    }

    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts) throws SAXException {
        depth++;

        if (localName.equals("channel")) {
            currentstate = 0;
            return;
        }
        if (localName.equals("image")) {
            // record our feed data - we temporarily stored it in the item :)
            _feed.title = _item.title;
            _feed.pubdate = _item.pubdate;
        }
        if (localName.equals("item")) {
            // create a new item
            _item = new RSSItem();
            return;
        }
        if (localName.equals("title")) {
            currentstate = RSS_TITLE;
            return;
        }
        if (localName.equals("description")) {
            currentstate = RSS_DESCRIPTION;
            return;
        }
        if (localName.equals("link")) {
            currentstate = RSS_LINK;
            return;
        }
        if (localName.equals("category")) {
            _item.showLink = atts.getValue("domain");
            currentstate = RSS_CATEGORY;
            return;
        }
        if (localName.equals("pubDate")) {
            currentstate = RSS_PUBDATE;
            return;
        }

        if (localName.equals("guid")) {
            currentstate = RSS_GUID;
            return;
        }

        if (localName.equals("enclosure")) {
            _item.filesize = Double.parseDouble(atts.getValue("length"));
            _item.altLInk = atts.getValue("url");
            currentstate = RSS_ENCLOSURE;
            return;
        }

        if (localName.equals("comments")) {
            currentstate = RSS_COMMENTS;
            return;
        }

        if (localName.equals("comments")) {
            currentstate = RSS_LINKS;
            return;
        }

        // if we don't explicitly handle the element, make sure we don't wind up
        // erroneously
        // storing a newline or other bogus data into one of our existing
        // elements
        currentstate = 0;
    }

    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        depth--;
        if (localName.equals("item")) {
            _feed.addItem(_item);
            count++;
            return;
        }
    }

    public void characters(char ch[], int start, int length) {
        String theString = new String(ch, start, length);
        //Log.i("RSSReader", "characters[" + theString + "]");

        switch (currentstate) {
            case RSS_TITLE:
                _item.title = theString;
                currentstate = 0;
                break;
            case RSS_LINK:
                _item.itemlink = theString;
                currentstate = 0;
                break;
            case RSS_DESCRIPTION:
                _item.description = theString;
                currentstate = 0;
                break;
            case RSS_CATEGORY:
                _item.category = theString;
                currentstate = 0;
                break;
            case RSS_PUBDATE:
                _item.pubdate = theString;
                currentstate = 0;
                break;
            case RSS_ENCLOSURE:
                _item.enclosure = theString;
                currentstate = 0;
                break;
            case RSS_COMMENTS:
                _item.comments = theString;
                currentstate = 0;
                break;
            case RSS_GUID:
                _item.guid = theString;
                currentstate = 0;
                break;
            case RSS_LINKS:
                _item.itemlink = theString;
                currentstate = 0;
                break;
            default:
                return;
        }

    }
}
