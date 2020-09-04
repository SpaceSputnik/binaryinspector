package com.binaryinspector.decoders.numeric;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.binaryinspector.decoders.*;
import com.binaryinspector.decoders.parameters.*;

public class PackedDecoder extends DecimalDecoderBase {
	private static final String EVEN_DIGITS = "Even digits";
	private boolean evenDigits;
	private int byteLength = -1;
	
	public PackedDecoder(ParameterValues params) {
		super(params);
	}
	
	@Override
	public String getName() {
		return ("Packed");
	}
	
	@Override
	public void refreshParams() {
		super.refreshParams();
		evenDigits = params.getBoolean(EVEN_DIGITS);
        if (totalDigits > 0) {
        	byteLength = 1 + totalDigits / 2;
       		evenDigits = totalDigits % 2 == 0;
        }
	}
	
	@Override
	public int getByteLength() {
		return byteLength;
	}
	
	public DecodeResult doDecode(byte [] data) {
		DecodeResult res = new DecodeResult();
		try {
			String value = readPackedString(evenDigits, totalDigits, fractionDigits, data, signed);
			res.setValue(value);
		} catch (NumericException e) {
			res.setError(e.getMessage());
		}
		return res;
	}

    private String readPackedString(boolean evenDigits, int totalDigits, int fractionDigits,
    		byte [] bytes, boolean signed) throws NumericException {
        BigInteger bigInt = BigInteger.ZERO;
       	int digits = bytes.length * 2 - (evenDigits ? 2 : 1);
        for (int i=0; i < bytes.length; i++) {
        	// ignore the first high nibble for even number of digits, it is not used in that case
        	if (i > 0 || digits % 2 != 0) {
	            byte hiNbl = (byte) ( (bytes[i] >> 4) & 0x0F );
	            if ( hiNbl < 0x00 || hiNbl > 0x09 ) {
	                throw new NumericException(DecodeUtils.hex(bytes[i]) + ": high nibble is not a digit");
	            }
	            
	            bigInt = bigInt.multiply(BigInteger.TEN);
	            bigInt = bigInt.add(new BigInteger(String.valueOf((int) hiNbl)));
        	}
            
            // Lower nibble in the last byte is sign 
            if ( i < (bytes.length - 1) ) {
                byte loNbl = (byte) ( bytes[i] & 0x0F );
                if ( loNbl < 0x00 || loNbl > 0x09 ) {
                    throw new NumericException(DecodeUtils.hex(bytes[i]) + ": low nibble is not a digit");
                }

                bigInt = bigInt.multiply(BigInteger.TEN);
                bigInt = bigInt.add(new BigInteger(String.valueOf(bytes[i] & 0x0F)));
            }
        }
        
        byte sign = (byte) (bytes[bytes.length - 1] & 0x0F);
        if (bigInt.signum() == 0 && sign == 0x00) {
            // all zeroes are acceptable and mean a zero value
            return "0";
        }
        
		switch (sign) {
		case 0x0A:
		case 0x0C:
		case 0x0E:
		case 0x0F:
			break;

		case 0x0B:
		case 0x0D:
			bigInt = bigInt.negate();
			break;
		default:
			throw new NumericException(sign + ": not a valid sign");

		}

		if (! signed && bigInt.signum() == -1) {
            // TODO: move after fractionDigits processing
			throw new NumericException(bigInt.toString() + " is negative");            
        }

        String value = bigInt.toString();
        if ( fractionDigits > 0 ) {
            BigDecimal bigDec = new BigDecimal(bigInt, fractionDigits);
            value = bigDec.toPlainString();
        }
        
        return value;
    }
}
