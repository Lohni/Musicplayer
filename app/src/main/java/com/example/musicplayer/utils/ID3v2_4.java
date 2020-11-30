package com.example.musicplayer.utils;

import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ID3v2_4 {

    //Size in bytes
    private static final int HEADER_SIZE = 10;

    private int TAG_SIZE;
    private int TAG_VERSION_MAJOR;
    private int TAG_VERSION_REVISION;
    private boolean UNSYNCHRONISATION, EXTENDED_HEADER, EXPERIMENTAL_INDICATOR, FOOTER;


    private Context context;

    public ID3v2_4(Uri uri, Context context){
        this.context = context;
        decode(uri);
    }

    private boolean decode(Uri uri){
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        InputStream is;
        try {
            is = context.getContentResolver().openInputStream(uri);
            int nRead;

            byte[] header = new byte[10];

            is.read(header, 0, header.length);
            decodeHeader(header);

            byte[] data = new byte[4096];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
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

    private void decodeHeader(byte[] header){
        if (header[0] == 0x49 && header[1] == 0x44 && header[2] == 0x33){
            TAG_VERSION_MAJOR = (int) header[3];
            TAG_VERSION_REVISION = (int) header[4];
        }

    }

}
