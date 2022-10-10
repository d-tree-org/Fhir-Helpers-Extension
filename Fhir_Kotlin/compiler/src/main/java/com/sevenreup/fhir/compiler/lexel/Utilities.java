package com.sevenreup.fhir.compiler.lexel;

public class Utilities {
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean noString(String v) {
        return v == null || v.equals("");
    }

    public static boolean isInteger(String string) {
        if (isBlank(string)) {
            return false;
        }
        String value = string.startsWith("-") ? string.substring(1) : string;
        for (char next : value.toCharArray()) {
            if (!Character.isDigit(next)) {
                return false;
            }
        }
        // check bounds -2,147,483,648..2,147,483,647
        if (value.length() > 10)
            return false;
        if (string.startsWith("-")) {
            if (value.length() == 10 && string.compareTo("2147483648") > 0)
                return false;
        } else {
            if (value.length() == 10 && string.compareTo("2147483647") > 0)
                return false;
        }
        return true;
    }
}
