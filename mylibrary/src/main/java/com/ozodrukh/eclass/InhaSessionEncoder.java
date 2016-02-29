package com.ozodrukh.eclass;

/**
 * Inha Session Encoder is port of Javascript implementation
 * <a href="http://eclass.inha.uz/script/jQuery.inhaE.js"></a> that used in
 * authentication system as encryption purpose(?)
 */
public abstract class InhaSessionEncoder {
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

