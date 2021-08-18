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

public class Mascota {
    public Integer idmascota, codigosistema, duenoid, actualizado, usuarioid;
    public String nombre, fechacelular, fechanacimiento, color1, color2, sexo,
            observacion, nipdueno, codigomascota;
    public Double peso, lat, lon;
    public Long longdaten;
    public List<Foto> fotos;
    public Catalogo especie, raza;
    public List<Consulta> consultas;
    public List<MedicamentoMascota> vacunas;

    public static SQLiteDatabase sqLiteDatabase;
    public static final String TAG = "TAGMASCOTA";

    public Mascota(){
        this.idmascota = 0;
        this.codigosistema = 0;
        this.duenoid = 0;
        this.actualizado = 0;
        this.nombre = "";
        this.fechacelular = "";
        this.fechanacimiento = "";
        this.especie = new Catalogo();
        this.raza = new Catalogo();
        this.color1 =  "";
        this.color2 = "";
        this.sexo = "";
        this.observacion = "";
        this.peso = 0d;
        this.lat = 0d;
        this.lon = 0d;
        this.longdaten = 0l;
        this.usuarioid = 0;
        this.nipdueno = "";
        this.codigomascota = "";
        this.fotos = new ArrayList<>();
        this.consultas = new ArrayList<>();
        this.vacunas = new ArrayList<>();
    }

