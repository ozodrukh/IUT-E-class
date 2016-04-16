package com.ozodrukh.eclass;

import com.squareup.duktape.Duktape;

/**
 * Inha Session Encoder is port of Javascript implementation
 * <a href="http://eclass.inha.uz/script/jQuery.inhaE.js"></a> that used in
 * authentication system as encryption purpose(?)
 */
public abstract class InhaSessionEncoder {
  private final static String JS_RAW_SESSION_ENCODER = "\n"
      + "    var keyString = \"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=\";\n"
      + "    var str1 = \"(!@$%%^&&^@)(*&^^&\";\n"
      + "    var str2 = \"*&^%#@$%%^$#)(**&*&\";\n"
      + "    var str3 = \")(*&^%$%^&*((())=+\";\n"
      + "    var str4 = \"&*^^*&*&(*(!+)((&&^&^*%^*\";\n"
      + "    var publickey = \"BCcd^%$%^&*(opqr%^$#)(*stuefgD^OPQRc^%$%^&*(opqrstudefgSTUV&^*EFGH@$%%ghij1234\";\n"
      + "    var rand_no = Math.floor(Math.random() * 10);\n"
      + "    var KeyInEncode = function (string) {\n"
      + "        string = string.replace(/\\x0d\\x0a/g, \"\\x0a\");\n"
      + "        var output = \"\";\n"
      + "        for (var n = 0; n < string.length; n++) {\n"
      + "            var c = string.charCodeAt(n);\n"
      + "            if (c < 128) {\n"
      + "                output += String.fromCharCode(c)\n"
      + "            } else if ((c > 127) && (c < 2048)) {\n"
      + "                output += String.fromCharCode((c >> 6) | 192);\n"
      + "                output += String.fromCharCode((c & 63) | 128)\n"
      + "            } else {\n"
      + "                output += String.fromCharCode((c >> 12) | 224);\n"
      + "                output += String.fromCharCode(((c >> 6) & 63) | 128);\n"
      + "                output += String.fromCharCode((c & 63) | 128)\n"
      + "            }\n"
      + "        }\n"
      + "        return output\n"
      + "    };\n"
      + "    function SessionInhaE(input) {\n"
      + "            var output = \"\";\n"
      + "            var output2 = \"\";\n"
      + "            var chr1, chr2, chr3, enc1, enc2, enc3, enc4;\n"
      + "            var i = 0;\n"
      + "            var keychain = \"\";\n"
      + "            var j = 0;\n"
      + "            while (j < 3) {\n"
      + "                rand_no = Math.floor(Math.random() * 10) + \"\";\n"
      + "                keychain = keychain + publickey.substring(rand_no, rand_no + j) + rand_no;\n"
      + "                j++\n"
      + "            }\n"
      + "            keychain = keychain + \"/\";\n"
      + "            input = KeyInEncode(input);\n"
      + "            while (i < input.length) {\n"
      + "                chr1 = input.charCodeAt(i++);\n"
      + "                chr2 = input.charCodeAt(i++);\n"
      + "                chr3 = input.charCodeAt(i++);\n"
      + "                enc1 = chr1 >> 2;\n"
      + "                enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);\n"
      + "                enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);\n"
      + "                enc4 = chr3 & 63;\n"
      + "                if (isNaN(chr2)) {\n"
      + "                    enc3 = enc4 = 64\n"
      + "                } else if (isNaN(chr3)) {\n"
      + "                    enc4 = 64\n"
      + "                }\n"
      + "                output = output + str1.substring(9, 11) + keyString.charAt(enc1) + str3.substring(16, 18) + keyString.charAt(enc2) + keyString.charAt(enc3) + keyString.charAt(enc4) + str2.substring(10, 12)\n"
      + "            }\n"
      + "            return keychain + str3.substring(16, 18) + output + str4.substring(11, 13)\n"
      + "        };\n"
      + "\n";


