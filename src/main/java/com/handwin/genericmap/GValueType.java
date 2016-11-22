package com.handwin.genericmap;


public enum GValueType {
    BOOL(1),
    BYTE(2),
    SHORT(3),
    INT(4),
    LONG(5),
    FLOAT(6),
    DOUBLE(7),
    BYTES(8),
    STRING(9),
    OBJECT(0x0A),
    LBOOL(0x11),
    LBYTE(0x12),
    LSHORT(0x13),
    LINT(0x14),
    LLONG(0x15),
    LFLOAT(0x16),
    LDOUBLE(0x17),
    LBYTES(0x18),
    LSTRING(0x19),
    LOBJECT(0x1A),
    UNKNOWN(0x1F);

    private int id;

    public int id() {
        return this.id;
    }

    GValueType(int id) {
        this.id = id;
    }

    public static GValueType getInstance(int id) {
        switch (id) {
            case 1:
                return BOOL;
            case 2:
                return BYTE;
            case 3:
                return SHORT;
            case 4:
                return INT;
            case 5:
                return LONG;
            case 6:
                return FLOAT;
            case 7:
                return DOUBLE;
            case 8:
                return BYTES;
            case 9:
                return STRING;
            case 0x0A:
                return OBJECT;
            case 0x11:
                return LBOOL;
            case 0x12:
                return LBYTE;
            case 0x13:
                return LSHORT;
            case 0x14:
                return LINT;
            case 0x15:
                return LLONG;
            case 0x16:
                return LFLOAT;
            case 0x17:
                return LDOUBLE;
            case 0x18:
                return LBYTES;
            case 0x19:
                return LSTRING;
            case 0x1A:
                return LOBJECT;
            default:
                return UNKNOWN;
        }
    }

}
