package net.iryndin.jdbf.core;

public enum DbfFieldTypeEnum {
    Character('C'),
    Currency('Y'),
    Numeric('N'),
    Float('F'),
    Date('D'),
    DateTime('T'),
    Double('B'),
    Integer('I'),
    Logical('L'),
    Memo('M'),
    General('G'),
    Picture('P'),
    Blob('W'),
    Varbinary('Q'),
    Varchar('V'),
    NullFlags('0');

    final char type;

    DbfFieldTypeEnum(char type) {
        this.type = type;
    }

    public static DbfFieldTypeEnum fromChar(char type) {
        for (DbfFieldTypeEnum e : DbfFieldTypeEnum.values()) {
            if (e.type == type) {
                return e;
            }
        }
        return null;
    }

    public byte toByte() {
        return (byte) type;
    }

    public char getType() {
        return type;
    }
}
