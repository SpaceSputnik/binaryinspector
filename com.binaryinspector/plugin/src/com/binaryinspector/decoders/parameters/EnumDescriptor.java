package com.binaryinspector.decoders.parameters;

import java.util.ArrayList;

public class EnumDescriptor extends Descriptor {
	private ArrayList<String> values;
	private ArrayList<String> enumDumps;
	public EnumDescriptor(String name, String label, ArrayList<String> values, String defaultValue, String dump,
			ArrayList<String> enumDumps) {
		super(name, label, dump);
		this.values = values;
		this.enumDumps = enumDumps;
	}
	public ArrayList<String> getValues() {
		return values;
	}
	public ArrayList<String> getEnumDumps() {
		return enumDumps;
	}
	
	public String dump(String value) {
		int i = 0;
		if (enumDumps != null) {
			for (String v : values) {
				if (v.equalsIgnoreCase(value)) {
					String format = enumDumps.get(i);
					if (format.length() == 0) {
						return "";
					}
					return String.format(format, value);
				}
				i++;
			}
		}
		return super.dump(value);
	}
}
