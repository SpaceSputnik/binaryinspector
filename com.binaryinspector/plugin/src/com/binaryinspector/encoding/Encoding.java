package com.binaryinspector.encoding;

import java.util.Vector;

/**
 * This class holds info about a specific encoding (Java, JTOpen, multibyte and other attributes).
 * Also holds static lists of available and supported encodings.
 *
 */
public abstract class Encoding {
    protected float averageBytesPerChar;// average bytes per char
    protected boolean ebcdic;           // true for ebcdic family
    protected byte[] spaceBytes;
    protected boolean singleByte;
    protected String name;

    protected static String addMultibyteDecoration(String label, boolean multibyte) {
        return multibyte ? label + "*" : label;
    }

    private static String [] encodingLabels = null; // encoding labels, will be displayed in designer
    private static String [] encodingNames = null; // encoding names, will be used in projects
    
    // frequently used encodings
    public static Encoding utf16Le = JavaEncoding.createEncoding("UTF-16LE");
    public static Encoding utf16Be = JavaEncoding.createEncoding("UTF-16BE");
    
    /**
     * Constructor
     * 
     * @param name - encoding name
     */
    protected Encoding(String name) {
        this.name = name;        
    }
    
    private static void fillNames() {
        if (encodingLabels != null) {
            return; // already populated
        }
        Vector<String> namesVect = new Vector<String>(200);
        Vector<String> labelsVect = new Vector<String>(200);
        JavaEncoding.fillLabels(namesVect, labelsVect);
        JtOpenEncoding.fillLabels(namesVect, labelsVect);
        encodingNames = namesVect.toArray(new String[0]);
        encodingLabels = labelsVect.toArray(new String[0]);
        return;
    }

    
    public final boolean isSingleByte() {
        return singleByte;
    }

    /**
     * Returns all encoding labels. Meant for design time use.  
     */
    public static final String[] getLabels() {
        fillNames();
        return encodingLabels;
    }

    /**
     * Returns all encoding names. Meant for design time use.  
     */
    public static final String[] getNames() {
        fillNames();
        return encodingNames;
    }

    public final String getName() {
        return name;
    }
    
    /**
     * Convert a string to a byte array
     * 
     * @return byte array
     */
    public abstract byte[] getBytes(String str);
    
    /** 
     * Convert a byte array to a String
     * 
     * @param bytes
     * @param offset
     * @param length
     * @return a String
     */
    public abstract String getString(byte[] bytes, int offset, int length);
    
    public final String getString(byte[] bytes) {
        return getString(bytes, 0, bytes.length);
    }
    
    /**
     * Return true for ebcdic family of encodings
     * 
     * @return
     */
    public final boolean isEbcdic() {
        return ebcdic;        
    }
    /**
     * Return the byte representation of a space character.
     * 
     * @return
     */
    public final byte[] getSpaceBytes() {
        return spaceBytes;
    }
    
    /**
     * Return the average number of bytes per character.
     * Note that for java encodings it comes from Java, but JTOpen does not make this number available, so for
     * single byte encodings we return 1 and 2 for others. 
     * This can be imprecise for variable encodings such as ccsid 300 (shift-in, shift-out).
     * 
     * @return the average number of bytes per char 
     */
    public final float getAverageBytesPerChar() {
        return averageBytesPerChar; 
    }
    
    /**
     * Create and Encoding object for a java or jtOpen encoding 
     * 
     * @param name
     * @return
     */
    public final static Encoding create(String name) {
        name = name.trim();
        if (utf16Be.name.equals(name)) {
        	return utf16Be; 
        }
        if (utf16Le.name.equals(name)) {
        	return utf16Le; 
        }

        Encoding enc = JavaEncoding.createEncoding(name);
        if (enc == null) {
            enc = JtOpenEncoding.createEncoding(name);
        }
        return enc;
    }
}
