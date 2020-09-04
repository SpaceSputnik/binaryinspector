package com.binaryinspector.decoders.numeric;

import java.io.UnsupportedEncodingException;
import java.math.*;

import com.binaryinspector.decoders.*;
import com.binaryinspector.decoders.parameters.*;
import com.binaryinspector.encoding.*;


public class ZonedDecoder extends DecimalDecoderBase {
	private static final String NATIONAL = "National";
	private static final String LEADING = "Leading";
	private static final String SIGN_POSITION = "Sign position";
	private static final String SIGN_SEPARATE = "Sign separate";
	private static final String ASCII = "Ascii";
	private static final String TYPE = "Type";
	
	private Encoding encoding;
	private boolean signSeparate;
	private boolean signLeading;
	private boolean national;
	private int byteLength;

	public ZonedDecoder(ParameterValues params) {
		super(params);
	}
	
	@Override
	public String getName() {
		return "Zoned";
	}
	
	@Override
	public void refreshParams() {
		super.refreshParams();
		String encName = params.compare(TYPE, ASCII) ? "ascii" : "cp037";
		encoding = Encoding.create(encName);
		signSeparate = params.getBoolean(SIGN_SEPARATE);
		signLeading = params.compare(SIGN_POSITION, LEADING);
		national = params.getBoolean(NATIONAL);
        if (totalDigits > 0) {
        	byteLength = signSeparate ? 1 : 0;
        	byteLength += totalDigits;
        }
	}
	
	@Override
	public int getByteLength() {
		return byteLength;
	}

