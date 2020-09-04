package com.binaryinspector.decoders.text;

import org.eclipse.core.runtime.preferences.*;

import com.binaryinspector.Activator;
import com.binaryinspector.decoders.*;
import com.binaryinspector.decoders.parameters.ParameterValues;
import com.binaryinspector.encoding.Encoding;
import com.binaryinspector.views.SearchDialog;

public class TextDecoder extends Decoder {
	private Encoding encoding;
	private int byteLength;
	
	public TextDecoder(ParameterValues params) {
		super(params);
		refreshParams();
	}

	@Override
	public void refreshParams() {
		String name = params.getString("Character set");
		encoding = Encoding.create(name);
		byteLength = params.isSpecified("Byte length") ? params.getInteger("Byte length") : -1;
	}
	
	@Override
	public int getByteLength() {
		return byteLength;
	}


	@Override
	public String getName() {
		return "Text";
	}

	@Override
	public DecodeResult doDecode(byte[] data) {
		DecodeResult r = new DecodeResult();
		r.setValue(encoding.getString(data));
		return r;
	}
	
	public static String[] getEnumValues() {
		return Encoding.getNames();
	};

	@Override
	public String validate(String value) {
		return null;
	}

	@Override
	protected boolean compare(String val1, String val2) {
		val1 = val1.trim();
		val2 = val2.trim();
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		boolean caseSensitive = prefs.getBoolean(SearchDialog.PREF_ENTRY_CASE_SENSITIVE, false);
		return caseSensitive ? val1.equals(val2) : val1.equalsIgnoreCase(val2);
	}
}
