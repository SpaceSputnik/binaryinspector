# Binary Inspector 2.0


Binary Inspector is an Eclipse Plugin that adds tooling to examine raw binary data.


# Usage
Data can be imported from a file or pasted directly into GUI as a hex or Base64 text.
A number of decoders can be configured, including most common data formats and some less common IBM Mainframe ones:


-Binary integers, Big and Little Endian, with optional implied decimal point.
-IEEE as well as IBM Hexadecimal floating point.
-Text in a large variety of character sets, including less common IBM CCSIDs


You can view typed values, search for a specific value, perform charset analysis and more.


# Supported Character Sets
ASCII,  Cp1252,
ISO8859_1,
UnicodeBig,
UnicodeBigUnmarked,
UnicodeLittle,
UnicodeLittleUnmarked,
UTF8,
UTF-16,
UTF-16LE,
UTF-16BE,
Big5,
Big5_HKSCS,
Cp037,
Cp273,
Cp277,
Cp278,
Cp280,
Cp284,
Cp285,
Cp297,
Cp420,
Cp424,
Cp437,
Cp500,
Cp737,
Cp775,
Cp838,
Cp850,
Cp852,
Cp855,
Cp856,
Cp857,
Cp858,
Cp860,
Cp861,
Cp862,
Cp863,
Cp864,
Cp865,
Cp866,
Cp868,
Cp869,
Cp870,
Cp871,
Cp874,
Cp875,
Cp918,
Cp921,
Cp922,
Cp930,
Cp933,
Cp935,
Cp937,
Cp939,
Cp942,
Cp942C,
Cp943,
Cp943C,
Cp948,
Cp949,
Cp949C,
Cp950,
Cp964,
Cp970,
Cp1006,
Cp1025,
Cp1026,
Cp1046,
Cp1047,
Cp1097,
Cp1098,
Cp1112,
Cp1122,
Cp1123,
Cp1124,
Cp1140,
Cp1141,
Cp1142,
Cp1143,
Cp1144,
Cp1145,
Cp1146,
Cp1147,
Cp1148,
Cp1149,
Cp1250,
Cp1251,
Cp1253,
Cp1254,
Cp1255,
Cp1256,
Cp1257,
Cp1258,
Cp1381,
Cp1383,
Cp33722,
EUC_CN,
EUC_JP,
EUC_JP_LINUX,
EUC_KR,
EUC_TW,
GBK,
ISO2022CN,
ISO2022CN_CNS,
ISO2022CN_GB,
ISO2022JP,
ISO2022KR,
ISO8859_2,
ISO8859_3,
ISO8859_4,
ISO8859_5,
ISO8859_6,
ISO8859_7,
ISO8859_8,
ISO8859_9,
ISO8859_13,
ISO8859_15,
ISO8859_15_FDIS,
JIS0201,
JIS0208,
JIS0212,
JISAutoDetect,
Johab,
KOI8_R,
MS874,
MS932,
MS936,
MS949,
MS950,
MacArabic,
MacCentralEurope,
MacCroatian,
MacCyrillic,
MacDingbat,
MacGreek,
MacHebrew,
MacIceland,
MacRoman,
MacRomania,
MacSymbol,
MacThai,
MacTurkish,
MacUkraine,
SJIS,
TIS62


       CCSID 37
       CCSID 256
       CCSID 273
       CCSID 277
       CCSID 278
       CCSID 280
       CCSID 284
       CCSID 285
       CCSID 290
       CCSID 297
       CCSID 300
       CCSID 367
       CCSID 420
       CCSID 423
       CCSID 424
       CCSID 425
       CCSID 437
       CCSID 500
       CCSID 720
       CCSID 737
       CCSID 775
       CCSID 813
       CCSID 819
       CCSID 833
       CCSID 834
       CCSID 835
       CCSID 836
       CCSID 837
       CCSID 838
       CCSID 850
       CCSID 851
       CCSID 852
       CCSID 855
       CCSID 857
       CCSID 860
       CCSID 861
       CCSID 862
       CCSID 863
       CCSID 864
       CCSID 865
       CCSID 866
       CCSID 869
       CCSID 870
       CCSID 871
       CCSID 874
       CCSID 875
       CCSID 878
       CCSID 880
       CCSID 905
       CCSID 912
       CCSID 914
       CCSID 915
       CCSID 916
       CCSID 920
       CCSID 921
       CCSID 922
       CCSID 923
       CCSID 924
       CCSID 930
       CCSID 933
       CCSID 935
       CCSID 937
       CCSID 939
       CCSID 1025
       CCSID 1026
       CCSID 1027
       CCSID 1046
       CCSID 1089
       CCSID 1112
       CCSID 1122
       CCSID 1123
       CCSID 1125
       CCSID 1129
       CCSID 1130
       CCSID 1131
       CCSID 1132
       CCSID 1137
       CCSID 1140
       CCSID 1141
       CCSID 1142
       CCSID 1143
       CCSID 1144
       CCSID 1145
       CCSID 1146
       CCSID 1147
       CCSID 1148
       CCSID 1149
       CCSID 1153
       CCSID 1154
       CCSID 1155
       CCSID 1156
       CCSID 1157
       CCSID 1158
       CCSID 1160
       CCSID 1164
       CCSID 1200
       CCSID 1201
       CCSID 1202
       CCSID 1208
       CCSID 1250
       CCSID 1251
       CCSID 1252
       CCSID 1253
       CCSID 1254
       CCSID 1255
       CCSID 1256
       CCSID 1257
       CCSID 1258
       CCSID 1364
       CCSID 1388
       CCSID 1399
       CCSID 4396
       CCSID 4930
       CCSID 4931
       CCSID 4933
       CCSID 4948
       CCSID 4951
       CCSID 4971
       CCSID 5026
       CCSID 5035
       CCSID 5123
       CCSID 5351
       CCSID 8492
       CCSID 8612
       CCSID 9026
       CCSID 9029
       CCSID 9030
       CCSID 9066
       CCSID 12588
       CCSID 12708
       CCSID 13121
       CCSID 13122
       CCSID 13124
       CCSID 13488
       CCSID 16684
       CCSID 17218
       CCSID 17584
       CCSID 21680
       CCSID 28709
       CCSID 57777
       CCSID 61952
       CCSID 62211
       CCSID 62224
       CCSID 62235
       CCSID 62245
       CCSID 62251


## Copyright


See LICENSE and AUTHORS files for details.
