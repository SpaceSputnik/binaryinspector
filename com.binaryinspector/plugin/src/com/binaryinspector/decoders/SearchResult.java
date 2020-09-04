package com.binaryinspector.decoders;

public class SearchResult {
	public final String actualValue;
	public final int offset;
	public int byteLength;
	public final Decoder decoder;
	
	public SearchResult(String actualValue, int offset, int byteLength, Decoder decoder) {
		this.actualValue = actualValue;
		this.offset = offset;
		this.byteLength = byteLength;
		this.decoder = decoder;
	}
}
