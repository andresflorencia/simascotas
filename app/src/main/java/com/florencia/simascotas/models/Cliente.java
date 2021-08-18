package com.florencia.simascotas.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.florencia.simascotas.services.SQLite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Cliente {
    public Integer idcliente;
    public String tiponip;
    public String nip;
    public String razonsocial;
    public String nombrecomercial;
    public String direccion;
    public Double lat, lon;
    public String categoria;
    public Integer usuarioid;
    public String fono1, fono2, email, observacion, ruc, fecharegistro, fechamodificacion, nombrecategoria;
    public Integer codigosistema, actualizado, establecimientoid, parroquiaid;
    public Long longdater, longdatem;
    public List<Propiedad> propiedades;
    public List<Foto> fotos;
    public List<Mascota> mascotas;

    public static SQLiteDatabase sqLiteDatabase;

    public Cliente(){
        this.idcliente= 0;
        this.tiponip= "";
        this.nip= "";
        this.razonsocial= "";
        this.nombrecomercial= "";
        this.direccion= "";
        this.lat = 0d;
        this.lon = 0d;
        this.categoria= "0";
        this.usuarioid= 0;
        this.fono1 = "";
        this.fono2 ="";
        this.email="";
        this.observacion="";
        this.ruc= "";
        this.codigosistema = 0;
        this.actualizado =0;
        this.establecimientoid = 0;
        this.parroquiaid = 0;
        this.fecharegistro = "";
        this.fechamodificacion = "";
        this.longdater = 0l;
        this.longdatem = 0l;
        this.nombrecategoria = "";
        this.propiedades = new ArrayList<>();
        this.fotos = new ArrayList<>();
        this.mascotas = new ArrayList<>();
    }

    public static boolean removeClientes(Integer idUsuario) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("DELETE FROM cliente WHERE usuarioid = ? AND codigosistema <> 0", new String[] {idUsuario.toString()});
            sqLiteDatabase.close();
            Log.d("TAGCLIENTE", "CLIENTES ELIMINADOS");
            return true;
        } catch (Exception ec) {
            ec.printStackTrace();
            Log.d("TAGCLIENTE", ec.getMessage());
        }
        return false;
    }

    public boolean Save() {
        try {
            this.sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            if (this.idcliente == 0)
                this.sqLiteDatabase.execSQL("INSERT INTO " +
                        "cliente(tiponip, nip, razonsocial, nombrecomercial, direccion, lat, lon, categoria, " +
                                "usuarioid, fono1, fono2, email, observacion, ruc, codigosistema, actualizado, " +
                                "establecimientoid, parroquiaid, fecharegistro, fechamodificacion, longdater, longdatem," +
                                "nombrecategoria) " +
                        "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        new String[]{ this.tiponip, this.nip, this.razonsocial, this.nombrecomercial, this.direccion,
                                this.lat.toString(), this.lon.toString(), this.categoria.equals("")?"0":this.categoria, this.usuarioid.toString(), this.fono1,
                                this.fono2, this.email, this.observacion, this.ruc, this.codigosistema.toString(), this.actualizado.toString(),
                                this.establecimientoid.toString(), this.parroquiaid.toString(), this.fecharegistro, this.fechamodificacion,
                                this.longdater.toString(), this.longdatem.toString(), this.nombrecategoria});
            else
                this.sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                                "cliente(idcliente, tiponip, nip, razonsocial, nombrecomercial, direccion, lat, lon, categoria, " +
                                "usuarioid, fono1, fono2, email, observacion, ruc, codigosistema, actualizado, establecimientoid, " +
                                "parroquiaid, fecharegistro, fechamodificacion, longdater, longdatem, nombrecategoria) " +
                                "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        new String[]{ this.idcliente.toString(), this.tiponip, this.nip, this.razonsocial, this.nombrecomercial, this.direccion,
                                this.lat.toString(), this.lon.toString(), this.categoria.equals("")?"0":this.categoria, this.usuarioid.toString(), this.fono1,
                                this.fono2, this.email, this.observacion, this.ruc, this.codigosistema.toString(), this.actualizado.toString(),
                                this.establecimientoid.toString(), this.parroquiaid.toString(), this.fecharegistro, this.fechamodificacion,
                                this.longdater.toString(), this.longdatem.toString(), this.nombrecategoria});
            if (this.idcliente == 0) this.idcliente = SQLite.sqlDB.getLastId();

            Foto.SaveList(this.idcliente, 0, this.fotos, "G");
            //Mascota.SaveList(this.mascotas);

            this.sqLiteDatabase.close();
            Log.d("TAGCLIENTE","SAVE CLIENTE OK");
            return true;
        } catch (SQLException ex){
            Log.d("TAGCLIENTE", this.nip + " - " + ex.getMessage());
            return false;
        }
    }

    public static Cliente get(Integer Id, boolean getdetalle) {
        Cliente Item = null;
        try {
            sqLiteDatabase = SQLite.sqlDB.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM cliente WHERE idcliente  = ?", new String[]{Id.toString()});
            if (cursor.moveToFirst()) Item = Cliente.AsignaDatos(cursor, getdetalle, false);
            sqLiteDatabase.close();
        }catch (Exception e){
            Log.d("TAGCLIENTE",e.getMessage());
        }
        return Item;
    }

    public static Cliente get(String nip, boolean getdetalle) {
        Cliente Item = null;
        try {
            sqLiteDatabase = SQLite.sqlDB.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM cliente WHERE nip  = ?", new String[]{nip});
            if (cursor.moveToFirst()) Item = Cliente.AsignaDatos(cursor, getdetalle, false);
            sqLiteDatabase.close();
        }catch (SQLiteException e){
            Log.d("TAGCLIENTE", e.getMessage());
        }
        return Item;
    }

    public static Cliente AsignaDatos(Cursor cursor, boolean getdetalle, boolean sincronizacion) {
        Cliente Item = null;
        try {
            Item = new Cliente();
            Item.idcliente = cursor.getInt(0);
            Item.tiponip = cursor.getString(1);
            Item.nip = cursor.getString(2);
            Item.razonsocial = cursor.getString(3);
            Item.nombrecomercial = cursor.getString(4);
            Item.direccion = cursor.getString(5);
            Item.lat = cursor.getDouble(6);
            Item.lon = cursor.getDouble(7);
            Item.categoria = cursor.getString(8);
            Item.usuarioid = cursor.getInt(9);
            Item.fono1 = cursor.getString(10);
            Item.fono2 = cursor.getString(11);
            Item.email = cursor.getString(12);
            Item.observacion = cursor.getString(13);
            Item.ruc = cursor.getString(14);
            Item.codigosistema = cursor.getInt(15);
            Item.actualizado = cursor.getInt(16);
            Item.establecimientoid = cursor.getInt(17);
            Item.parroquiaid = cursor.getInt(18);
            Item.fecharegistro = cursor.getString(19);
            Item.fechamodificacion = cursor.getString(20);
            Item.longdater = cursor.getLong(21);
            Item.longdatem = cursor.getLong(22);
            Item.nombrecategoria = cursor.getString(23);
            if(getdetalle) {
                Item.propiedades = Propiedad.getByPropietario(Item.idcliente);
                Item.fotos = Foto.getLista(Item.idcliente, 0, "G");
                Item.mascotas = Mascota.getByPropietario(Item.idcliente, false);
            }else if(sincronizacion){
                Item.propiedades = Propiedad.getPropiedadesSC(Item.idcliente);
                Item.fotos = Foto.getLista(Item.idcliente, 0, "G");
                Item.mascotas = Mascota.getMascotaSC(Item.idcliente);
            }
        } catch (SQLiteException ec) {
            Log.d("TAGCLIENTE", ec.getMessage());
        }
        return Item;
    }

    public String Validate() {
        String str = "";
        if (this.nip.equals("")) str += "   -   NIP" + '\n';
        if (this.razonsocial.equals("")) str += "  -  Razón Social" + '\n';
        if (this.nombrecomercial.equals("")) str += "   -   Nombre de la tienda o comercio" + '\n';
        if (this.fono1.equals("") && this.fono2.equals("")) str += "   -   Celular o convencional" + '\n';
        if (this.direccion.equals("")) str += "   -   Dirección" + '\n';
        //if (this.lat == 0 || this.lon == 0) str += "   -   Coordenadas. Por favor verificar que el GPS esté activo";
        return str;
    }

    public static List<Cliente> getClientes(Integer idUser, String fecha, boolean getdetalle) {
        List<Cliente> lista = new ArrayList<>();
        try {
            List<String> params = new ArrayList<>();
            params.add(idUser.toString());
            String WHERE = "usuarioid = ?";
            if(!fecha.trim().equals(""))
                WHERE += " AND (fecharegistro like '"+fecha+"%' OR fechamodificacion like '"+fecha+"%')";
            String[]paramsA = new String[params.size()];
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM cliente WHERE "+WHERE+" ORDER BY actualizado desc, razonsocial asc", params.toArray(paramsA));
            Cliente cliente;
            if (cursor.moveToFirst()) {
                do {
                    cliente = Cliente.AsignaDatos(cursor, getdetalle, false);
                    if (cliente != null) lista.add(cliente);
                }while (cursor.moveToNext());
            }
            cursor.close();
            sqLiteDatabase.close();
        } catch (Exception ec) {
            Log.d("TAGCLIENTE",ec.getMessage());
            ec.printStackTrace();
        }
        return lista;
    }

    public static ArrayList<Cliente> getClientesSC(Integer idUser) {
        ArrayList<Cliente> items = new ArrayList<Cliente>();
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery(
                    "SELECT DISTINCT * FROM cliente where usuarioid = ? and nip <> ? and (codigosistema = 0 or actualizado = 1)" +
                        "UNION " +
                        "SELECT * FROM cliente WHERE idcliente IN (SELECT DISTINCT duenoid FROM mascota WHERE usuarioid = ? and (codigosistema = 0 or actualizado = 1))",
                    new String[]{idUser.toString(), "9999999999999", idUser.toString()});
            Cliente cliente;
            if (cursor.moveToFirst()) {
                do {
                    cliente = Cliente.AsignaDatos(cursor, false, true);
                    if (cliente != null) items.add(cliente);
                }while (cursor.moveToNext());
            }
            cursor.close();
            sqLiteDatabase.close();
        } catch (Exception ec) {
            ec.printStackTrace();
            Log.d("TAGCLIENT", ec.getMessage());
        }
        return items;
    }

    public static boolean Update(Integer id, ContentValues data) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.update("cliente",data, "idcliente = ?",new String[]{id.toString()});
            sqLiteDatabase.close();
            Log.d("TAGCLIENTE","UPDATE CLIENTE OK");
            return true;
        } catch (SQLException ex){
            Log.d("TAGCLIENTE", "Update(): " + ex.getMessage());
            return false;
        }
    }
}
