package com.binaryinspector.decoders.numeric;

import java.io.*;
import java.math.BigDecimal;
import java.nio.ByteBuffer;

import org.eclipse.core.runtime.preferences.*;

import com.binaryinspector.Activator;
import com.binaryinspector.decoders.*;
import com.binaryinspector.decoders.parameters.ParameterValues;
import com.binaryinspector.views.SearchDialog;

public class FloatDecoder extends Decoder {
	private boolean ieee;
	private boolean bigEndian;
	private int size;

	public FloatDecoder(ParameterValues params) {
		super(params);
	}

	@Override
	public String getName() {
		return "Floating Point";
	}
	
	@Override
	public void refreshParams() {
		String format = params.getString("Format");
		ieee = format.contains("Ieee");
		bigEndian = format.contains("Big Endian");
		boolean fl = "Float".equals(params.getString("Size"));	
		size = fl ? 4 : 8;		
	}
	
	@Override
	public int getByteLength() {
		return size;
	}

	@Override
	public DecodeResult doDecode(byte[] data) {
		DecodeResult res = new DecodeResult();
		try {
			if (size == 4) {
				float f = unpackFloat(data, !ieee, bigEndian);
				res.setValue(Float.toString(f));
			} else {
				double d = unpackDouble(data, !ieee, bigEndian);
				res.setValue(Double.toString(d));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return res;
	}
	
    /**
     * Extract a float from a bye array.
     * 
     * @param bytes 4 byte array
     * @param ibmHexadec true for ibm hexadecimals, false for ieee
     * @param bigEndian endian type of an ieee float, in effect only if ibmHexadec == false
     * @return a java float value (which is ieee float)
     * @throws IOException
     */
    public static float unpackFloat(byte[] bytes, boolean ibmHexadec, boolean bigEndian) throws IOException {
    	if (ibmHexadec) {
    		return ibm2ieee(bytes);
    	}
        if(! bigEndian) {
            bytes = swapBytes(bytes);
        }
        ByteArrayInputStream byInStream = new ByteArrayInputStream(bytes);
        DataInputStream inStream = new DataInputStream(byInStream);
        return inStream.readFloat();
    }

    /**
     * Extract a double from a bye array.
     * 
     * @param bytes 8 byte array
     * @param ibmHexadec true for ibm hexadecimals, false for ieee
     * @param endian type of an ieee d, in effect only if ibmHexadec == false
     * @return a java float value (which is ieee double)
     * @throws IOException
     */
    public static double unpackDouble(byte[] bytes, boolean ibmHexadec, boolean bigEndian) throws IOException {
    	if (ibmHexadec) {
    		return ibm2ieeeDouble(bytes);
    	}
        if(! bigEndian) {
            bytes = swapBytes(bytes);
        }
        ByteArrayInputStream byInStream = new ByteArrayInputStream(bytes);
        DataInputStream inStream = new DataInputStream(byInStream);
        return inStream.readDouble();
    }	
    
	/*
	 * Takes an 4 byte array containing an IBM formated hex single precision float
	 * and converts it to an IEEE float acceptable to java.
	 */	
	public static float ibm2ieee(byte[] from) {
		if(from.length != 4){
			throw new IllegalArgumentException("Argument not an 4 byte buffer");
		}
		long fr; /* fraction */
		int exp; /* exponent */
		int sgn; /* sign */

		/* split into sign, exponent, and fraction */
		fr  =(long) ByteBuffer.wrap(from).getInt();
		fr &= 0x00000000ffffffffL;

		sgn =(int) ((long)fr >> 31);
		fr = (fr << 1) & 0x00000000ffffffffL;  //fr <<= 1; /* shift sign out*/
		exp =(int) ((long) fr>>25);
		fr = (fr << 7) & 0x00000000ffffffffL; /* shift exponent out */
		
		// check for the special values NAN and INFINITY
		if((exp & 0x0000007f) == 0x0000007f){
			if(fr == 0x00000000000000L)
				return sgn==0?Float.POSITIVE_INFINITY:Float.NEGATIVE_INFINITY;
			else
				return Float.NaN;
		}
		
		if (fr == 0) {
			return (float)0;
		}

		/* adjust exponent from base 16 offset 64 radix point before first digit
		 to base 2 offset 127 radix point after first digit */
		/* (exp - 64) * 4 + 127 - 1 == exp * 4 - 256 + 126 == (exp << 2) - 130 
		 * ((exp * 4) -(64*4)) + 126
		 * (exp * 4) - 130
		 * (exp << 2) - 130 
		 * */
		exp = (exp << 2) - 130;

		/* (re)normalize */
		while (fr < 0x80000000L) { /* 3 times max for normalized input */
			--exp;
			fr <<= 1;
		}

		if (exp <= 0) {       /* underflow */
			if (exp < -24)    /* complete underflow */
				return Float.NaN;
			else
				fr >>= -exp;  /* partial underflow - return denormalized number */
			exp = 0;
		} else if (exp >= 255) { /* overflow - return infinity */
			return Float.NaN;
		} else {              /* just a plain old number - remove the assumed high bit */
			fr = (fr << 1) & 0x00000000ffffffffL;
		}

		fr  = (fr >> 9) | (exp << 23) | (sgn << 31);
		ByteBuffer b = ByteBuffer.allocate(8);
		b.putLong(fr);
		return b.getFloat(4);
	}
	
	/*
	 * Takes an 8 byte array containing an IBM formated hex double precision float
	 * and converts it to an IEEE double acceptable to java.
	 */
	public static double ibm2ieeeDouble(byte[] from) throws java.lang.IllegalArgumentException {
		long fr; /* fraction */
		int exp; /* exponent */
		int sgn; /* sign */

		if(from.length != 8){
			throw new IllegalArgumentException("Argument not an 8 byte buffer");
		}
		/* split into sign, exponent, and fraction */
		fr  =(long) ByteBuffer.wrap(from).getLong();
		sgn =(int) ((long)fr >> 63) & 0x00000001;
		//fr <<=1; /* shift sign out */
		exp =(int) ((long)fr>>56) &0x0000007f; //Shift exponen out and flip off sign bit.
		fr <<= 8; /* shift out exponent and sign */

		// check for the special values NAN and INFINITY
		if((exp & 0x0000007f) == 0x0000007f){
			if(fr == 0x00000000000000L)
				return sgn==0?Double.POSITIVE_INFINITY:Double.NEGATIVE_INFINITY;
			else
				return Double.NaN;
		}

		if (fr == 0) { 
			return (float)0;
		}

		/* adjust exponent from base 16 offset 64 radix point before first digit
		 to base 2 offset 1023 radix point after first digit */
		/* (exp - 64) * 4 + 1023 - 1
		 * ((exp * 4) -(64*4)) + 1022
		 * (exp * 4) +766
		 * (exp << 2) +766 
		 * */
		exp = (exp << 2) +766;

		/* (re)normalize */
		while ((fr & 0x8000000000000000L) == 0) { 
			--exp;
			fr <<= 1;
		}

		if (exp <= 0) {          /* underflow */
			if (exp < -24)       /* complete underflow - return nan */
				return Double.NaN;
			else
				fr >>= -exp;     /* partial underflow - return denormalized number */
			exp = 0;
		} else if (exp >= 4095) { /* overflow - return nan */
			return Float.NaN;
		} else {              /* just a plain old number - remove the assumed high bit */
			fr <<= 1;
		}
		fr  = (((long)fr >> 12)& 0x000fffffffffffffL) | ((long)exp << 52) | ((long)sgn << 63);
		ByteBuffer b = ByteBuffer.allocate(8);
		b.putLong(0,fr);
		return b.getDouble(0);
	}
	
    public static byte[] swapBytes(byte[] bytes) {
    	// TODO: this is not very smart, no need to create another array
        byte[] swappedBytes = new byte[bytes.length];
        for (int i=0; i < bytes.length; i++){
           swappedBytes[i] = bytes[(bytes.length-1) - i];
        }
        return swappedBytes;
    }

	@Override
	public String validate(String value) {
		try {
			new BigDecimal(value);
		} catch (NumberFormatException e) {
			return "Not a valid number";
		}
		return null;
	}

	@Override
	protected boolean compare(String val1, String val2) {
		if (val1.equalsIgnoreCase("NaN")) {
			return val2.equalsIgnoreCase("NaN");
		}
		if (val2.equalsIgnoreCase("NaN")) {
			return val1.equalsIgnoreCase("NaN");
		}
		
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		String s = prefs.get(SearchDialog.PREF_ENTRY_FLOAT_PRECISION, "0");
		BigDecimal pbd = new BigDecimal(s);
		BigDecimal bd1 = new BigDecimal(val1);
		BigDecimal bd2 = new BigDecimal(val2);
		BigDecimal diff = bd1.subtract(bd2).abs();
		return diff.compareTo(pbd) <= 0;
	}
}
