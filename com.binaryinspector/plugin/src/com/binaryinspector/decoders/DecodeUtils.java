package com.binaryinspector.decoders;

import java.io.*;
import java.util.*;

import org.eclipse.swt.graphics.Point;

public class DecodeUtils {
    static char hexadecimals[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f' };
	public static DecoderFactory decodeFactory = new DecoderFactory();
	
	public static int findByteByTextPos(String hexDigits, int textPos) {
		int byteIndex = -1;
		boolean firstDigitInByte = true;

		int end = Math.min(textPos + 1, hexDigits.length());
		for (int i = 0; i < end && i < hexDigits.length(); i++) {
			char ch = hexDigits.charAt(i);
			if (! isHexDigit(ch)) {
				continue;
			}
			if (firstDigitInByte) {
				byteIndex++;
			}
			firstDigitInByte = ! firstDigitInByte;
		}
		return byteIndex;
	}
	
	public static byte[] parseHexStringStrict(String hexDigits) throws EnvelopeException {
		return parseHexString(hexDigits, false);
	}
	
	public static byte[] parseHexString(String hexDigits) throws EnvelopeException {
		return parseHexString(hexDigits, true);
	}
	
	public static byte[] parseHexString(String hexDigits, boolean strict) throws EnvelopeException {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < hexDigits.length(); i++) {
			char ch = hexDigits.charAt(i);
			if (isHexDigit(ch)) {
				boolean found = false;
				for (char hex : hexadecimals) {
					if (ch == hex || Character.toLowerCase(ch) == hex) {
						found = true;
						break;
					}
				}
				buf.append(ch);
			} else if (strict && ! Character.isWhitespace(ch)){
				throw new EnvelopeException("\"" + ch + "\" is not a valid hex digit");
			}
		}
		
		hexDigits = buf.toString();
		
		if (hexDigits.length() % 2 != 0) {
			// shouldn't happen, a safeguard
			throw new EnvelopeException("Number of hex digits must be even");
		}
		int bytesNum = hexDigits.length() / 2;
		byte[] bytes = new byte[bytesNum];

		for (int i = 0; i < bytesNum; i++) {
			// parseByte has a problem hence we're using Integer.parseInt
			bytes[i] = (byte) Integer.parseInt(
					hexDigits.substring(i * 2, i * 2 + 2), 16);
		}
		return bytes;
	}

	private enum HexParseSelectionState {
		lookingForStartPos,
		lookingForStart,
		lookingForEnd,
		done
	}
	
	public static Point searchBytePositions(String hexDigits, int gotoByte, int fromTextPos, int selectBytes, boolean truncate) throws EnvelopeException {
		boolean firstDigitInByte = true;
		int byteStart = -1;
		int byteEnd = -1;
		int byteIndex = 0;
		HexParseSelectionState selState = fromTextPos > 0 ? HexParseSelectionState.lookingForStartPos 
				: (gotoByte >= 0) ? HexParseSelectionState.lookingForStart : HexParseSelectionState.done;
		int selStart = -1;
		int selEnd = -1;
		int end = hexDigits.length();
		for (int i = 0; i < end; i++) {
			char ch = hexDigits.charAt(i);
			if (! isHexDigit(ch)) {
				// skip non-hex stuff
			} else {
				boolean done = false;
				switch (selState) {
				case done:
					done = true;
					break;
				case lookingForStartPos:
					if (i >= fromTextPos) {
						selState = HexParseSelectionState.lookingForStart;
					}
					break;
				}
				if (done) {
					break;
				}
				
				if (firstDigitInByte) {
					byteStart = i;
				}
				
				switch (selState) {
				case lookingForStart:
					if (byteIndex == gotoByte) {
						selState = selectBytes > 0 ? HexParseSelectionState.lookingForEnd : HexParseSelectionState.done;
						selStart = byteStart;
						byteIndex = 0;
					}
					break;
				}
				
				if (! firstDigitInByte) {
					byteEnd = i + 1;
					switch (selState) {
					case lookingForStart:
						byteIndex++;
						break;

					case lookingForEnd:
						if (byteIndex >= selectBytes - 1) {
							selEnd = i + 1;
							selState = HexParseSelectionState.done;
						}
						byteIndex++;
						break;
					}
				}
				firstDigitInByte = ! firstDigitInByte; 
			}
		}
		
		if (truncate) { 
			if (selEnd < 0) {
				if (byteEnd > 0) {
					selEnd = byteEnd;
				} else {
					selEnd = hexDigits.length();
				}
			}
			if (selStart < 0) {
				selStart = selEnd;
			}
		} else {
			// TODO: improper exception class
			switch (selState) {
			case lookingForStartPos:
				throw new EnvelopeException("Position #" + fromTextPos + " was not found, not enough data");
			case lookingForStart:
				throw new EnvelopeException("Offset #" + gotoByte + " was not found, not enough data");
			case lookingForEnd:	
				throw new EnvelopeException("Could not select " + selectBytes + " bytes, not enough data");
			}
		}
		
		return new Point(selStart, selEnd < 0 ? selStart : selEnd);
	}

