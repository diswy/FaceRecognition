/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.orbbec.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import android.graphics.Bitmap;

public final class Converter {

    private static final int BYTES_IN_INT = 4;

    private Converter() {}

    public static byte [] convert(int [] array) {
        if (isEmpty(array)) {
            return new byte[0];
        }

        return writeInts(array);
    }

    public static int [] convert(byte [] array) {
        if (isEmpty(array)) {
            return new int[0];
        }

        return readInts(array);
    }

    private static byte [] writeInts(int [] array) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(array.length * 4);
            DataOutputStream dos = new DataOutputStream(bos);
            for (int i = 0; i < array.length; i++) {
                dos.writeInt(array[i]);
            }

            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int [] readInts(byte [] array) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(array);
            DataInputStream dataInputStream = new DataInputStream(bis);
            int size = array.length / BYTES_IN_INT;
            int[] res = new int[size];
            for (int i = 0; i < size; i++) {
                res[i] = dataInputStream.readInt();
            }
            return res;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isEmpty(byte [] array) {
        if (array == null || array.length == 0) {
            return true;
        }
        return false;
     }

    private static boolean isEmpty(int [] array) {
        if (array == null || array.length == 0) {
            return true;
        }
        return false;
    }


    int IntToByte(byte arrayDst[], int arrayOrg[], int maxOrg){
        int i;
        int idxDst;
        int maxDst;
        //
        maxDst = maxOrg*4;
        //
        if (arrayDst==null)
            return 0;
        if (arrayOrg==null)
            return 0;
        if (arrayDst.length < maxDst)
            return 0;
        if (arrayOrg.length < maxOrg)
            return 0;
        //
        idxDst = 0;
        for (i=0; i<maxOrg; i++){
            // Copia o int, byte a byte.
            arrayDst[idxDst] = (byte)(arrayOrg[i]);
            idxDst++;
            arrayDst[idxDst] = (byte)(arrayOrg[i] >> 8);
            idxDst++;
            arrayDst[idxDst] = (byte)(arrayOrg[i] >> 16);
            idxDst++;
            arrayDst[idxDst] = (byte)(arrayOrg[i] >> 24);
            idxDst++;
        }
        //
        return idxDst;
    }

    public static int byteToInt(int arrayDst[], byte arrayOrg[], int maxOrg){
        int i;
        int v;
        int idxOrg;
        int maxDst;
        //
        // maxDst = maxOrg/3;
        maxDst = maxOrg;
        //
        if (arrayDst==null)
            return 0;
        if (arrayOrg==null)
            return 0;
        if (arrayDst.length < maxDst)
            return 0;
        if (arrayOrg.length < maxOrg)
            return 0;

        byte[] rgba = new byte[maxOrg * 4];
        for (int j = 0; j < maxOrg; j++) {
            byte b1 = arrayOrg[j * 3 + 0];
            byte b2 = arrayOrg[j * 3 + 1];
            byte b3 = arrayOrg[j * 3 + 2];
            // set value
            rgba[j * 4 + 0] = b1;
            rgba[j * 4 + 1] = b2;
            rgba[j * 4 + 2] = b3;
            rgba[j * 4 + 3] = (byte) 255;
        }

        //
        idxOrg = 0;
        for (i=0; i<maxDst; i++){
            arrayDst[i] = 0;
            //
            v = 0x000000FF & rgba[idxOrg];
            arrayDst[i] = arrayDst[i] | v;
            idxOrg++;
            //
            v = 0x000000FF & rgba[idxOrg];
            arrayDst[i] = arrayDst[i] | (v << 8);
            idxOrg++;
            //
            v = 0x000000FF & rgba[idxOrg];
            arrayDst[i] = arrayDst[i] | (v << 16);
            idxOrg++;
            //
            v = 0x000000FF & rgba[idxOrg];
            arrayDst[i] = arrayDst[i] | (v << 24);
            idxOrg++;
        }
        //
        return maxDst;
    }

    public static Bitmap RGB2Bitmap(byte[] bytes, int width, int height) {
        // use Bitmap.Config.ARGB_8888 instead of type is OK
        Bitmap stitchBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        byte[] rgba = new byte[width * height * 4];
        for (int i = 0; i < width * height; i++) {
            byte b1 = bytes[i * 3 + 0];
            byte b2 = bytes[i * 3 + 1];
            byte b3 = bytes[i * 3 + 2];
            // set value
            rgba[i * 4 + 0] = b1;
            rgba[i * 4 + 1] = b2;
            rgba[i * 4 + 2] = b3;
            rgba[i * 4 + 3] = (byte) 255;
        }
        stitchBmp.copyPixelsFromBuffer(ByteBuffer.wrap(rgba));
        return stitchBmp;
    }
}