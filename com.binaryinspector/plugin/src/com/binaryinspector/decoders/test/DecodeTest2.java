package com.binaryinspector.decoders.test;

import java.io.UnsupportedEncodingException;

import com.binaryinspector.decoders.*;
import com.binaryinspector.decoders.parameters.*;

public class DecodeTest2 extends Decoder {
	public DecodeTest2(ParameterValues params) {
		super(params);
	}

	@Override
	public String getName() {
		return "Test Decoder";
	}

	@Override
	public DecodeResult doDecode(byte[] data) {
		DecodeResult res = new DecodeResult();
		res.setDescription(getName());
		try {
			String val = new String(data, "ascii");
			if (! "Test".equals(val)) {
				res.setError("\"Test\" is expected");
			} else {
				res.setValue("Test");
			}			
		} catch (UnsupportedEncodingException e) {
			res.setError(e.getMessage());
		}
		return res;
	}

	@Override
	protected void refreshParams() {}

	@Override
	public String validate(String value) {
		return null;
	}

	@Override
	protected boolean compare(String val1, String val2) {
		return true;
	}

	@Override
	public int getByteLength() {
		return -1;
	}
}
