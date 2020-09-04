package com.binaryinspector.encoding;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Vector;


public class JavaEncoding extends Encoding {
    // TODO: test preservation of latin set + carriage return
    private static final String[] javaEncodings = new String[] {
        "ASCII",
        "Cp1252",
        "ISO8859_1",
        "UnicodeBig",
        "UnicodeBigUnmarked",
        "UnicodeLittle",
        "UnicodeLittleUnmarked",
        "UTF8",
        "UTF-16",
        "UTF-16LE",
        "UTF-16BE",        
        "Big5",
        "Big5_HKSCS",
        "Cp037",
        "Cp273",
        "Cp277",
        "Cp278",
        "Cp280",
        "Cp284",
        "Cp285",
        "Cp297",
        "Cp420",
        "Cp424",
        "Cp437",
        "Cp500",
        "Cp737",
        "Cp775",
        "Cp838",
        "Cp850",
        "Cp852",
        "Cp855",
        "Cp856",
        "Cp857",
        "Cp858",
        "Cp860",
        "Cp861",
        "Cp862",
        "Cp863",
        "Cp864",
        "Cp865",
        "Cp866",
        "Cp868",
        "Cp869",
        "Cp870",
        "Cp871",
        "Cp874",
        "Cp875",
        "Cp918",
        "Cp921",
        "Cp922",
        "Cp930",
        "Cp933",
        "Cp935",
        "Cp937",
        "Cp939",
        "Cp942",
        "Cp942C",
        "Cp943",
        "Cp943C",
        "Cp948",
        "Cp949",
        "Cp949C",
        "Cp950",
        "Cp964",
        "Cp970",
        "Cp1006",
        "Cp1025",
        "Cp1026",
        "Cp1046",
        "Cp1047",
        "Cp1097",
        "Cp1098",
        "Cp1112",
        "Cp1122",
        "Cp1123",
        "Cp1124",
        "Cp1140",
        "Cp1141",
        "Cp1142",
        "Cp1143",
        "Cp1144",
        "Cp1145",
        "Cp1146",
        "Cp1147",
        "Cp1148",
        "Cp1149",
        "Cp1250",
        "Cp1251",
        "Cp1253",
        "Cp1254",
        "Cp1255",
        "Cp1256",
        "Cp1257",
        "Cp1258",
        "Cp1381",
        "Cp1383",
        "Cp33722",
        "EUC_CN",
        "EUC_JP",
        "EUC_JP_LINUX",
        "EUC_KR",
        "EUC_TW",
        "GBK",
        "ISO2022CN",
        "ISO2022CN_CNS",
        "ISO2022CN_GB",
        "ISO2022JP",
        "ISO2022KR",
        "ISO8859_2",
        "ISO8859_3",
        "ISO8859_4",
        "ISO8859_5",
        "ISO8859_6",
        "ISO8859_7",
        "ISO8859_8",
        "ISO8859_9",
        "ISO8859_13",
        "ISO8859_15",        
        "ISO8859_15_FDIS",
        "JIS0201",
        "JIS0208",
        "JIS0212",
        "JISAutoDetect",
        "Johab",
        "KOI8_R",
        "MS874",
        "MS932",
        "MS936",
        "MS949",
        "MS950",
        "MacArabic",
        "MacCentralEurope",
        "MacCroatian",
        "MacCyrillic",
        "MacDingbat",
        "MacGreek",
        "MacHebrew",
        "MacIceland",
        "MacRoman",
        "MacRomania",
        "MacSymbol",
        "MacThai",
        "MacTurkish",
        "MacUkraine",
        "SJIS",
        "TIS62"
    };

    /**
     * Populate a list of encoding names and labels for design time
     * 
     * @param names
     * @param labels
     */
    // TODO: check compatibility with prev. versions
    public static void fillLabels(Vector<String> names, Vector<String> labels) {
        for (String encoding : javaEncodings) {
            // we cannot simply add every encoding because the current environment may not support them all
            Charset set = getJavaCharset(encoding);
            if(set != null) {
                try {
                    CharsetEncoder encoder = set.newEncoder();
                    labels.add(addMultibyteDecoration(encoding, encoder.maxBytesPerChar() != 1));
                    names.add(encoding);
                } catch (UnsupportedOperationException e) {
                    // some encodings prohibit encoder creation, we can't use those encodings
                }
            }
        }
    }

    /**
     * Private constructor. Use create(...) method to create from outside of this class.
     * 
     * @param set
     */
    private JavaEncoding(Charset set, String name) {
        super(name);
        ebcdic = getBytes("0")[0] == (byte)0xF0;
        spaceBytes = getBytes(" ");
        CharsetEncoder encoder = set.newEncoder();
        averageBytesPerChar = encoder.averageBytesPerChar();
        singleByte = encoder.maxBytesPerChar() == 1;
    }
    
    private static Charset getJavaCharset(String name) {
        Charset set;
        try {
            set = Charset.forName(name);
        } catch (IllegalCharsetNameException e) {
            return null;
        } catch (UnsupportedCharsetException e) {
            return null;
        }
        return set;
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
        String res;
        try {
            res = new String(bytes, offset, length, name);
        } catch (UnsupportedEncodingException e) {
            // shouldn't happen, the encoding name was checked
            throw new RuntimeException(e);
        }
        return res;
    }
    
    /**
     * Convert a string to a byte array
     * 
     * @return byte array
     */
    public byte[] getBytes(String str) {
        try {
            return str.getBytes(name);
        } catch (UnsupportedEncodingException e) {
            // shouldn't happen, the encoding name was checked
            throw new RuntimeException(e);
        }
    }

    /**
     * Create an encoding object. This is the only proper way to create a JavaEncoding
     * 
     * @param name
     * @return
     */
    public static final Encoding createEncoding(String name) {
        Charset set = getJavaCharset(name);
        return set == null ? null : new JavaEncoding(set, name);  
    }
}
