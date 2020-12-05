package com.example.musicplayer.utils;

import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ID3Editor {

    //Size in bytes
    private static final int HEADER_SIZE = 10;

    private int TAG_SIZE;
    private int TAG_VERSION_MAJOR;
    private int TAG_VERSION_REVISION;
    private int UNSYNCHRONISATION, EXTENDED_HEADER, EXPERIMENTAL_INDICATOR, FOOTER;


    private Context context;

    public ID3Editor(Uri uri, Context context){
        this.context = context;
        decode(uri);
    }

    private boolean decode(Uri uri){
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        InputStream is;
        try {
            is = context.getContentResolver().openInputStream(uri);
            int nRead;

            byte[] data = new byte[4096];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            if (decodeHeader(Arrays.copyOfRange(data,0,HEADER_SIZE))){
                switch (TAG_VERSION_MAJOR){
                    case 2:{

                    }
                    case 3:{

                    }
                    case 4:{

                    }
                    default:{

                    }
                }
            }

            is.close();
        } catch (FileNotFoundException e){
            System.out.println(e);
        } catch (IOException e){
            System.out.println(e);
        } finally {
            buffer.toByteArray();
        }

        return true;
    }

    private void checkFooter(InputStream is){

    }

    private void getFlags(byte flag){
        UNSYNCHRONISATION = flag >> 7;
        EXTENDED_HEADER = flag >> 6;
        EXPERIMENTAL_INDICATOR = flag >> 5;
        FOOTER = flag >> 4;
    }

    private void getSize(byte size4, byte size3, byte size2, byte size1){
        size4 = (byte) (size4 >> 1);
        size3 = (byte) (size3 >> 1);
        size2 = (byte) (size2 >> 1);
        size1 = (byte) (size1 >> 1);

        TAG_SIZE = (int) ((size4 & 0xFF) << 8)

    }

    private boolean decodeHeader(byte[] header){
        if (header[0] == 0x49 && header[1] == 0x44 && header[2] == 0x33){
            TAG_VERSION_MAJOR = (int) header[3];
            TAG_VERSION_REVISION = (int) header[4];
            getFlags(header[5]);
            return true;
        } else {
            return false;
        }
    }
}
