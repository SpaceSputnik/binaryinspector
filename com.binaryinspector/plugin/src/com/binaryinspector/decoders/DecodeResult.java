package com.binaryinspector.decoders;

import org.eclipse.swt.graphics.*;

import com.binaryinspector.views.widgets.ColumnDataSource;

public class DecodeResult implements ColumnDataSource {
	private static final RGB RED = new RGB(0xFF, 00, 00);
	private String description;
	private boolean success = true;
	private String value;
	private Decoder decoder;
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setError(String message) {
		this.success = false;
		this.value = message;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Decoder getDecoder() {
		return decoder;
	}
	public void setDecoder(Decoder decoder) {
		this.decoder = decoder;
	}
	@Override
	public String getData(int col) {
		return col == 0 ? value : description;
	}
	@Override
	public RGB getColor(int col) {
		return success ? null : RED;
	}
}
