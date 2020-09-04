package com.binaryinspector.decoders;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.util.ILogger;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.widgets.Display;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import com.binaryinspector.Activator;
import com.binaryinspector.decoders.parameters.*;

public class DecoderFactory {
	private static final String PARM_STRING = "String";
	private static final String PARM_BOOLEAN = "Boolean";
	private static final String PARM_INTEGER = "Integer";
	private static final String PARM_ENUM = "Enum";
	private ArrayList<Decoder> templates = new ArrayList<Decoder>();
	private Object[] templateArray = null;
	private ArrayList<Decoder> decoders = new ArrayList<Decoder>();
	ArrayList<Decoder> enabledDecoders = null;	
	private ArrayList<Decoder> fixedDecoders = null;
	
	DecoderFactory() {
		createTemplates();
		if (! read()) {
			for (Decoder decoder : templates) {
				Decoder clone = (Decoder) decoder.clone();
				decoders.add(clone);
			}
		}
	}
	
	private void createTemplates() {
		InputStream stream = DecoderFactory.class.getResourceAsStream("decoders.json");
		String text = DecodeUtils.readFile(stream);
		try {
			JSONObject root = new JSONObject(text);
			JSONArray array = root.getJSONArray("decoders");
			for (int i = 0; i < array.length(); i++) {
				JSONObject o = array.getJSONObject(i);
				String className = o.getString("class");
				JSONArray parmA = o.getJSONArray("parameters");
				
				ArrayList<Descriptor> parameters = new ArrayList<Descriptor>();
				HashMap<Descriptor, String> defaults = new HashMap<Descriptor, String>();
				for (int j = 0; j < parmA.length(); j++) {
					JSONObject parmO = parmA.getJSONObject(j);
					String parmName = parmO.getString("name");
					String parmType = parmO.getString("type");
					String defaultVal = null;
					if (parmO.has("default")) {
						// TODO: not needed?
						defaultVal = parmO.getString("default");
					}
					ArrayList<String> values = new ArrayList<String>();
					if (parmO.has("values")) {
						JSONArray valuesA = parmO.getJSONArray("values");
						for (int k = 0; k < valuesA.length(); k++) {
							values.add(valuesA.getString(k));
						}
					}
					if (parmO.has("valuesAskClass") && parmO.getBoolean("valuesAskClass")) {
						Class<?> cls;
						try {
							cls = Class.forName(className);
							Method m = cls.getMethod("getEnumValues", (Class<?>[])null);
							String [] l = (String[])m.invoke(null, (Object[])null);
							for (String s : l) {
								values.add(s);
							}							
						} catch (Exception e) {
							throw new RuntimeException(e);
						} 
					}
					String label = null;
					if (parmO.has("label")) {
						label = parmO.getString("label");
					}
					String dump = null;
					if (parmO.has("dump")) {
						dump = parmO.getString("dump");
					}
					
					Descriptor d = null;
					if (PARM_ENUM.equals(parmType)) {
						ArrayList<String> enumDump = null;
						if (parmO.has("enumDump")) {
							enumDump = new ArrayList<String>();
							JSONArray valuesA = parmO.getJSONArray("enumDump");
							for (int k = 0; k < valuesA.length(); k++) {
								enumDump.add(valuesA.getString(k));
							}
						}						
						d = new EnumDescriptor(parmName, label, values, defaultVal, dump, enumDump);
					} else if (PARM_INTEGER.equals(parmType)) {
						d = new IntegerDescriptor(parmName, label, defaultVal, dump);
					} else if (PARM_BOOLEAN.equals(parmType)) {
						d = new BoolDescriptor(parmName, label, defaultVal);						
					} else if (PARM_STRING.equals(parmType)) {
						d = new StringDescriptor(parmName, label, defaultVal, dump);						
					} 
					if (d != null) {
						parameters.add(d);
					}
					if (defaultVal != null) {
						defaults.put(d, defaultVal);
					}
				}
				ParameterValues parmValues = new ParameterValues(parameters);
				for (Descriptor d : defaults.keySet()) {
					parmValues.addString(d.getName(), defaults.get(d));
				}

				Class<?> cls;
				try {
					cls = Class.forName(className);
					Class<?> partypes[] = new Class[1];
					partypes[0] = ParameterValues.class;
					Constructor<?> ct = cls.getConstructor(partypes);
					Object arglist[] = new Object[1];
					arglist[0] = parmValues;
					Object obj = ct.newInstance(arglist);
					
					Decoder d = (Decoder)obj;
					templates.add(d);
					d.refreshParams();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	public ArrayList<Decoder> getDecoders() {
		return decoders;
	}
	
	public ArrayList<Decoder> getEnabledDecoders() {
		if (enabledDecoders == null) {
			enabledDecoders = new ArrayList<Decoder>();
			for (Decoder d : decoders) {
				if (d.isEnabled()) {
					enabledDecoders.add(d);
				}
			}
		}
		return enabledDecoders;
	}
	
	public List<Decoder> getFixedLengthDecoders() {
		if (fixedDecoders != null) {
			return fixedDecoders;
		}
		fixedDecoders = new ArrayList<Decoder>();
		for (Decoder d : decoders) {
			if (d.isEnabled()) {
				int bl = d.getByteLength();
				if (bl > 0) {
					fixedDecoders.add(d);
				}
			}
		}
		return fixedDecoders;
	}
	
	public ArrayList<Decoder> getTemplates() {
		return templates;
	}
	
	public Object [] getTemplateArray() {
		if (templateArray == null) {
			templateArray = templates.toArray(new Object[templates.size()]);
		}
		return templateArray;
	}

	private static ArrayList<Decoder> clone(ArrayList<Decoder> decoders) {
		ArrayList<Decoder> newDecoders = new ArrayList<Decoder>();
		for (Decoder decoder : decoders) {
			Decoder clone = (Decoder) decoder.clone();
			newDecoders.add(clone);
		}
		return newDecoders;
	}
	
	public ArrayList<Decoder> cloneDecoders() {
		return clone(decoders);
	}
	
	public void setDecoders(ArrayList<Decoder> decoders) {
		this.decoders = decoders;
		this.fixedDecoders = null;
		this.enabledDecoders = null;
	}

	public void saveDecoders() {
		saveDecoders(null);
	}

	public void saveDecoders(ArrayList<Decoder> decoders) {
		if (decoders != null) {
			setDecoders(decoders);
		}
		try {
			write();
		} catch (IOException e) {
	        Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, e.getMessage(), e);
	        ILogger logger = Policy.getLog();
	        logger.log(status);
	        ErrorDialog.openError(Display.getCurrent().getActiveShell(), "Error", "Could not write the settings file", status);
		}
	}
	
	public Decoder createFromTemplate(Decoder decoder, ArrayList<Decoder> collection) {
		Decoder clone = (Decoder) decoder.clone();
		collection.add(clone);
		return clone;
	}

	public Decoder createFromTemplate(String name, ArrayList<Decoder> collection) {
		for (Decoder decoder : templates) {
			if (decoder.getName().equals(name)) {
				return createFromTemplate(decoder, collection);
			}
		}
		return null;
	}
	
	public void remove(Decoder decoder) {
		decoders.remove(decoder);
	}
	
	public void write() throws IOException {
		String s = writeToString();
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID); 
		prefs.put("decoders", s);
	}

	public String writeToString() {
		try {
			JSONStringer stringer = new JSONStringer();
			stringer.object().key("decoders").array();
			for (Decoder d : decoders) {
				d.refreshParams();
				stringer.object();
				stringer.key("name").value(d.getName());
				stringer.key("search").value(d.isIncludeInSearch());
				stringer.key("enabled").value(d.isEnabled());
				stringer.key("parameters");
				stringer.object();
				for (String p : d.params.getMetadata().keySet()) {
					if (d.params.isSpecified(p)) {
						stringer.key(p).value(d.params.getString(p));
					}
				}
				stringer.endObject();
				stringer.endObject();
			}
			stringer.endArray();
			stringer.endObject();
			return stringer.toString();
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	private boolean read() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		// you might want to call prefs.sync() if you're worried about others changing your settings
		String s = prefs.get("decoders", null);
		if (s == null) {
			// new workspace, no decoders entry at all
			return false;
		}
		readFromString(s);
		fixedDecoders = null;
		return true;
	}
	
	private void readFromString(String s) {
		ArrayList<Decoder> newDecoders = new ArrayList<Decoder>();
		try {
			if (s.length() > 0) {
				JSONObject root = new JSONObject(s);
				JSONArray array = root.getJSONArray("decoders");
				for (int i = 0; i < array.length(); i++) {
					JSONObject o = array.getJSONObject(i);
					String name = o.getString("name");
					
					Decoder d = createFromTemplate(name, newDecoders);
					boolean enabled = o.has("enabled") ? o.getBoolean("enabled") : true;
					d.setEnabled(enabled);

					boolean includeInSearch = o.has("search") ? o.getBoolean("search") : true;
					JSONObject parmO = o.getJSONObject("parameters");
					d.setIncludeInSearch(includeInSearch);
					
					for (String parmName : d.getParams().getMetadata().keySet()) {
						if (parmO.has(parmName)) {
							String value = parmO.getString(parmName);
							d.getParams().addString(parmName, value);
						}
					}
				}
			}
			decoders = newDecoders;
			for (Decoder d : decoders) {
				d.refreshParams();
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
}