    public boolean Save() {
        boolean retorno = false;
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            if (this.idmascota == 0) {
                sqLiteDatabase.execSQL("INSERT INTO " +
                                "mascota(codigosistema,duenoid,nombre,fechacelular,fechanacimiento," +
                                "especieid,razaid,color1,color2,peso,sexo,observacion,actualizado,lat,lon," +
                                "longdaten, usuarioid, nipdueno, codigomascota) " +
                                "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?)",
                        new String[]{this.codigosistema.toString(), this.duenoid.toString(), this.nombre,
                                this.fechacelular, this.fechanacimiento, this.especie.codigocatalogo, this.raza.codigocatalogo, this.color1,
                                this.color2, this.peso.toString(), this.sexo, this.observacion, this.actualizado.toString(),
                                this.lat.toString(), this.lon.toString(), this.longdaten.toString(), this.usuarioid.toString(), this.nipdueno, this.codigomascota});
            } else {
                sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                                "mascota(idmascota,codigosistema,duenoid,nombre,fechacelular,fechanacimiento," +
                                "especieid,razaid,color1,color2,peso,sexo,observacion,actualizado,lat,lon,longdaten, " +
                                "usuarioid, nipdueno, codigomascota) " +
                                "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        new String[]{this.idmascota.toString(), this.codigosistema.toString(), this.duenoid.toString(), this.nombre,
                                this.fechacelular, this.fechanacimiento, this.especie.codigocatalogo, this.raza.codigocatalogo, this.color1,
                                this.color2, this.peso.toString(), this.sexo, this.observacion, this.actualizado.toString(),
                                this.lat.toString(), this.lon.toString(), this.longdaten.toString(), this.usuarioid.toString(),
                                this.nipdueno, this.codigomascota});
            }
            if (this.idmascota == 0) this.idmascota = SQLite.sqlDB.getLastId();

            retorno = true;

            if(this.fotos.size()>0)
                retorno =  retorno && Foto.SaveList(this.duenoid, this.idmascota, this.fotos, "M");
            if(this.consultas.size()>0)
                retorno = retorno && Consulta.SaveList(this.idmascota, this.consultas);
            if(this.vacunas.size()>0)
                retorno = retorno && MedicamentoMascota.SaveList(this.idmascota, this.vacunas);
            sqLiteDatabase.close();
            Log.d(TAG, "SAVE LISTA DE MASCOTAS OK");
        } catch (SQLException ex) {
            Log.d(TAG, ex.getMessage());
            retorno = false;
        }
        return retorno;
    }

    public static boolean SaveList(List<Mascota> mascotas) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            for (Mascota mas : mascotas) {
                if (mas.idmascota == 0) {
                    sqLiteDatabase.execSQL("INSERT INTO " +
                                    "mascota(codigosistema,duenoid,nombre,fechacelular,fechanacimiento," +
                                    "especieid,razaid,color1,color2,peso,sexo,observacion,actualizado,lat,lon," +
                                    "longdaten, usuarioid, nipdueno, codigomascota) " +
                                    "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?)",
                            new String[]{mas.codigosistema.toString(), mas.duenoid.toString(), mas.nombre,
                                    mas.fechacelular, mas.fechanacimiento, mas.especie.codigocatalogo, mas.raza.codigocatalogo, mas.color1,
                                    mas.color2, mas.peso.toString(), mas.sexo, mas.observacion, mas.actualizado.toString(),
                                    mas.lat.toString(), mas.lon.toString(), mas.longdaten.toString(), mas.usuarioid.toString(), mas.nipdueno, mas.codigomascota});
                } else {
                    sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                                    "propiedad(idmascota,codigosistema,duenoid,nombre,fechacelular,fechanacimiento," +
                                    "especieid,razaid,color1,color2,peso,sexo,observacion,actualizado,lat,lon,longdaten, " +
                                    "usuarioid, nipdueno, codigomascota) " +
                                    "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            new String[]{mas.idmascota.toString(), mas.codigosistema.toString(), mas.duenoid.toString(), mas.nombre,
                                    mas.fechacelular, mas.fechanacimiento, mas.especie.codigocatalogo, mas.raza.codigocatalogo, mas.color1,
                                    mas.color2, mas.peso.toString(), mas.sexo, mas.observacion, mas.actualizado.toString(),
                                    mas.lat.toString(), mas.lon.toString(), mas.longdaten.toString(), mas.usuarioid.toString(),
                                    mas.nipdueno, mas.codigomascota});
                }
                if (mas.idmascota == 0) mas.idmascota = SQLite.sqlDB.getLastId();

                Foto.SaveList(mas.duenoid, mas.idmascota, mas.fotos, "M");
            }
            sqLiteDatabase.close();
            Log.d(TAG, "SAVE LISTA DE MASCOTAS OK");
            return true;
        } catch (SQLException ex) {
            Log.d(TAG, ex.getMessage());
            return false;
        }
    }

    public static Mascota getById(Integer Id) {
        Mascota Item = null;
        try {
            sqLiteDatabase = SQLite.sqlDB.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM mascota WHERE idmascota = ?", new String[]{Id.toString()});
            if (cursor.moveToFirst()) Item = Mascota.AsignaDatos(cursor, false, false);
            sqLiteDatabase.close();
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return Item;
    }

    public static List<Mascota> getByPropietario(Integer idDueno, boolean todos) {
        List<Mascota> retorno = new ArrayList<>();
        try {
            String query = "SELECT * FROM mascota WHERE duenoid  = ?";
            if(todos)
                query = "SELECT mas.* FROM mascota mas JOIN cliente cli ON cli.idcliente = mas.duenoid and cli.usuarioid = ?";
            sqLiteDatabase = SQLite.sqlDB.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery(query, new String[]{todos?String.valueOf(SQLite.usuario.IdUsuario):idDueno.toString()});
            if (cursor.moveToFirst()) {
                do {
                    Mascota Item = Mascota.AsignaDatos(cursor, false, false);
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

    public static ArrayList<Mascota> getMascotaSC(Integer idDueno) {
        ArrayList<Mascota> items = new ArrayList<>();
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM mascota where duenoid = ? and (codigosistema = 0 or actualizado = 1)", new String[]{idDueno.toString()});
            Mascota mascota;
            if (cursor.moveToFirst()) {
                do {
                    mascota = Mascota.AsignaDatos(cursor, false, true);
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
            sqLiteDatabase.update("mascota",data, "idmascota = ?",new String[]{id.toString()});
            sqLiteDatabase.close();
            Log.d(TAG,"UPDATE mascota OK");
            return true;
        } catch (SQLException ex){
            Log.d(TAG, "Update(): " + String.valueOf(ex));
            return false;
        }
    }

    public static boolean removeMascotas(Integer idUsuario) {
        try {
            Consulta.removeConsultas(idUsuario);
            MedicamentoMascota.Delete(idUsuario);
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("DELETE FROM mascota WHERE codigosistema <> ? and usuarioid = ?", new String[] {"0", idUsuario.toString()});
            sqLiteDatabase.close();
            Log.d(TAG, "MASCOTAS ELIMINADOS");
            return true;
        } catch (Exception ec) {
            ec.printStackTrace();
            Log.d(TAG, ec.getMessage());
        }
        return false;
    }

    public static Mascota AsignaDatos(Cursor cursor, boolean detalle, boolean sincronizacion) {
        Mascota Item = null;
        try {
            Item = new Mascota();
            Item.idmascota = cursor.getInt(0);
            Item.codigosistema = cursor.getInt(1);
            Item.duenoid = cursor.getInt(2);
            Item.nombre = cursor.getString(3);
            Item.fechacelular = cursor.getString(4);
            Item.fechanacimiento = cursor.getString(5);
            Item.especie = Catalogo.getByPadre(cursor.getString(6), "ESPECIE");
            Item.raza = Catalogo.getByPadre(cursor.getString(7), Item.especie.codigocatalogo);
            Item.color1 = cursor.getString(8);
            Item.color2 = cursor.getString(9);
            Item.peso = cursor.getDouble(10);
            Item.sexo = cursor.getString(11);
            Item.observacion = cursor.getString(12);
            Item.actualizado = cursor.getInt(13);
            Item.lat = cursor.getDouble(14);
            Item.lon = cursor.getDouble(15);
            Item.longdaten = cursor.getLong(16);
            Item.usuarioid = cursor.getInt(17);
            Item.nipdueno = cursor.getString(18);
            Item.codigomascota = cursor.getString(19);
            Item.fotos = Foto.getLista(Item.duenoid, Item.idmascota, "M");

            if(detalle) {
                Item.consultas = Consulta.getByMascota(Item.idmascota);
                Item.vacunas = MedicamentoMascota.getByMascota(Item.idmascota, "");
            }else if(sincronizacion){
                Item.consultas = Consulta.getConsultaSC(Item.idmascota);
                Item.vacunas = MedicamentoMascota.getMedicamentoSC(Item.idmascota);
            }

            if(Item.fotos == null)
                Item.fotos = new ArrayList<>();
        } catch (SQLiteException ec) {
            Log.d(TAG, ec.getMessage());
        } catch (Exception ex){
            Log.d(TAG, ex.getMessage());
        }
        return Item;
    }

    public static String GeneraCodigo(){
        return "MASC-".concat(Utils.getDateFormat("yyMMdd-HHmmss"));
    }
}
