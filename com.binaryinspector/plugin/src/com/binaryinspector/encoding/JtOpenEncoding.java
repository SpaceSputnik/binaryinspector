package com.binaryinspector.encoding;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Vector;

import com.ibm.as400.access.CharConverter;


public class JtOpenEncoding extends Encoding {
    /**
     * Populate a vector with jtOpen encodings
     */
    private static void createAllDescriptors() {
        createDescriptors(37, true);
        createDescriptors(256, true);
        createDescriptors(273, true);
        createDescriptors(277, true);
        createDescriptors(278, true);
        createDescriptors(280, true);
        createDescriptors(284, true);
        createDescriptors(285, true);
        createDescriptors(290, true);
        createDescriptors(297, true);
        createDescriptors(300, false, true);
        createDescriptors(367, false);
        createDescriptors(420, false);
        createDescriptors(423, true);
        createDescriptors(424, false);
        createDescriptors(425, false);
        createDescriptors(437, false);
        createDescriptors(500, true);
        createDescriptors(720, false);
        createDescriptors(737, false);
        createDescriptors(775, false);
        createDescriptors(813, false);
        createDescriptors(819, false);
        createDescriptors(833, true);
        createDescriptors(834, false);
        createDescriptors(835, false);
        createDescriptors(836, true);
        createDescriptors(837, false, true);
        createDescriptors(838, true);
        createDescriptors(850, false);
        createDescriptors(851, false);
        createDescriptors(852, false);
        createDescriptors(855, false);
        createDescriptors(857, false);
        createDescriptors(860, false);
        createDescriptors(861, false);
        createDescriptors(862, false);
        createDescriptors(863, false);
        createDescriptors(864, false);
        createDescriptors(865, false);
        createDescriptors(866, false);
        createDescriptors(869, false);
        createDescriptors(870, true);
        createDescriptors(871, true);
        createDescriptors(874, false);
        createDescriptors(875, true);
        createDescriptors(878, false);
        createDescriptors(880, true);
        createDescriptors(905, true);
        createDescriptors(912, false);
        createDescriptors(914, false);
        createDescriptors(915, false);
        createDescriptors(916, false);
        createDescriptors(920, false);
        createDescriptors(921, false);
        createDescriptors(922, false);
        createDescriptors(923, false);
        createDescriptors(924, true);
        createDescriptors(930, false);
        createDescriptors(933, false);
        createDescriptors(935, false);
        createDescriptors(937, false);
        createDescriptors(939, false);
        createDescriptors(1025, true);
        createDescriptors(1026, true);
        createDescriptors(1027, true);
        createDescriptors(1046, false);
        createDescriptors(1089, false);
        createDescriptors(1112, true);
        createDescriptors(1122, true);
        createDescriptors(1123, true);
        createDescriptors(1125, false);
        createDescriptors(1129, false);
        createDescriptors(1130, true);
        createDescriptors(1131, false);
        createDescriptors(1132, true);
        createDescriptors(1137, true);
        createDescriptors(1140, true);
        createDescriptors(1141, true);
        createDescriptors(1142, true);
        createDescriptors(1143, true);
        createDescriptors(1144, true);
        createDescriptors(1145, true);
        createDescriptors(1146, true);
        createDescriptors(1147, true);
        createDescriptors(1148, true);
        createDescriptors(1149, true);
        createDescriptors(1153, true);
        createDescriptors(1154, true);
        createDescriptors(1155, true);
        createDescriptors(1156, true);
        createDescriptors(1157, true);
        createDescriptors(1158, true);
        createDescriptors(1160, true);
        createDescriptors(1164, true);
        createDescriptors(1200, false);
        createDescriptors(1201, false);
        createDescriptors(1202, false);
        createDescriptors(1208, false);
        createDescriptors(1250, false);
        createDescriptors(1251, false);
        createDescriptors(1252, false);
        createDescriptors(1253, false);
        createDescriptors(1254, false);
        createDescriptors(1255, false);
        createDescriptors(1256, false);
        createDescriptors(1257, false);
        createDescriptors(1258, false);
        createDescriptors(1364, false);
        createDescriptors(1388, false);
        createDescriptors(1399, false);
        createDescriptors(4396, false, true);
        createDescriptors(4930, false);
        createDescriptors(4931, false);
        createDescriptors(4933, false, true);
        createDescriptors(4948, false);
        createDescriptors(4951, false);
        createDescriptors(4971, true);
        createDescriptors(5026, false);
        createDescriptors(5035, false);
        createDescriptors(5123, true);
        createDescriptors(5351, false);
        createDescriptors(8492, false, true);
        createDescriptors(8612, false);
        createDescriptors(9026, false);
        createDescriptors(9029, false, true);
        createDescriptors(9030, true);
        createDescriptors(9066, false);
        createDescriptors(12588, false, true);
        createDescriptors(12708, false);
        createDescriptors(13121, true);
        createDescriptors(13122, false);
        createDescriptors(13124, true);
        createDescriptors(13488, false);
        createDescriptors(16684, false, true);
        createDescriptors(17218, false);
        createDescriptors(17584, false);
        createDescriptors(21680, false);
        createDescriptors(28709, true);
        createDescriptors(57777, true);
        createDescriptors(61952, false);
        createDescriptors(62211, false);
        createDescriptors(62224, false);
        createDescriptors(62235, false);
        createDescriptors(62245, false);
        createDescriptors(62251, false);
    }
    private static LinkedHashMap<String, EncodingDescriptor> descriptors = new LinkedHashMap<String, EncodingDescriptor>();
    
