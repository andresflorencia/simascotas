package com.florencia.simascotas.models;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.florencia.simascotas.services.SQLite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Foto {
    public Integer idfoto, ganaderoid, propiedadid;
    public String name, path, image_base, tipo;
    public Uri uriFoto;
    public Bitmap bitmap;
    public File file;

    public static SQLiteDatabase sqLiteDatabase;
    public static final String TAG = "TAG_FOTO";

    public Foto(){
        this.idfoto = 0;
        this.ganaderoid = 0;
        this.propiedadid = 0;
        this.name = "";
        this.path = "";
        this.tipo = "";
        this.image_base="";
    }

    public static boolean removeFotos(Integer idGanadero) {
        try {
            String[] params = new String[]{idGanadero.toString()};
            String where = "WHERE ganaderoid = ?";
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("DELETE FROM foto " + where, params);
            sqLiteDatabase.close();
            Log.d(TAG, "fotos ELIMINADAS ganaderoid: " + idGanadero );
            return true;
        } catch (Exception ec) {
            ec.printStackTrace();
            Log.d(TAG, "removeFotos(): " + ec.getMessage());
        }
        return false;
    }

    public static boolean removeFotos(Integer idGanadero, Integer idPropiedad, String tipo) {
        try {
            String [] params = new String[]{idGanadero.toString(),idPropiedad.toString(), tipo};
            String where = "WHERE ganaderoid = ? and propiedadid = ? and tipo = ?";
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("DELETE FROM foto " + where, params);
            sqLiteDatabase.close();
            Log.d(TAG, "fotos ELIMINADAS ganaderoid: " + idGanadero + " - propiedadid: " + idPropiedad + " - tipo: " + tipo);
            return true;
        } catch (Exception ec) {
            ec.printStackTrace();
            Log.d(TAG, "removeFotos(): " + ec.getMessage());
        }
        return false;
    }

    public static boolean SaveList(Integer idganadero, Integer idpropiedad, List<Foto> fotos, String tipo) {
        try {
            removeFotos(idganadero, idpropiedad, tipo);
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            for (Foto foto:fotos) {
                foto.ganaderoid = idganadero;
                foto.propiedadid = idpropiedad;
                sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                                "foto(idfoto, ganaderoid, propiedadid, name, path, tipo) "+
                                "values(?, ?, ?, ?, ?, ?)",
                        new String[]{foto.idfoto==0?null: foto.idfoto.toString(), foto.ganaderoid.toString(), foto.propiedadid.toString(),
                                foto.name, foto.path, foto.tipo});
                if (foto.idfoto == 0)foto.idfoto = SQLite.sqlDB.getLastId();
            }
            sqLiteDatabase.close();
            Log.d(TAG,"SAVE LISTA DE FOTOS OK");
            return true;
        } catch (SQLException ex){
            Log.d(TAG,"SaveList(): " +ex.getMessage());
            return false;
        }
    }

    public static List<Foto> getLista(Integer idganadero, Integer idpropiedad, String tipo){
        List<Foto> retorno = new ArrayList<>();
        try{
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM foto WHERE ganaderoid = ? AND propiedadid = ? and tipo = ?",
                    new String[]{idganadero.toString(), idpropiedad.toString(), tipo});
            Foto foto;
            if (cursor.moveToFirst()) {
                do {
                    foto = Foto.AsignaDatos(cursor);
                    if (foto != null) retorno.add(foto);
                }while (cursor.moveToNext());
            }
            cursor.close();
            sqLiteDatabase.close();
        }catch (SQLiteException e){
            Log.d(TAG, "getLista(): " + e.getMessage());
        }
        return retorno;
    }

    public static Foto AsignaDatos(Cursor cursor) {
        Foto Item = null;
        try {
            Item = new Foto();
            Item.idfoto = cursor.getInt(0);
            Item.ganaderoid = cursor.getInt(1);
            Item.propiedadid = cursor.getInt(2);
            Item.name = cursor.getString(3);
            Item.path = cursor.getString(4);
            Item.tipo = cursor.getString(5);
        } catch (SQLiteException ec) {
            Log.d(TAG, "AsignaDatos(): " + ec.getMessage());
        }
        return Item;
    }
}
