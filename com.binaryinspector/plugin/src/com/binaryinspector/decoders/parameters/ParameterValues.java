package com.binaryinspector.decoders.parameters;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ParameterValues implements Cloneable {
	private LinkedHashMap<String, Descriptor> metadata;
	private LinkedHashMap<Descriptor, String> values = new LinkedHashMap<Descriptor, String>();
	private LinkedHashMap<Descriptor, Boolean> specified = new LinkedHashMap<Descriptor, Boolean>();

	public ParameterValues(ArrayList<Descriptor> metadataList) {
		this.metadata = new LinkedHashMap<String, Descriptor>();
		for (Descriptor d : metadataList) {
			this.metadata.put(d.getName(), d);
		}
	}

	public LinkedHashMap<String, Descriptor> getMetadata() {
		return metadata;
	}

	public LinkedHashMap<Descriptor, String> getValues() {
		return values;
	}
	
	private Descriptor getDescriptor(String name) {
		Descriptor d = metadata.get(name);
		return d;
	}

	public ParameterValues addString(String name, String value) {
		Descriptor d = getDescriptor(name);
		values.put(d, value);
		specified.put(d, true);
		return this;
	};

	public String getString(String name) {
		Descriptor d = getDescriptor(name);
		return isSpecified(d) ? values.get(d) : "";
	}

	public ParameterValues addBoolean(String name, boolean value) {
		addString(name, Boolean.toString(value));
		return this;
	};

	public boolean getBoolean(String name) {
		return Boolean.valueOf(getString(name));
	}

	public ParameterValues addInteger(String name, int value) {
		addString(name, Integer.toString(value));
		return this;
	};

	public Integer getInteger(String name) {
		return Integer.valueOf(getString(name));
	}

	public boolean compare(String name, String value) {
		return value.equals(getString(name));
	}

	public void setSpecified(String name, boolean yes) {
		Descriptor d = getDescriptor(name);
		specified.put(d, yes);
	}

	public boolean isSpecified(String name) {
		Descriptor d = getDescriptor(name);
		return isSpecified(d);
	}

	public boolean isSpecified(Descriptor d) {
		Boolean b = specified.get(d);
		return b == null ? false : b.booleanValue();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {
		try {
			Object clone = super.clone();
			ParameterValues typedClone = (ParameterValues) clone;
			// no need to clone metadata
			typedClone.values = (LinkedHashMap<Descriptor, String>)values.clone();
			typedClone.specified = (LinkedHashMap<Descriptor, Boolean>)specified.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
