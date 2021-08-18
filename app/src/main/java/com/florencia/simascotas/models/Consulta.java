package com.florencia.simascotas.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.florencia.simascotas.services.SQLite;
import com.florencia.simascotas.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Consulta {
    public Integer idconsulta, codigosistema, mascotaid, actualizado, usuarioid;
    public String codigoconsulta, fechacelular, fechaconsulta, diagnostico, receta, prescripcion, nombreusuario;
    public Long longdatec;
    public Double lat, lon;

    public static final String TAG = "TAGCONSULTA";
    public static SQLiteDatabase sqLiteDatabase;

    public Consulta() {
        this.idconsulta = 0;
        this.codigosistema = 0;
        this.mascotaid = 0;
        this.fechacelular = "";
        this.fechaconsulta = "";
        this.diagnostico = "";
        this.receta = "";
        this.prescripcion = "";
        this.actualizado = 0;
        this.codigoconsulta = "";
        this.nombreusuario = "";
        this.longdatec = 0l;
        this.lat = 0d;
        this.lon = 0d;
        this.actualizado = 0;
    }

    public Consulta(String fecha, String diagnostico, String receta, String prescripcion) {
        fechaconsulta = fecha;
        this.diagnostico = diagnostico;
        this.receta = receta;
        this.prescripcion = prescripcion;
    }

    public boolean Save() {
        boolean retorno = false;
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            if (this.idconsulta == 0) {
                sqLiteDatabase.execSQL("INSERT INTO " +
                                "consulta(codigosistema, mascotaid, actualizado, usuarioid," +
                                "codigoconsulta, fechacelular, fechaconsulta, diagnostico, receta, prescripcion," +
                                "nombreusuario, longdatec, lat, lon) " +
                                "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        new String[]{this.codigosistema.toString(), this.mascotaid.toString(), this.actualizado.toString(),
                                this.usuarioid.toString(), this.codigoconsulta, this.fechacelular, this.fechaconsulta, this.diagnostico,
                                this.receta, this.prescripcion, this.nombreusuario, this.longdatec.toString(), this.lat.toString(), this.lon.toString()});
            } else {
                sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                                "consulta(idconsulta, codigosistema, mascotaid, actualizado, usuarioid," +
                                "codigoconsulta, fechacelular, fechaconsulta, diagnostico, receta, prescripcion," +
                                "nombreusuario, longdatec, lat, lon) " +
                                "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        new String[]{this.idconsulta.toString(), this.codigosistema.toString(), this.mascotaid.toString(), this.actualizado.toString(),
                                this.usuarioid.toString(), this.codigoconsulta, this.fechacelular, this.fechaconsulta, this.diagnostico,
                                this.receta, this.prescripcion, this.nombreusuario, this.longdatec.toString(), this.lat.toString(), this.lon.toString()});
            }
            if (this.idconsulta == 0) this.idconsulta = SQLite.sqlDB.getLastId();

            retorno = true;

            Log.d(TAG, "SAVE CONSULTA OK");
        } catch (SQLException ex) {
            Log.d(TAG, ex.getMessage());
            retorno = false;
        }
        return retorno;
    }

    public static boolean SaveList(Integer idmascota, List<Consulta> consultas) {
        boolean retorno = false;
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            for (Consulta cons : consultas) {
                if (cons.idconsulta == 0) {
                    sqLiteDatabase.execSQL("INSERT INTO " +
                                    "consulta(codigosistema, mascotaid, actualizado, usuarioid," +
                                    "codigoconsulta, fechacelular, fechaconsulta, diagnostico, receta, prescripcion," +
                                    "nombreusuario, longdatec, lat, lon) " +
                                    "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            new String[]{cons.codigosistema.toString(), idmascota.toString(), cons.actualizado.toString(),
                                    cons.usuarioid.toString(), cons.codigoconsulta, cons.fechacelular, cons.fechaconsulta, cons.diagnostico,
                                    cons.receta, cons.prescripcion, cons.nombreusuario, cons.longdatec.toString(), cons.lat.toString(), cons.lon.toString()});
                } else {
                    sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                                    "consulta(idconsulta, codigosistema, mascotaid, actualizado, usuarioid," +
                                    "codigoconsulta, fechacelular, fechaconsulta, diagnostico, receta, prescripcion," +
                                    "nombreusuario, longdatec, lat, lon) " +
                                    "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            new String[]{cons.idconsulta.toString(), cons.codigosistema.toString(), idmascota.toString(), cons.actualizado.toString(),
                                    cons.usuarioid.toString(), cons.codigoconsulta, cons.fechacelular, cons.fechaconsulta, cons.diagnostico,
                                    cons.receta, cons.prescripcion, cons.nombreusuario, cons.longdatec.toString(), cons.lat.toString(), cons.lon.toString()});
                }
                if (cons.idconsulta == 0) cons.idconsulta = SQLite.sqlDB.getLastId();
            }
            sqLiteDatabase.close();
            retorno = true;
            Log.d(TAG, "SAVE CONSULTA OK");
        } catch (SQLException ex) {
            Log.d(TAG, ex.getMessage());
            retorno = false;
        }
        return retorno;
    }

    public static Consulta getById(Integer Id) {
        Consulta Item = null;
        try {
            sqLiteDatabase = SQLite.sqlDB.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM consulta WHERE idconsulta = ?", new String[]{Id.toString()});
            if (cursor.moveToFirst()) Item = Consulta.AsignaDatos(cursor);
            sqLiteDatabase.close();
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return Item;
    }

    public static List<Consulta> getByMascota(Integer idMascota) {
        List<Consulta> retorno = new ArrayList<>();
        try {
            sqLiteDatabase = SQLite.sqlDB.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM consulta WHERE mascotaid  = ? ORDER BY longdatec DESC",
                    new String[]{idMascota.toString()});
            if (cursor.moveToFirst()) {
                do {
                    Consulta Item = Consulta.AsignaDatos(cursor);
                    if(Item!=null)
                        retorno.add(Item);
                }while (cursor.moveToNext());
            }
            sqLiteDatabase.close();
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return retorno;
    }

    public static ArrayList<Consulta> getConsultaSC(Integer idMascota) {
        ArrayList<Consulta> items = new ArrayList<>();
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM consulta where mascotaid = ? and (codigosistema = 0 or actualizado = 1)", new String[]{idMascota.toString()});
            Consulta mascota;
            if (cursor.moveToFirst()) {
                do {
                    mascota = Consulta.AsignaDatos(cursor);
                    if (mascota != null) items.add(mascota);
                }while (cursor.moveToNext());
            }
            cursor.close();
            sqLiteDatabase.close();
        } catch (Exception ec) {
            ec.printStackTrace();
        }
        return items;
    }

    public static boolean Update(Integer id, ContentValues data) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.update("consulta",data, "idconsulta = ?",new String[]{id.toString()});
            sqLiteDatabase.close();
            Log.d(TAG,"UPDATE consulta OK");
            return true;
        } catch (SQLException ex){
            Log.d(TAG, "Update(): " + String.valueOf(ex));
            return false;
        }
    }

    public static boolean removeConsultas(Integer idUsuario) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("DELETE FROM consulta WHERE codigosistema <> ? and usuarioid = ?", new String[] {"0", idUsuario.toString()});
            sqLiteDatabase.close();
            Log.d(TAG, "MASCOTAS ELIMINADOS");
            return true;
        } catch (Exception ec) {
            ec.printStackTrace();
            Log.d(TAG, ec.getMessage());
        }
        return false;
    }

    public static Consulta AsignaDatos(Cursor cursor) {
        Consulta Item = null;
        try {
            Item = new Consulta();
            Item.idconsulta = cursor.getInt(0);
            Item.codigosistema = cursor.getInt(1);
            Item.mascotaid = cursor.getInt(2);
            Item.actualizado = cursor.getInt(3);
            Item.usuarioid = cursor.getInt(4);
            Item.codigoconsulta = cursor.getString(5);
            Item.fechacelular = cursor.getString(6);
            Item.fechaconsulta = cursor.getString(7);
            Item.diagnostico = cursor.getString(8);
            Item.receta = cursor.getString(9);
            Item.prescripcion = cursor.getString(10);
            Item.nombreusuario = cursor.getString(11);
            Item.longdatec = cursor.getLong(12);
            Item.lat = cursor.getDouble(13);
            Item.lon = cursor.getDouble(14);
        } catch (SQLiteException ec) {
            Log.d(TAG, ec.getMessage());
        }
        return Item;
    }

    public static String GeneraCodigo(){
        return "CONS-".concat(Utils.getDateFormat("yyMMdd-HHmmss"));
    }
}
