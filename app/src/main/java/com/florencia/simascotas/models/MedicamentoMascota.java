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

public class MedicamentoMascota {
    public Integer idmedicamentomascota, codigosistema, mascotaid, actualizado, usuarioid;
    public Medicamento medicamento;
    public String codigo, fechacelular, fechaaplicacion, proximaaplicacion, observacion, nombreusuario, tipo;
    public Long longdate;
    public Double lat, lon;
    public static final String TAG = "TAGVACUNA";
    public static SQLiteDatabase sqLiteDatabase;

    public MedicamentoMascota() {
        this.idmedicamentomascota = this.codigosistema = this.mascotaid = this.actualizado = this.usuarioid = 0;
        this.medicamento = new Medicamento();
        this.codigo = this.fechacelular = this.fechaaplicacion = this.proximaaplicacion = this.observacion = this.nombreusuario = this.tipo = "";
        this.longdate = 0l;
        this.lat = this.lon = 0d;
    }

    public MedicamentoMascota(Medicamento medicamento, String fechaaplicacion, String proximaaplicacion, String observacion) {
        this.medicamento = medicamento;
        this.fechaaplicacion = fechaaplicacion;
        this.proximaaplicacion = proximaaplicacion;
        this.observacion = observacion;
    }

    public boolean Save() {
        boolean retorno = false;
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            if (this.idmedicamentomascota == 0) {
                sqLiteDatabase.execSQL("INSERT INTO " +
                                "medicamentomascota(codigosistema, mascotaid, actualizado, usuarioid," +
                                "medicamentoid, tipo, codigo, fechacelular, fechaaplicacion, proximaaplicacion, " +
                                "observacion, nombreusuario, longdate, lat, lon) " +
                                "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        new String[]{this.codigosistema.toString(), this.mascotaid.toString(), this.actualizado.toString(),
                                this.usuarioid.toString(), this.medicamento.idmedicamento.toString(), this.tipo, this.codigo, this.fechacelular,
                                this.fechaaplicacion, this.proximaaplicacion, this.observacion, this.nombreusuario,
                                this.longdate.toString(), this.lat.toString(), this.lon.toString()});
            } else {
                sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                                "medicamentomascota(idmedicamentomascota, codigosistema, mascotaid, actualizado, usuarioid," +
                                "medicamentoid, tipo, codigo, fechacelular, fechaaplicacion, proximaaplicacion, " +
                                "observacion, nombreusuario, longdate, lat, lon) " +
                                "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        new String[]{this.idmedicamentomascota.toString(), this.codigosistema.toString(), this.mascotaid.toString(), this.actualizado.toString(),
                                this.usuarioid.toString(), this.medicamento.idmedicamento.toString(), this.tipo, this.codigo, this.fechacelular,
                                this.fechaaplicacion, this.proximaaplicacion, this.observacion, this.nombreusuario,
                                this.longdate.toString(), this.lat.toString(), this.lon.toString()});
            }
            if (this.idmedicamentomascota == 0)
                this.idmedicamentomascota = SQLite.sqlDB.getLastId();

            retorno = true;

            Log.d(TAG, "SAVE MEDICAMENTO MASCOTA OK");
        } catch (SQLException ex) {
            Log.d(TAG, ex.getMessage());
            retorno = false;
        }
        return retorno;
    }

    public static boolean SaveList(Integer mascotaid, List<MedicamentoMascota> vacunas) {
        boolean retorno = false;
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            for (MedicamentoMascota vac : vacunas) {
                if (vac.idmedicamentomascota == 0) {
                    sqLiteDatabase.execSQL("INSERT INTO " +
                                    "medicamentomascota(codigosistema, mascotaid, actualizado, usuarioid," +
                                    "medicamentoid, tipo, codigo, fechacelular, fechaaplicacion, proximaaplicacion, " +
                                    "observacion, nombreusuario, longdate, lat, lon) " +
                                    "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            new String[]{vac.codigosistema.toString(), mascotaid.toString(), vac.actualizado.toString(),
                                    vac.usuarioid.toString(), vac.medicamento.idmedicamento.toString(), vac.tipo, vac.codigo, vac.fechacelular,
                                    vac.fechaaplicacion, vac.proximaaplicacion, vac.observacion, vac.nombreusuario,
                                    vac.longdate.toString(), vac.lat.toString(), vac.lon.toString()});
                } else {
                    sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                                    "medicamentomascota(idmedicamentomascota, codigosistema, mascotaid, actualizado, usuarioid," +
                                    "medicamentoid, tipo, codigo, fechacelular, fechaaplicacion, proximaaplicacion, " +
                                    "observacion, nombreusuario, longdate, lat, lon) " +
                                    "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            new String[]{vac.idmedicamentomascota.toString(), vac.codigosistema.toString(), mascotaid.toString(), vac.actualizado.toString(),
                                    vac.usuarioid.toString(), vac.medicamento.idmedicamento.toString(), vac.tipo, vac.codigo, vac.fechacelular,
                                    vac.fechaaplicacion, vac.proximaaplicacion, vac.observacion, vac.nombreusuario,
                                    vac.longdate.toString(), vac.lat.toString(), vac.lon.toString()});
                }
                if (vac.idmedicamentomascota == 0)
                    vac.idmedicamentomascota = SQLite.sqlDB.getLastId();
            }
            sqLiteDatabase.close();
            Log.d(TAG, "SAVE LISTA MEDICAMENTO MASCOTA OK");
            retorno = true;

        } catch (SQLException ex) {
            Log.d(TAG, ex.getMessage());
            retorno = false;
        }
        return retorno;
    }

    public static MedicamentoMascota getById(Integer Id) {
        MedicamentoMascota Item = null;
        try {
            sqLiteDatabase = SQLite.sqlDB.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM medicamentomascota WHERE idmedicamentomascota = ?", new String[]{Id.toString()});
            if (cursor.moveToFirst()) Item = MedicamentoMascota.AsignaDatos(cursor);
            sqLiteDatabase.close();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return Item;
    }

    public static List<MedicamentoMascota> getByMascota(Integer idMascota, String tipo) {
        List<MedicamentoMascota> retorno = new ArrayList<>();
        try {
            sqLiteDatabase = SQLite.sqlDB.getReadableDatabase();
            Cursor cursor = null;
            if(tipo.equals(""))
                cursor = sqLiteDatabase.rawQuery("SELECT * FROM medicamentomascota WHERE mascotaid  = ? ORDER BY tipo, longdate DESC", new String[]{idMascota.toString()});
            else
                cursor = sqLiteDatabase.rawQuery("SELECT * FROM medicamentomascota WHERE mascotaid  = ? AND tipo = ? ORDER BY longdate DESC", new String[]{idMascota.toString(), tipo});
            if (cursor.moveToFirst()) {
                do {
                    MedicamentoMascota Item = MedicamentoMascota.AsignaDatos(cursor);
                    if (Item != null)
                        retorno.add(Item);
                } while (cursor.moveToNext());
            }
            sqLiteDatabase.close();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return retorno;
    }

    public static ArrayList<MedicamentoMascota> getMedicamentoSC(Integer idMascota) {
        ArrayList<MedicamentoMascota> items = new ArrayList<>();
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM medicamentomascota where mascotaid = ? AND (codigosistema = 0 or actualizado = 1)", new String[]{idMascota.toString()});
            MedicamentoMascota mascota;
            if (cursor.moveToFirst()) {
                do {
                    mascota = MedicamentoMascota.AsignaDatos(cursor);
                    if (mascota != null) items.add(mascota);
                } while (cursor.moveToNext());
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
            sqLiteDatabase.update("medicamentomascota", data, "idmedicamentomascota = ?", new String[]{id.toString()});
            sqLiteDatabase.close();
            Log.d(TAG, "UPDATE medicamentomascota OK");
            return true;
        } catch (SQLException ex) {
            Log.d(TAG, "Update(): " + ex.getMessage());
            return false;
        }
    }

    public static boolean Delete(Integer idUsuario) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("DELETE FROM medicamentomascota WHERE codigosistema <> ? and usuarioid = ?", new String[]{"0", idUsuario.toString()});
            sqLiteDatabase.close();
            Log.d(TAG, "MEDICAMENTOS ELIMINADOS");
            return true;
        } catch (Exception ec) {
            ec.printStackTrace();
            Log.d(TAG, ec.getMessage());
        }
        return false;
    }

    public static MedicamentoMascota AsignaDatos(Cursor cursor) {
        MedicamentoMascota Item = null;
        try {
            Item = new MedicamentoMascota();
            Item.idmedicamentomascota = cursor.getInt(0);
            Item.codigosistema = cursor.getInt(1);
            Item.mascotaid = cursor.getInt(2);
            Item.actualizado = cursor.getInt(3);
            Item.usuarioid = cursor.getInt(4);
            Item.medicamento = Medicamento.get(cursor.getInt(5));
            Item.tipo = cursor.getString(6);
            Item.codigo = cursor.getString(7);
            Item.fechacelular = cursor.getString(8);
            Item.fechaaplicacion = cursor.getString(9);
            Item.proximaaplicacion = cursor.getString(10);
            Item.observacion = cursor.getString(11);
            Item.nombreusuario = cursor.getString(12);
            Item.longdate = cursor.getLong(13);
            Item.lat = cursor.getDouble(14);
            Item.lon = cursor.getDouble(15);
        } catch (SQLiteException ec) {
            Log.d(TAG, ec.getMessage());
        }
        return Item;
    }

    public static String GeneraCodigo(String tipo) {
        return tipo.concat("-" + Utils.getDateFormat("yyMMdd-HHmmss"));
    }
}
