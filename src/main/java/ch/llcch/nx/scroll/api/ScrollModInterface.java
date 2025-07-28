package ch.llcch.nx.scroll.api;

import com.google.gson.Gson;

public class ScrollModInterface {
	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	@Override
	public String toString() {
		return this.toJson();
	}
	
	public String modId;
	
	public ScrollRegistry ITEMS;
	public ScrollRegistry BLOCKS;
	public ScrollRegistry EVENTS;
	
	public ScrollModInterface(String id) {
		modId = id;
		ITEMS = new ScrollRegistry();
		BLOCKS = new ScrollRegistry();
		EVENTS = new ScrollRegistry();
	}
}
