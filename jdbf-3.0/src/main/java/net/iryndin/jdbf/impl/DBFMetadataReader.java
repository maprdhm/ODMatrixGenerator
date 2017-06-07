package net.iryndin.jdbf.impl;

import net.iryndin.jdbf.api.IDBFField;
import net.iryndin.jdbf.api.IDBFHeader;
import net.iryndin.jdbf.api.IDBFMetadata;
import net.iryndin.jdbf.core.DbfFieldTypeEnum;
import net.iryndin.jdbf.core.DbfFileTypeEnum;
import net.iryndin.jdbf.util.BitUtils;
import net.iryndin.jdbf.util.CharsetHelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Reads header and metadata info
 */
public class DBFMetadataReader {

    public static final int FIELD_RECORD_LENGTH = 32;
    public static final int HEADER_TERMINATOR = 0x0D;

    private final InputStream inputStream;
    private int readBytesCount = 0;

    public DBFMetadataReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public IDBFMetadata readDbfMetadata() throws IOException {
        IDBFHeader header = readHeader();
        List<IDBFField> fields = readFields(header);
        return new DBFMetadataImpl(header, fields);
    }

    public IDBFHeader readHeader() throws IOException {
        byte[] headerBytes = new byte[32];
        int bytesRead = inputStream.read(headerBytes);
        if (bytesRead != 32) {
            throw new IOException("When reading DBF header should read exactly 32 bytes! Bytes read instead: " + bytesRead);
        }
        readBytesCount += bytesRead;

        DbfFileTypeEnum type = DbfFileTypeEnum.fromInt(headerBytes[0]);
        Date updateDate = parseHeaderUpdateDate(headerBytes[1], headerBytes[2], headerBytes[3], type);
        int recordsQty = BitUtils.makeInt(headerBytes[4], headerBytes[5], headerBytes[6], headerBytes[7]);
        // fullHeaderLength or position of first data record
        int fullHeaderLength = BitUtils.makeInt(headerBytes[8], headerBytes[9]);
        int oneRecordLength = BitUtils.makeInt(headerBytes[10], headerBytes[11]);
        byte uncompletedTxFlag = headerBytes[14];
        byte ecnryptionFlag = headerBytes[15];
        Charset charset = CharsetHelper.getCharsetByByte(headerBytes[29]);

        return new DBFHeaderImpl(type, updateDate, recordsQty, fullHeaderLength, oneRecordLength, uncompletedTxFlag, ecnryptionFlag, charset);
    }

    public List<IDBFField> readFields(IDBFHeader header) throws IOException {
        List<IDBFField> fields = new ArrayList<>();
        byte[] fieldBytes = new byte[FIELD_RECORD_LENGTH];
        int headerLength = 0;
        int fieldLength = 0;
        // offset == 1
        // because first (zero-shifted) byte is record flag
        int offset = 1;
        int fieldBytesOffset = 0;
        while (true) {
            readBytesCount += inputStream.read(fieldBytes, fieldBytesOffset, FIELD_RECORD_LENGTH-fieldBytesOffset);
            IDBFField field = readDbfField(fieldBytes, offset);
            fields.add(field);
            offset += field.getLength();

            fieldLength += field.getLength();
            headerLength += fieldBytes.length;

            int terminator = inputStream.read();
            readBytesCount+=1;
            if (terminator == HEADER_TERMINATOR) {
                break;
            } else {
                fieldBytes[0] = (byte)terminator;
                fieldBytesOffset=1;
            }
        }
        fieldLength += 1;
        headerLength += 32;
        headerLength += 1;

        //if (headerLength != header.getFullHeaderLength()) {
        //    throw new IllegalStateException("headerLength != header.getFullHeaderLength()");
        //}
        if (fieldLength != header.getOneRecordLength()) {
            throw new IllegalStateException("fieldLength != header.getOneRecordLength()");
        }

        return fields;
    }

    /**
     *
     * Read field value according to this info:
     * Title: Table File Structure (.dbc, .dbf, .frx, .lbx, .mnx, .pjx, .scx, .vcx)
     * http://msdn.microsoft.com/en-US/library/st4a0s68(v=vs.80).aspx
     *
     * @param fieldBytes
     * @return
     */
    private static IDBFField readDbfField(byte[] fieldBytes, int fieldOffset) {

        String name;
        int length;
        int numberOfDecimalPlaces;

        // 1. Set name
        {
            int i;
            for (i = 0; i < 11 && fieldBytes[i] > 0; i++) ;
            name = new String(fieldBytes, 0, i);
        }
        // 2. Set type
        DbfFieldTypeEnum type = DbfFieldTypeEnum.fromChar((char) fieldBytes[11]);
        // 3. Set length
        {
            length = fieldBytes[16];
            if (length < 0) {
                length = 256 + length;
            }
        }
        // 4. Set number of decimal places
        numberOfDecimalPlaces = fieldBytes[17];

        return new DBFFieldImpl(name, type, length, numberOfDecimalPlaces, fieldOffset);
    }

    private static Date parseHeaderUpdateDate(byte yearByte, byte monthByte, byte dayByte, DbfFileTypeEnum fileType) {
        int year = yearByte + 2000 - 1900;
        switch (fileType) {
            case FoxBASEPlus1:
                year = yearByte;
        }
        int month = monthByte - 1;
        int day = dayByte;
        return new Date(year,month,day);

    }

    public int getReadBytesCount() {
        return readBytesCount;
    }
}
