package com.binaryinspector.decoders.numeric;

import java.math.*;

import com.binaryinspector.decoders.DecodeResult;
import com.binaryinspector.decoders.parameters.*;

public class Binary extends DecimalDecoderBase {
	private boolean littleEndian;
	private int byteLength;

	public Binary(ParameterValues params) {
		super(params);
	}

	@Override
	public String getName() {
		return "Binary Integer";
	}
	
	@Override
	public void refreshParams() {
		super.refreshParams();
		littleEndian = "Little".equals(params.getString("Endian"));
		byteLength = params.isSpecified("Byte length") ? params.getInteger("Byte length") : -1;
	};
	
	@Override
	public int getByteLength() {
		return byteLength;
	}

	@Override
	public DecodeResult doDecode(byte[] data) {
		if (littleEndian) {
			data = swapBytes(data);
		}
		boolean unsigned = params.getBoolean("Unsigned");
		BigInteger b = unsigned ? new BigInteger(1, data) : new BigInteger(data);
		BigDecimal d = new BigDecimal(b);
		if (fractionDigits > 0) {
			d = d.movePointLeft(fractionDigits);
		}
		DecodeResult r = new DecodeResult();
		r.setValue(d.toPlainString());
		return r;
	}
	
    public static byte[] swapBytes(byte[] bytes) {
    	// TODO: this is not very smart, no need to create another array
        byte[] swappedBytes = new byte[bytes.length];
        for (int i=0; i < bytes.length; i++){
           swappedBytes[i] = bytes[(bytes.length-1) - i];
        }
        return swappedBytes;
    }
}
