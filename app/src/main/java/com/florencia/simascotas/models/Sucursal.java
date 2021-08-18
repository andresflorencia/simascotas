package com.florencia.simascotas.models;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import com.florencia.simascotas.services.SQLite;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.json.JSONException;
import org.json.JSONObject;

public class Sucursal {
    public String IdSucursal;
    public String RUC;
    public String RazonSocial;
    public String NombreComercial;
    public String NombreSucursal;
    public String Direcion;
    public String CodigoEstablecimiento;
    public String PuntoEmision;
    public String Ambiente;
    public String SucursalPadreID;
    public Integer IdEstablecimiento, IdPuntoEmision, periodo, mesactual;
    public static SQLiteDatabase sqLiteDatabase;

    public Sucursal() {
        this.IdSucursal = "";
        this.RUC = "";
        this.RazonSocial = "";
        this.NombreComercial = "";
        this.NombreSucursal = "";
        this.Direcion = "";
        this.CodigoEstablecimiento = "";
        this.PuntoEmision = "";
        this.Ambiente = "";
        this.SucursalPadreID = "";
        this.IdEstablecimiento=0;
        this.IdPuntoEmision =0;
        this.periodo = 0;
        this.mesactual = 0;
    }

    public boolean Guardar(){
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                    "sucursal(idsucursal, ruc, razonsocial, nombrecomercial, nombresucursal, direccion, codigoestablecimiento, puntoemision, ambiente, idestablecimiento, idpuntoemision, periodo, mesactual) " +
                    "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new String[]{this.IdSucursal, this.RUC, this.RazonSocial, this.NombreComercial, this.NombreSucursal, this.Direcion, this.CodigoEstablecimiento,
                    this.PuntoEmision, this.Ambiente, this.IdEstablecimiento.toString() , this.IdPuntoEmision.toString(),
                    this.periodo.toString(), this.mesactual.toString()});
            sqLiteDatabase.close();
            return true;
        } catch (SQLException ex) {
            Log.d("TAGSUCURSAL", "Guardar(): " + ex.getMessage());
            return false;
        }
    }

    public static boolean Delete (Integer id){
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.delete("sucursal","idestablecimiento = ?",new String[]{id.toString()});
            sqLiteDatabase.close();
            Log.d("TAGSUCURSAL","DELETE SUCURSAL OK");
            return true;
        } catch (SQLException ex){
            Log.d("TAGSUCURSAL", "Delete(): " + ex.getMessage());
            return false;
        }
    }

    static public Sucursal getSucursal(String cod){
        Sucursal Item = null;
        sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM sucursal WHERE idestablecimiento = ?", new String[] { cod });
        if (cursor.moveToFirst()) {
            Item = AsignaDatos(cursor);
        }
        cursor.close();
        sqLiteDatabase.close();
        return Item;
    }

    private static Sucursal AsignaDatos(Cursor cursor)
    {
        Sucursal Item = new Sucursal();
        Item.IdSucursal = cursor.getString(0);
        Item.RUC = cursor.getString(1);
        Item.RazonSocial = cursor.getString(2);
        Item.NombreComercial = cursor.getString(3);
        Item.NombreSucursal = cursor.getString(4);
        Item.Direcion = cursor.getString(5);
        Item.CodigoEstablecimiento = cursor.getString(6);
        Item.PuntoEmision = cursor.getString(7);
        Item.Ambiente = cursor.getString(8);
        Item.IdEstablecimiento = cursor.getInt(10);
        Item.IdPuntoEmision = cursor.getInt(11);
        Item.periodo = cursor.getInt(12);
        Item.mesactual = cursor.getInt(13);
        return Item;
    }

    public static Sucursal AsignaDatos(JsonObject object) throws JsonParseException {
        Sucursal Item = null;
        try {
            if (object != null) {
                Item = new Sucursal();
                Item.IdSucursal = object.get("idsucursal").getAsString();
                Item.RUC = object.get("ruc").getAsString();
                Item.RazonSocial = object.get("razonsocial").getAsString();
                Item.NombreComercial = object.get("nombrecomercial").getAsString();
                Item.Direcion = object.get("direccion").getAsString();
                Item.CodigoEstablecimiento = object.get("codigoestablecimiento").getAsString();
                Item.PuntoEmision = object.get("puntoemision").getAsString();
                Item.Ambiente = object.get("ambiente").getAsString();
                Item.IdEstablecimiento = object.get("idestablecimiento").getAsInt();
                Item.IdPuntoEmision = object.get("idpuntoemision").getAsInt();
                Item.NombreSucursal = object.get("nombreestablecimiento").getAsString();
                Item.periodo =  object.has("periodo")? object.get("periodo").getAsInt():0;
                Item.mesactual =  object.has("mesactual")? object.get("mesactual").getAsInt():0;
                Sucursal.Delete(Item.IdEstablecimiento);
                if(Item.Guardar())
                    Item.actualizasecuencial(object.get("s01").getAsInt(), "01");
            }
        }catch (JsonParseException e){
            Log.d("TAGSUCURSAL", "AsiganDatos(): " + e.getMessage());
        }
        return Item;
    }

    private boolean actualizasecuencial(int Secuencial, String TipoComprobante) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("INSERT OR REPLACE INTO secuencial(secuencial, sucursalid, codigoestablecimiento, puntoemision, tipocomprobante) VALUES(?, ?, ?, ?, ?) ", new String[]{String.valueOf(Secuencial), this.IdSucursal.toString(), this.CodigoEstablecimiento, this.PuntoEmision, TipoComprobante});
            sqLiteDatabase.close();
            Log.d("TAGSUCURSAL", "SECUENCIAL ACTUALIZADO");
            return true;
        } catch (Exception ec) {
            Log.d("TAGSUCURSAL", "actualizasecuencial(): " + ec.getMessage());
            ec.printStackTrace();
            return false;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return this.NombreSucursal;
    }
}
