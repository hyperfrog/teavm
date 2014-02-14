/*
 *  Copyright 2014 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.classlib.java.lang;

import org.teavm.javascript.ni.Rename;

/**
 *
 * @author Alexey Andreev
 */
public class TInteger extends TNumber implements TComparable<TInteger> {
    public static final int SIZE = 32;
    public static final int MIN_VALUE = 0x80000000;
    public static final int MAX_VALUE = 0x7FFFFFFF;
    public static final TClass<TInteger> TYPE = TClass.intClass();
    private static TInteger[] integerCache;
    private int value;

    public TInteger(int value) {
        this.value = value;
    }

    public TInteger(TString s) throws NumberFormatException {
        this(parseInt(s));
    }

    public static TString toString(int i, int radix) {
        if (radix < MIN_VALUE || radix > MAX_VALUE) {
            radix = 10;
        }
        return TString.wrap(new TAbstractStringBuilder(20).append(i, radix).toString());
    }

    public static TString toHexString(int i) {
        return toString(i, 16);
    }

    public static TString toOctalString(int i) {
        return toString(i, 8);
    }

    public static TString toBinaryString(int i) {
        return toString(i, 2);
    }

    public static TString toString(int i) {
        return toString(i, 10);
    }

    public static int parseInt(TString s, int radix) throws TNumberFormatException {
        if (radix < TCharacter.MIN_RADIX || radix > TCharacter.MAX_RADIX) {
            throw new TNumberFormatException(TString.wrap("Illegal radix: " + radix));
        }
        if (s == null || s.isEmpty()) {
            throw new TNumberFormatException(TString.wrap("String is null or empty"));
        }
        boolean negative = false;
        int index = 0;
        switch (s.charAt(0)) {
            case '-':
                negative = true;
                index = 1;
                break;
            case '+':
                index = 1;
                break;
        }
        int value = 0;
        while (index < s.length()) {
            int digit = TCharacter.digit(s.charAt(index++));
            if (digit < 0) {
                throw new TNumberFormatException(TString.wrap("String contains invalid digits: " + s));
            }
            if (digit >= radix) {
                throw new TNumberFormatException(TString.wrap("String contains digits out of radix " + radix +
                        ": " + s));
            }
            value = radix * value + digit;
            if (value < 0) {
                if (index == s.length() && value == MIN_VALUE && negative) {
                    return MIN_VALUE;
                }
                throw new TNumberFormatException(TString.wrap("The value is too big for int type: " + s));
            }
        }
        return negative ? -value : value;
    }

    public static int parseInt(TString s) throws TNumberFormatException {
        return parseInt(s, 10);
    }

    public static TInteger valueOf(TString s, int radix) throws TNumberFormatException {
        return valueOf(parseInt(s, radix));
    }

    public static TInteger valueOf(TString s) throws TNumberFormatException {
        return valueOf(s, 10);
    }

    public static TInteger valueOf(int i) {
        if (i >= -128 && i <= 127) {
            ensureIntegerCache();
            return integerCache[i + 128];
        }
        return new TInteger(i);
    }

    private static void ensureIntegerCache() {
        if (integerCache == null) {
            integerCache = new TInteger[256];
            for (int j = 0; j < integerCache.length; ++j) {
                integerCache[j - 128] = new TInteger(j);
            }
        }
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    @Rename("toString")
    public TString toString0() {
        return toString(value);
    }

    @Override
    public int hashCode() {
        return (value >>> 4) ^ (value << 28) ^ (value << 8) ^ (value >>> 24);
    }

    @Override
    public boolean equals(TObject other) {
        if (this == other) {
            return true;
        }
        return other instanceof TInteger && ((TInteger)other).value == value;
    }

    public static TInteger getInteger(TString nm) {
        return getInteger(nm, null);
    }

    public static TInteger getInteger(TString nm, int val) {
        return getInteger(nm, val);
    }

    public static TInteger getInteger(TString nm, TInteger val) {
        TString result = TSystem.getProperty(nm);
        return result != null ? TInteger.valueOf(result) : val;
    }

    public static TInteger decode(TString nm) throws TNumberFormatException {
        if (nm == null || nm.isEmpty()) {
            throw new TNumberFormatException(TString.wrap("Can't parse empty or null string"));
        }
        int index = 0;
        boolean negaive = false;
        if (nm.charAt(index) == '+') {
            ++index;
        } else if (nm.charAt(index) == '-') {
            ++index;
            negaive = true;
        }
        if (index >= nm.length()) {
            throw new TNumberFormatException(TString.wrap("The string does not represent a number"));
        }
        int radix = 10;
        if (nm.charAt(index) == '#') {
            radix = 16;
            ++index;
        } else if (nm.charAt(index) == '0') {
            ++index;
            if (index == nm.length()) {
                return TInteger.valueOf(0);
            }
            if (nm.charAt(index) == 'x' || nm.charAt(index) == 'X') {
                radix = 16;
                ++index;
            } else {
                radix = 8;
            }
        }
        if (index >= nm.length()) {
            throw new TNumberFormatException(TString.wrap("The string does not represent a number"));
        }
        int value = 0;
        while (index < nm.length()) {
            int digit = decodeDigit(nm.charAt(index++));
            if (digit >= radix) {
                throw new TNumberFormatException(TString.wrap("The string does not represent a number"));
            }
            value = value * radix + digit;
            if (value < 0) {
                if (negaive && value == MIN_VALUE && index == nm.length()) {
                    return TInteger.valueOf(MIN_VALUE);
                }
                throw new TNumberFormatException(TString.wrap("The string represents a too big number"));
            }
        }
        return TInteger.valueOf(negaive ? -value : value);
    }

    private static int decodeDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        } else if (c >= 'a' && c <= 'z') {
            return c - 'a' + 10;
        } else if (c >= 'A' && c <= 'Z') {
            return c - 'A' + 10;
        } else {
            return 255;
        }
    }

    @Override
    public int compareTo(TInteger other) {
        return compare(value, other.value);
    }

    public static int compare(int x, int y) {
        return x > y ? 1 : x < y ? -1 : 0;
    }
}