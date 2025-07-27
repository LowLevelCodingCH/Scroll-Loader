package ch.llcch.nx.scroll.api;

import java.util.List;
import java.util.ArrayList;

import com.google.gson.Gson;

public class ScrollRegistry {
	private List<ScrollRegistryItem> items;
	
	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	@Override
	public String toString() {
		return this.toJson();
	}
	
	public ScrollRegistry() {
		items = new ArrayList<ScrollRegistryItem>(64);
	}

	public void register(String id, Object reg, Object deg) {
		items.add(new ScrollRegistryItem(id, reg, deg));
	}
	
	public List<ScrollRegistryItem> getItems() {
		return items;
	}
}
