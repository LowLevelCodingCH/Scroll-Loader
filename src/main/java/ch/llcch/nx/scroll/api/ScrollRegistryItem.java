package ch.llcch.nx.scroll.api;

import com.google.gson.Gson;

public class ScrollRegistryItem {
	public Object obj;
	public Object pbj;
	public String id;
	
	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	@Override
	public String toString() {
		return this.toJson();
	}
	
	public ScrollRegistryItem(String a, Object b, Object c) {
		this.obj = b;
		this.pbj = c;
		this.id = a;
	}
}
