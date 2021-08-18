package com.florencia.simascotas.models;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.florencia.simascotas.services.SQLite;

import java.util.ArrayList;
import java.util.List;

public class Medicamento {
    public Integer idmedicamento, numfrecuencia;
    public String tipo, nombre, descripcion, frecuencia;

    public static final String TAG = "TAGMEDICAMENTO";
    public static SQLiteDatabase sqLiteDatabase;

    public Medicamento(String tipo, String nombre) {
        this.idmedicamento = 0;
        this.tipo = tipo;
        this.nombre = nombre;
        this.frecuencia = "DIARIA";
        this.numfrecuencia = 0;
    }

    public Medicamento() {
        this.idmedicamento = numfrecuencia = 0;
        this.tipo = this.nombre = this.descripcion = this.frecuencia = "";
    }

    public static boolean SaveLista(List<Medicamento> medicamentos) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            for (Medicamento item : medicamentos) {
                sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                                "medicamento(idmedicamento, numfrecuencia, tipo, nombre, descripcion," +
                                "frecuencia) values(?, ?, ?, ?, ?, ?)",
                        new String[]{item.idmedicamento.toString(), item.numfrecuencia.toString(), item.tipo,
                                item.nombre, item.descripcion, item.frecuencia});
            }
            sqLiteDatabase.close();
            Log.d(TAG, "Guardó lista de medicamentos");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d(TAG, "SaveLista(): " + ex.getMessage());
            return false;
        }
    }

    public static Medicamento get(Integer codigo) {
        Medicamento retorno = null;
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM medicamento where idmedicamento = ?", new String[]{codigo.toString()});
            if (cursor.moveToFirst())
                retorno = Medicamento.AsignaDatos(cursor);
            cursor.close();
            sqLiteDatabase.close();
        } catch (Exception ec) {
            Log.d(TAG, "get()" + ec.getMessage());
            ec.printStackTrace();
        }
        return retorno;
    }

    public static List<Medicamento> getList(String tipo) {
        List<Medicamento> lista = new ArrayList<>();
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery(
                    "SELECT * FROM medicamento WHERE tipo = ?",
                    new String[]{tipo});
            Medicamento med;
            if (cursor.moveToFirst()) {
                do {
                    med = Medicamento.AsignaDatos(cursor);
                    if (med != null) lista.add(med);
                } while (cursor.moveToNext());
            }
            cursor.close();
            sqLiteDatabase.close();
        } catch (Exception ec) {
            Log.d(TAG, "getList" + ec.getMessage());
            ec.printStackTrace();
        }
        return lista;
    }

    public static Medicamento AsignaDatos(Cursor cursor) {
        Medicamento Item = null;
        try {
            Item = new Medicamento();
            Item.idmedicamento = cursor.getInt(0);
            Item.numfrecuencia = cursor.getInt(1);
            Item.tipo = cursor.getString(2);
            Item.nombre = cursor.getString(3);
            Item.descripcion = cursor.getString(4);
            Item.frecuencia = cursor.getString(5);
        } catch (SQLiteException ec) {
            Log.d(TAG, ec.getMessage());
        }
        return Item;
    }

    @Override
    public String toString() {
        return this.nombre + (this.idmedicamento > 0 ? " >> " + this.frecuencia : "");
    }
}
