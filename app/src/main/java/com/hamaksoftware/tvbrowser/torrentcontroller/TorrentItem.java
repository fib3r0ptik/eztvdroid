package com.hamaksoftware.tvbrowser.torrentcontroller;



public class TorrentItem{
	private String _name = null;
	private String _hash = null;
	private int _status;
	private int _size = 0;
	private int _percent = 0;
	private int _downloaded = 0;
	private int _uploaded = 0;
	private int _download_speed = 0;
	private int _upload_speed = 0;
	private int _seeders_con = 0;
	private int _seeders_all = 0;
	private int _peers_con = 0;
	private int _peers_all = 0;
	private int _order = 0;
	private int _remaining = 0;
	private int _eta = 0;
	public boolean isSelected;
	public boolean sortByQueue;
	

	public void setname(String name) {
		_name = name;
	}

	public void setHash(String hash) {
		_hash = hash;
	}

	public void setStatus(int status) {
		_status = status;
	}

	public void setSize(int size) {
		_size = size;
	}

	public void setPercent(int percent) {
		_percent = percent;
	}

	public void setDownloaded(int downloaded) {
		_downloaded = downloaded;
	}

	public void setUploaded(int uploaded) {
		_uploaded = uploaded;
	}

	public void setDownloadSpeed(int downloadSpeed) {
		_download_speed = downloadSpeed;
	}

	public void setUploadSpeed(int uploadSpeed) {
		_upload_speed = uploadSpeed;
	}

	public void setSeedersCon(int seeders) {
		_seeders_con = seeders;
	}

	public void setSeedersAll(int seeders) {
		_seeders_all = seeders;
	}

	public void setPeersCon(int peers) {
		_peers_con = peers;
	}

	public void setPeersAll(int peers) {
		_peers_all = peers;
	}

	public void setOrder(int order) {
		_order = order;
	}

	public void setRemaining(int remaining) {
		_remaining = remaining;
	}

	public void setETA(int eta) {
		_eta = eta;
	}

	public String getName() {
		return _name;
	}

	public String getHash() {
		return _hash;
	}

	public int getStatus() {
		return _status;
	}

	public int getSize() {
		return _size;
	}

	public int getPercent() {
		return _percent;
	}

	public int getDownloaded() {
		return _downloaded;
	}

	public int getUploaded() {
		return _uploaded;
	}

	public int getDownloadSpeed() {
		return _download_speed;
	}

	public int getUploadSpeed() {
		return _upload_speed;
	}

	public int getSeedersCon() {
		return _seeders_con;
	}

	public int getSeedersAll() {
		return _seeders_all;
	}

	public int getPeersCon() {
		return _peers_con;
	}

	public int getPeersAll() {
		return _peers_all;
	}

	public int getOrder() {
		return _order;
	}

	public int getRemaining() {
		return _remaining;
	}

	public int getETA() {
		return _eta;
	}

	/*
	 * public int compareT
	 * 
	 * public int compare(TorrentItem t) { return _percent - t._percent; }
	 */
	/*
	public int compareTo(TorrentItem t) {
		if(t.getOrder() == t._order) return 0;
		return t.getOrder() - _order;
		//return (t.getOrder() < _order?-1:1);
		/*
		if(sortByQueue){
			if(t.getOrder() == t._order) return 0;
			return (t.getOrder() < _order?-1:1);
		}
		return t._percent - _percent;
	}*/
}
