package com.hamaksoftware.tvbrowser.torrentcontroller;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Torrents {

	private int _itemcount = 0;
	private List<TorrentItem> _itemlist = new ArrayList<TorrentItem>(0);

	public Torrents() {
	}

	public int addItem(TorrentItem item) {
		_itemlist.add(item);
		_itemcount++;
		return _itemlist.size();
		// return _itemcount;
	}

	public void clear() {
		_itemlist.clear();
		_itemcount = _itemlist.size();
	}

	public TorrentItem getItem(int location) {
		return _itemlist.get(location);
	}

	public List<TorrentItem> getAllItems() {
		Collections.sort(_itemlist, new SortbyRemaining());
		return _itemlist;
	}

	public int getItemCount() {
		if (_itemlist == null)
			return 0;
		return _itemlist.size();
		// return _itemcount;
	}

	public String getTotalSpeed(String section) {
		String sd = "";
		try {
			final long KILOBYTE = 1024L;
			final long MEGABYTE = 1024L * 1024L;
			double ttl = 0;
			for (int i = 0; i < _itemcount; i++) {
				if (section.equals("D")) {
					ttl += (double) _itemlist.get(i).getDownloadSpeed();
				} else {
					ttl += (double) _itemlist.get(i).getUploadSpeed();
				}
			}
			DecimalFormat Currency = new DecimalFormat("#0.00");
			if ((ttl / KILOBYTE) > 1000) {
				sd = Currency.format(ttl / MEGABYTE) + " MB/s";
			} else {
				sd = Currency.format(ttl / KILOBYTE) + " KB/s";
			}
		} catch (Exception e) {
			sd = "0 KB/s";
		}
		return sd;
	}

	public String getHashes() {
		if (_itemcount > 0) {
			StringBuilder sb = new StringBuilder("&");
			for (int i = 0; i < _itemcount; i++) {
				sb.append("hash=").append(_itemlist.get(i).getHash())
						.append("&");
			}
			return sb.toString().substring(0, sb.toString().length() - 1);
		} else {
			return "";
		}
	}

	public String getCompletedHashes() {
		if (_itemcount > 0) {
			StringBuilder sb = new StringBuilder("&");
			for (int i = 0; i < _itemcount; i++) {
				if (_itemlist.get(i).getPercent() >= 1000)
					sb.append("hash=").append(_itemlist.get(i).getHash())
							.append("&");
			}
			return sb.toString().substring(0, sb.toString().length() - 1);
		} else {
			return "";
		}
	}

	public class SortbyRemaining implements Comparator<TorrentItem> {
		public int compare(TorrentItem o1, TorrentItem o2) {
			return o2.getPercent() - o1.getPercent();
		}
	}

	public boolean hasCompleted() {
		if (_itemlist == null)
			return false;
		if (_itemcount <= 0)
			return false;
		boolean found = false;
		for (int i = 0; i < _itemcount; i++) {
			if (_itemlist.get(i).getPercent() >= 1000) {
				found = true;
				break;
			}
		}
		return found;
	}

}
