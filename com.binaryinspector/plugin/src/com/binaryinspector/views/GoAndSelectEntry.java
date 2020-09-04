package com.binaryinspector.views;

import org.json.*;

public class GoAndSelectEntry {
	public final String name;
	public final int offset;
	public final boolean relative;
	public final int byteLength;
	
	public GoAndSelectEntry(String name, int position, boolean relative, int selectLength) {
		this.name = name;
		this.offset = position;
		this.relative = relative;
		this.byteLength = selectLength;
	}
	
	public boolean equals(Object o) {
		if (o == null || ! (o instanceof GoAndSelectEntry)) {
			return false;
		}
		GoAndSelectEntry anotherO = (GoAndSelectEntry)o;
		return this.offset == anotherO.offset && this.relative == anotherO.relative && this.byteLength == anotherO.byteLength;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (name != null) {
			sb.append(name).append(": ");
		}
		sb.append("Pos ").append(offset).append(" from ");
		sb.append(relative ? "cursor" : "beginning");
		if (byteLength > 0) {
			sb.append(", ").append("select ").append(byteLength).append(" bytes");
		}
		return sb.toString();
	}

	
	public static GoAndSelectEntry fromJson(JSONObject jo) {
		String name = null;
		int position = -1;
		boolean relative = true;
		int selectLength = 0;
		
		try {
			if (jo != null) {
				name = jo.has("name") ? jo.getString("name") : null;
				position = jo.getInt("offset");
				relative = jo.getBoolean("relative");
				selectLength = jo.getInt("byteLength");
			}
		} catch (JSONException e) {}
		
		return new GoAndSelectEntry(name, position, relative, selectLength);
	}
	
	public JSONObject toJson() {
		JSONObject jo = new JSONObject();
		try {
			jo.put("name", name);
			jo.put("offset", offset);
			jo.put("relative", relative);
			jo.put("byteLength", byteLength);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return jo;
	}
	
	
}
