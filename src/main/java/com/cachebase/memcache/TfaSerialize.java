/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cachebase.memcache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author thangblc
 */
public class TfaSerialize
{

    private static Logger _logger = Logger.getLogger(TfaSerialize.class);

    public static byte[] serialize(Object obj) throws IOException
    {
        byte[] rs = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try
        {

            oos.writeObject(obj);
            rs = bos.toByteArray();
        }
        catch (Exception ex)
        {
            _logger.error(ex.getMessage(), ex);
        }
        finally
        {
            oos.reset();
            oos.close();
            bos.reset();
            bos.close();
        }
        return rs;
    }

    public static Object deSerialize(byte[] bytes) throws IOException
    {
        Object rs = null;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        try
        {

            rs = objectInputStream.readObject();
        }
        catch (Exception ex)
        {
            _logger.error(ex.getMessage(), ex);
        }
        finally
        {
//            byteArrayInputStream.reset();
            byteArrayInputStream.close();
//            objectInputStream.reset();
            objectInputStream.close();
        }

        return rs;
    }

    public static byte[] intToByteArray(int key)
    {
        return BigInteger.valueOf((long) key).toByteArray();
    }

    public static int byteArrayToInt(byte[] bytes)
    {
        if(bytes == null)
        {
            return -1;
        }
        return new BigInteger(bytes).intValue();
    }
    
     public static byte[] stringToByteArray(String value)
    {
        if(value == null || value.equals(""))
        {
            value = "_";
        }
        return value.getBytes();
    }

    public static String byteArrayToString(byte[] bytes)
    {
        if(bytes == null)
        {
            return "";
        }
        String rs = new String(bytes);
        if(rs.equals("_"))
        {
            rs = "";
        }
        return rs;
    }
    
    public static byte[] genKeyList(int key, byte type)
    {
        byte[] keyBytes = intToByteArray(key);
        byte[] bytes = new byte[keyBytes.length + 1];
        byte[] typeBytes = new byte[1];
        typeBytes[0] = type;
        System.arraycopy(typeBytes, 0, bytes, 0, 1);
        System.arraycopy(keyBytes, 0, bytes, 1, keyBytes.length);
        return bytes;
    }

    public static List<Integer> byteArrayToList(byte[] bytes)
    {
        List<Integer> list = new ArrayList<Integer>();
        if(bytes == null)
        {
            return list;
        }
        try
        {
            int[] intArray = (int[]) deSerialize(bytes);
            for (int i = 0; i < intArray.length; i++)
            {
                list.add(intArray[i]);
            }
        }
        catch (Exception ex)
        {
            _logger.error(ex.getMessage(), ex);
        }

        return list;
    }

    public static byte[] listToByteArray(List<Integer> list) throws IOException
    {
        byte[] bytes;
        int size = 0;
        if (list == null || list.isEmpty())
        {
            int[] intAray = new int[size];
            bytes = serialize(intAray);
        }
        else
        {
            size = list.size();
            int[] intAray = new int[size];
            for (int i = 0; i < size; i++)
            {
                intAray[i] = list.get(i);
            }
            bytes = serialize(intAray);
        }

        return bytes;
    }
    public static void main(String[] args)
    {
        int key = 12;
        byte type = 100;
        
        byte[] typeBytes = null;
        //typeBytes[0] = type;
        int byteArrayToInt = byteArrayToInt(typeBytes);
        System.out.println(byteArrayToInt);
    }

}
