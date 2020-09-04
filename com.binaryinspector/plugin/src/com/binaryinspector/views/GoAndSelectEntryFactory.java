package com.binaryinspector.views;

import java.util.*;

import org.eclipse.core.runtime.preferences.*;
import org.json.*;

import com.binaryinspector.Activator;

public class GoAndSelectEntryFactory {
	
	private static final String GO_AND_SELECT_PREF_ENTRY = "goAndSelect";

	public static Collection<GoAndSelectEntry> read() {
		ArrayList<GoAndSelectEntry> entries = new ArrayList<GoAndSelectEntry>();
		
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		String s = prefs.get(GO_AND_SELECT_PREF_ENTRY, "");
		if (s != null) {
			JSONObject root;
			try {
				root = new JSONObject(s);
				JSONArray array = root.getJSONArray("entries");
				for (int i = 0; i < array.length(); i++) {
					JSONObject o = array.getJSONObject(i);
					entries.add(GoAndSelectEntry.fromJson(o));
				}
			} catch (JSONException e) {
				// just return an empty list
			}
		}
		
		return entries;
	}
	
	public static void write(Collection<GoAndSelectEntry> entries) {
		try {
			JSONObject root = new JSONObject();
			JSONArray array = new JSONArray();
			root.put("entries", array);
			
			for (GoAndSelectEntry entry : entries) {
				array.put(entry.toJson());
			}
			IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
			prefs.put(GO_AND_SELECT_PREF_ENTRY, root.toString());
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void clearAll() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		prefs.remove(GO_AND_SELECT_PREF_ENTRY);
	}
}
