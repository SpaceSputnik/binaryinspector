package com.binaryinspector.decoders.parameters;

public abstract class Descriptor {
	private String name;
	private String label;
	private String dump;
	
	public String getName() {
		return name;
	}
	public Descriptor(String name, String label, String dump) {
		this.name = name;
		this.label = label;
		this.dump = dump;
	}
	@Override
	public String toString() {
		return label != null ? label : name;
	}
	
	public String dump(String value) {
		if (dump != null) {
			return String.format(dump, value);
		}
		return name + "=" + value;
	}
}
