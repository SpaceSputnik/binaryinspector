package com.binaryinspector.decoders.numeric;

import java.math.BigDecimal;

import com.binaryinspector.decoders.Decoder;
import com.binaryinspector.decoders.parameters.ParameterValues;

public abstract class DecimalDecoderBase extends Decoder {
	private static final String FRACTION_DIGITS = "Fraction digits";
	private static final String TOTAL_DIGITS = "Total digits";
	private static final String UNSIGNED = "Unsigned";
	protected int totalDigits;
	protected int fractionDigits;
	protected boolean signed;
	
	public DecimalDecoderBase(ParameterValues params) {
		super(params);
	}
	
	
	@Override
	public void refreshParams() {
		fractionDigits = params.isSpecified(FRACTION_DIGITS) ? params.getInteger(FRACTION_DIGITS) : -1;
		totalDigits = params.isSpecified(TOTAL_DIGITS) ? params.getInteger(TOTAL_DIGITS) : -1;
		signed = ! params.getBoolean(UNSIGNED);
	}
	
	@Override
	public String validate(String value) {
		BigDecimal bd;
		try {
			bd = new BigDecimal(value);
		} catch (NumberFormatException e) {
			return "Not a valid number";
		}
		if (signed && bd.compareTo(BigDecimal.ZERO) < 0) {
			return "Value cannot be negative";
		}
		if (totalDigits < 0 && fractionDigits < 0) {
			return null;
		}
		
		String s = bd.toPlainString();
		int actuaDecDigits = 0;
		int actualFractionDigits = 0;
		boolean decPointSeen = false;
		for (char c : s.toCharArray()) {
			if (c == '.') {
				decPointSeen = true;
			} else if (Character.isDigit(c)) {
				if (decPointSeen) {
					actualFractionDigits++;
				} else {	
					actuaDecDigits++;
				}
			}
		}
		if (totalDigits > 0 && (actuaDecDigits > totalDigits)) {
			return "Too many digits";
		}
		
		if (fractionDigits >= 0 && actualFractionDigits > fractionDigits) {
			return "Too many digits after the decimal point";
		}
		
		return null;
	}
	
	@Override
	protected boolean compare(String val1, String val2) {
		BigDecimal bd1 = new BigDecimal(val1);
		BigDecimal bd2 = new BigDecimal(val2);
		return bd1.compareTo(bd2) == 0;
	}
}