	public static boolean isHexDigit(char ch) {
		for (char hex : hexadecimals) {
			if (ch == hex || Character.toLowerCase(ch) == hex) {
				return true;
			}
		}
		return false;
	}
	
    public static String getHex(byte [] array, boolean delimit) {
    	StringBuilder sb = new StringBuilder();
    	for(int i = 0 ; i < array.length ; i++) {
    	   sb.append(hex(array[i]));
    	   if(delimit && i < array.length) {
    		   sb.append(' ');
    	   }
    	}
    	return sb.toString();
    }
    
    public static String hex(byte b) {
        return new String(new char[] {hexadecimals[(b >> 4) & 0x0f],
                hexadecimals[b & 0x0f]});
    }    

	public static List<DecodeResult> decode(String hexDigits) {
		byte[] bytes;
		ArrayList<DecodeResult> allResults = new ArrayList<DecodeResult>();
		
		try {
			bytes = parseHexString(hexDigits);
		} catch (EnvelopeException e) {
			DecodeResult res = new DecodeResult();
			res.setError(e.getMessage());
			allResults.add(res);
			return allResults;
		}		
		if (bytes.length > 0) {
			for (Decoder d : decodeFactory.getEnabledDecoders()) {
				DecodeResult res = d.decode(bytes);
				res.setDecoder(d);
				res.setDescription(d.toString());
				allResults.add(res);
			}
		}

		return allResults;
	}
	

	public static String readFile(InputStream stream) {
    	BufferedReader br = new BufferedReader(new InputStreamReader(stream));
     	StringBuilder sb = new StringBuilder();
 
    	String line;
    	try {
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 
    	return sb.toString();
	}
	
	
	// TODO: unused?
	public static void writeFile(String path, String s) throws IOException {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(path));
			out.write(s);
		} finally {
			if (out != null) {
				out.close();
			}
		}	
	}
	
	public static Integer parseIntOrHex(String s) {
		if (s == null || s.isEmpty()) {
			return null;
		}
		char c = s.charAt(0);
		try {
			if (Character.toLowerCase(c) == 'x') {
				// hex
				s = s.substring(1);
				return Integer.parseInt(s, 16);
			} else {
				return Integer.parseInt(s);
			}
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	public static String renderIntOrHex(int value, boolean hex) {
		if (hex) {
			return "x" + Integer.toHexString(value);
		} else {
			return Integer.toString(value);
		}
	}
	
    /**
     * Remove leading zeros
     * 
     * @param str
     * @return
     */
    public static String removeLeadingZeroes(String str) {
        int i;
        for (i = 0; i < str.length(); i++) {
            if (str.charAt(i) != '0') {
                break;
            }
        }
        return i == str.length() ? "" : str.substring(i);
    }
    
    /**
     * Remove trailing zeroes
     * @param str
     * @return
     */
    public static String removeTrailingZeroes(String str) {
        int i;
        for (i = str.length() - 1; i >= 0; i--) {
            if (str.charAt(i) != '0') {
                break;
            }
        }
        return i < 0 ? "" : str.substring(0, i + 1);
    }
    
    public static byte[] readBytesFromFile(File file) throws IOException {
        // Get the size of the file
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            throw new IOException("File is too long to be loaded");
        }
        FileInputStream fs = null;
        byte[] bytes = new byte[(int)length];
        try {
	        fs = new FileInputStream(file);
	        // Read in the bytes
	        int offset = 0;
	        while (true) { 
	        	if (offset == bytes.length) {
	        		bytes = Arrays.copyOf(bytes, offset + 1024 * 10);
	        	}
	        	int numRead = fs.read(bytes, offset, bytes.length - offset);
	        	if (numRead == -1) {
	        		break;
	        	}
	            offset += numRead;
	        }
	        if (offset != bytes.length) {
	        	bytes = Arrays.copyOf(bytes, offset);
	        }
        } finally {
        	if (fs != null) {
        		try {
        			fs.close();
        		} catch (IOException e1) {
        			e1.printStackTrace();
        		}
        	}
        }
        return bytes;
    }
}