  private static final String ACCEPTED_CHARS =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
  private static final String PUBLIC_KEY =
    "BCcd^%$%^&*(opqr%^$#)(*stuefgD^OPQRc^%$%^&*(opqrstudefgSTUV&^*EFGH@$%%ghij1234";

  private static final String SUGAR1 = "(!@$%%^&&^@)(*&^^&";
  private static final String SUGAR2 = "*&^%#@$%%^$#)(**&*&";
  private static final String SUGAR3 = ")(*&^%$%^&*((())=+";
  private static final String SUGAR4 = "&*^^*&*&(*(!+)((&&^&^*%^*";

  /**
   * Magic from Korean friends.
   *
   * Have no idea of purpose of this O_o
   *
   * @param input Text to encrypt
   * @return Transformed | Encrypted input text
   */
  public static String encode(String input) {
    Duktape duktape = null;
    try{
      duktape = Duktape.create();
      duktape.evaluate(JS_RAW_SESSION_ENCODER);
      return duktape.evaluate("SessionInhaE('" + input + "')");
    }
    catch (OutOfMemoryError e){
      return encodeInternal(input);
    }
    finally {
      if(duktape != null) {
        duktape.close();
      }
    }
  }

  /**
   * @deprecated Instead better to use raw js function
   */
  private static String encodeInternal(String input){
    input += " ";
    StringBuilder output = new StringBuilder();
    int chr1, chr2, chr3, enc1, enc2, enc3, enc4;
    int i = 0, j = 0;
    StringBuilder keychain = new StringBuilder();
    while (j < 3) {
      int rand_no = (int) Math.floor(Math.random() * 10);
      keychain.append(keychain)
          .append(PUBLIC_KEY.substring(rand_no, Math.min(rand_no * 10 + j, PUBLIC_KEY.length())))
          .append(rand_no);
      j++;
    }
    keychain.append("/");
    input = keyInCode(input);
    final int length = input.length();
    while (i < length) {
      chr1 = input.codePointAt(i++);
      chr2 = length > i ? input.codePointAt(i++) : -1;
      chr3 = length > i ? input.codePointAt(i++) : -1;
      enc1 = chr1 >> 2;
      enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
      enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
      enc4 = chr3 & 63;
      if (chr2 == -1) {
        enc3 = enc4 = 64;
      } else if (chr3 == -1) {
        enc4 = 64;
      }

      final int acceptCharsLength = ACCEPTED_CHARS.length();
      output.append(SUGAR1.substring(9, 11))
          .append(acceptCharsLength > enc1 && enc1 != -1 ? ACCEPTED_CHARS.charAt(enc1) : "")
          .append(SUGAR3.substring(16, 18))
          .append(acceptCharsLength > enc2 && enc2 != -1 ? ACCEPTED_CHARS.charAt(enc2) : "")
          .append(acceptCharsLength > enc3 && enc3 != -1 ? ACCEPTED_CHARS.charAt(enc3) : "")
          .append(acceptCharsLength > enc4 && enc4 != -1 ? ACCEPTED_CHARS.charAt(enc4) : "")
          .append(SUGAR2.substring(10, 12));
    }
    return keychain.toString() + SUGAR3.substring(16, 18) + output.toString() + SUGAR4.substring(11, 13);
  }

  private static String keyInCode(String input) {
    input = input.replaceAll("/\\x0d\\x0a/g", "\\x0a");
    StringBuilder output = new StringBuilder();
    for (int n = 0; n < input.length(); n++) {
      int c = input.codePointAt(n);
      if (c < 128) {
        output.appendCodePoint(c);
      } else if ((c > 127) && (c < 2048)) {
        output.appendCodePoint((c >> 6) | 192);
      } else {
        output.appendCodePoint((c >> 12) | 224);
        output.appendCodePoint(((c >> 6) & 63) | 128);
        output.appendCodePoint((c & 63) | 128);
      }
    }
    return output.toString();
  }
}