	@Override
	public DecodeResult doDecode(byte[] data) {
		DecodeResult res = new DecodeResult();
		try {
			String value = readZonedString(data, encoding,
					signed,
					totalDigits,
					fractionDigits,
					signSeparate, 
					signLeading, 
					national);
			res.setValue(value);
		} catch (NumericException e) {
			res.setError(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			res.setError(e.getMessage());
		}
		return res;
	}
	
    protected String readZonedString(byte[] bytes, Encoding encoding, boolean signed, int totalDigits, int fractionDigits,
            boolean signSeparate, boolean signLeading, boolean national) throws UnsupportedEncodingException, NumericException {
        boolean positive = true;
        String digits;
        Encoding enc = encoding;

        // TODO: treats AC as 12 for Ebcdic???
        
        if (signSeparate) {
            String value = enc.getString(bytes);
            int signPos = signLeading ? 0 : value.length() - 1;
            char signChar = value.charAt(signPos);
            if (signChar != '+' && signChar != '-' && signChar != ' ') {
                throw new NumericException(signChar + ": invalid sign");
            }
            positive = signChar != '-';
            
            int digitsStart = signLeading ? 1 : 0;
            int digitsEnd = signLeading ? value.length() : value.length() - 1;
            digits = value.substring(digitsStart, digitsEnd);
            // skip leading blanks
            int start = 0;
            int i;
            for (i = 0; i < digits.length(); i++) {
                if (digits.charAt(i) != ' ') {
                    start = i;
                    break;
                }
            }
            digits = (i == digits.length()) ? "" : digits.substring(start);
        } else {
            if (national) {
                throw new RuntimeException("National can't have a non-separate sign");
            }
            // can't be NATIONAL
            int digitsBytesStart = signLeading ? 1 : 0;
            int digitsBytesEnd = signLeading ? bytes.length : bytes.length - 1;
            
            byte [] digitsBytes = new byte[digitsBytesEnd - digitsBytesStart];
            
            System.arraycopy(bytes, digitsBytesStart, digitsBytes, 0, digitsBytesEnd - digitsBytesStart);
            digits = encoding.getString(digitsBytes);
            // skip leading blanks
            int start = 0;
            int i;
            for (i = 0; i < digits.length(); i++) {
                if (digits.charAt(i) != ' ') {
                    start = i;
                    break;
                }
            }
            digits = (i == digits.length()) ? "" : digits.substring(start);
            // now process the sign byte
            byte signByte = bytes[signLeading ? 0 : bytes.length - 1];
            // treat an empty string as zero
            if (!(digits.length() == 0 && signByte == encoding.getSpaceBytes()[0])) {
                positive = processSignNibble(signByte, signed, encoding.isEbcdic());
                signByte = convertModifiedASCIISignByte(signByte);
                
                // restore the digit from the sight byte
                byte tmpByte = (byte)(signByte & 0x0F);
                String missingChar = Integer.valueOf(((int)tmpByte)).toString();  
                if (signLeading) {
                    digits = missingChar + digits; 
                } else {
                    digits = digits + missingChar;
                }
            }
        }
        
        // validate each digit
        for (int i = 0; i < digits.length(); i++) {
            char ch = digits.charAt(i);
            if (Character.digit(ch, 10) < 0) {
                throw new NumericException("\"" + (ch == 0 ? " " : ch) + "\": invalid digit");
            }
        }

        // at this point we have the digits retrieved, now generate the actual value
        BigInteger bigIntValue = new BigInteger(digits.length() == 0 ? "0" : digits);
        
        if (! positive) {
            bigIntValue = bigIntValue.negate();
        }
        
        String value = bigIntValue.toString();
        if (fractionDigits > 0) {
            BigDecimal bigDec = new BigDecimal(bigIntValue, fractionDigits);
            value = bigDec.toPlainString();
        }
        if (! positive && ! signed) {
            // can happen for unsigned picture and sign separate 
            throw new NumericException(value + " is negative");            
        }    
        
        return value;
    }
    
    private static byte convertModifiedASCIISignByte(byte signByte) {
        switch (signByte) {
            case (byte) 0x7B : return (byte) 0x30;
            case (byte) 0x41 : return (byte) 0x31;
            case (byte) 0x42 : return (byte) 0x32;
            case (byte) 0x43 : return (byte) 0x33;
            case (byte) 0x44 : return (byte) 0x34;
            case (byte) 0x45 : return (byte) 0x35;
            case (byte) 0x46 : return (byte) 0x36;
            case (byte) 0x47 : return (byte) 0x37;
            case (byte) 0x48 : return (byte) 0x38;
            case (byte) 0x49 : return (byte) 0x39;
            case (byte) 0x7D : return (byte) 0x70;
            case (byte) 0x4A : return (byte) 0x71;
            case (byte) 0x4B : return (byte) 0x72;
            case (byte) 0x4C : return (byte) 0x73;
            case (byte) 0x4D : return (byte) 0x74;
            case (byte) 0x4E : return (byte) 0x75;
            case (byte) 0x4F : return (byte) 0x76;
            case (byte) 0x50 : return (byte) 0x77;
            case (byte) 0x51 : return (byte) 0x78;
            case (byte) 0x52 : return (byte) 0x79;
        }
        return signByte;
    }
    
    private boolean processSignNibble(byte signByte, boolean signedPic, boolean ebcdic) throws NumericException  {
        byte nibble = (byte)(signByte & 0xF0);
        if (ebcdic) {
            if (signedPic && (nibble == (byte)0xC0 || nibble == (byte)0xA0)) {
                // values as per Principles of Operation (0xC0 is the preferred value, 0xA0 is an alternative value)
                return true; // valid ebcdic positive,   
            } 
            if (signedPic && (nibble == (byte)0xD0 || nibble == (byte)0xB0)) {
                // values as per Principles of Operation (0xD0 is the preferred value, 0xB0 is an alternative value)
                return false; // valid ebcdic negative
            }
            if (nibble == (byte)0xF0) { // people want unsigned values to be accepted in signed fields as positive - CR 1-9T9Y5D
                return true; // valid ebcdic unsigned zoned
            }
        } else {
            byte convertedByte = convertModifiedASCIISignByte(signByte);
            // at this point convertedByte is a strict ascii zoned byte
            byte convertedNibble = (byte)(convertedByte & 0xF0);
            
            // note that there is no dedicated nibble value for unsigned values in ascii strict or ascii modified zoned format
            if (convertedNibble == 0x30) {
                return true; // valid strict ascii positive 
            } else if (convertedNibble == (byte)0x70) {
                return false; // valid strict ascii negative
            }
        }
        throw new NumericException(DecodeUtils.hex(signByte) + ": invalid sign nibble");
    }
}