    static {
        createAllDescriptors();
    }
    
    private boolean normalizeLatin = false;
    private CharConverter jtOpenConverter = null;    

    /**
     * Private constructor. Use create(...) method to create from outside of this class.
     * 
     * @param set
     */
    private JtOpenEncoding(EncodingDescriptor descr) {
        super(descr.name);
        try {
            jtOpenConverter = new CharConverter(descr.ccsid);
        } catch (UnsupportedEncodingException e) {
            // this should not happen because we work off a list of known encodings which
            // is consistent with the version of JTOpen we carry with us
            throw new RuntimeException(e);
        }
        
        ebcdic = getBytes("0")[0] == (byte)0xF0;
        spaceBytes = getBytes(" ");
        normalizeLatin = descr.normalizeLatin;
        singleByte = descr.singleByte;
        averageBytesPerChar = singleByte ? 1 : 2; // this is an imprecise guess
    }
    
    private static void createDescriptors(int ccsid, boolean singleByte) {
        createDescriptors(ccsid, singleByte, false);
    }
    
    private static void createDescriptors(int ccsid, boolean singleByte, boolean normalizeLatin) {
        EncodingDescriptor enc = new EncodingDescriptor(ccsid, singleByte, false);
        descriptors.put(enc.name.toLowerCase(), enc);
        if (normalizeLatin) {
            // if latin normalization is applicable add a codepage that will normalize
            enc = new EncodingDescriptor(ccsid, singleByte, true);
            descriptors.put(enc.name.toLowerCase(), enc);
        }
    }

    /**
     * Populate a list of encoding names and labels for design time
     * 
     * @param names
     * @param labels
     */
    public static void fillLabels(Vector<String> names, Vector<String> labels) {
        for (EncodingDescriptor descr : descriptors.values()) {
            names.add(descr.name);
            labels.add(addMultibyteDecoration(descr.name, ! descr.singleByte));
        }
    }

    /** 
     * Convert a byte array to a String
     * 
     * @param bytes
     * @param offset
     * @param length
     * @return a String
     */
    public final String getString(byte[] bytes, int offset, int length) {
        String res = jtOpenConverter.byteArrayToString(bytes, offset, length);
        if (normalizeLatin) {
            res = fromFullWidthLatin(res);
        }
        return res;
    }
    
    /**
     * Convert full width latin and special characters to their canonical Unicode counterparts.
     * Will also convert IDEOGRAPHIC SPACE' (U+3000) to a canonical space (U+2000).
     * 
     * Full width latin is an alternative set of Unicode chars that has wide glyphs.
     * It's a legacy susbset used in far east languages. Original purpose of it is too look
     * more natural together with the national characters. 
     * See more at http://en.wikipedia.org/wiki/Halfwidth_and_Fullwidth_Forms
     * @param str
     * @return
     */
    private static final String fromFullWidthLatin(String str) {
        return shiftCharRange(str, 0xFF01, 0xFF5E, -0xFEE0, '\u3000', ' ');
    }
    /**
     * Convert canonical latin and special Unicode characters to their full width counterparts.
     * Will also convert canonical space (U+2000) to IDEOGRAPHIC SPACE' (U+3000). 
     * 
     * @param str
     * @return
     */
    private static final String toFullWidthLatin(String str) {
        return shiftCharRange(str, 0x0021, 0x007E, 0xFEE0, ' ', '\u3000');
    }

    /**
     * Shift Unicode codes in a given range by a given value.
     * Also replace one char with another, so one out of range character may be processed.
     * 
     * @param src
     * @param start
     * @param end
     * @param shift
     * @param extraCharSrc
     * @param extraCharTarget
     * @return
     */
    private static String shiftCharRange(String src, int start, int end, int shift, char extraCharSrc, char extraCharTarget) {
    	StringBuilder res = new StringBuilder(src.length());
        char [] chars = src.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char ch = src.charAt(i);
            if (ch >= start && ch <= end) {
                int num = (int)ch;
                num+=shift;
                ch = (char)((int)ch + shift);
            }
            ch = ch == extraCharSrc ? extraCharTarget : ch; 
            res.append(ch);
        }
        return res.toString();
    }
    
    /**
     * Convert a string to a byte array
     * 
     * @return byte array
     */
    public final byte[] getBytes(String str) {
        if (normalizeLatin) {
            str = toFullWidthLatin(str);
        }
        return jtOpenConverter.stringToByteArray(str);
    }
    
    /**
     * Create an encoding object. This is the only proper way to create a JTOpenEncoding
     * 
     * @param name
     * @return
     */
    public static final Encoding createEncoding(String name) {
        EncodingDescriptor descr = descriptors.get(name.toLowerCase());
        return descr == null ? null : new JtOpenEncoding(descr);  
    }
}

class EncodingDescriptor {
    private static final String CCSID_PREF = "CCSID";
    int ccsid;
    String name; // label to be shown at design time

    boolean singleByte;
    boolean normalizeLatin;
    boolean canCorruptCopybook;
    
    EncodingDescriptor(int ccsid, boolean singleByte, boolean normalizeLatin) {
        this.ccsid = ccsid; 
        this.singleByte = singleByte;
        this.normalizeLatin = normalizeLatin;
        
        StringBuilder labelBuff = new StringBuilder(CCSID_PREF);
        labelBuff.append(' ');
        labelBuff.append(ccsid);
        
        if (normalizeLatin) {
            // don't localize, used as a value in resources
            labelBuff.append(" with latin normalization");
        }
        
        name = labelBuff.toString();
    }
}
