package com.florencia.simascotas.models;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.florencia.simascotas.services.SQLite;
import com.florencia.simascotas.utils.Constants;
import com.florencia.simascotas.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import static android.content.Context.MODE_PRIVATE;

public class Usuario {
    public int IdUsuario;
    public String Usuario;
    public String RazonSocial;
    public String Clave;
    public String Pin;
    public Sucursal sucursal;
    public Integer ParroquiaID;
    public int Autorizacion;
    public int Perfil;
    public String nombrePerfil;

    public List<Permiso> permisos;

    public static SQLiteDatabase sqLiteDatabase;

    public Usuario() {
        this.RazonSocial = "";
        this.Usuario = "";
        this.Clave = "";
        this.Perfil = 0;
        this.Pin = "";
        this.sucursal = new Sucursal();
        this.Autorizacion = 0;
        this.ParroquiaID = 0;
        this.permisos = new ArrayList<>();
        this.nombrePerfil = "";
        //this.context = context;
    }

    public String Codigo() {return String.valueOf(this.IdUsuario); }

    public boolean Guardar(){
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                            "usuario(idusuario, razonsocial, usuario, clave, perfil, autorizacion, pin, sucursalid, parroquiaid, nombreperfil) " +
                            "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    new String[]{
                            String.valueOf(this.IdUsuario),
                            this.RazonSocial,
                            this.Usuario,
                            this.Clave,
                            String.valueOf(this.Perfil),
                            String.valueOf(this.Autorizacion),
                            this.Pin,
                            this.sucursal.IdEstablecimiento == null ? "0": this.sucursal.IdEstablecimiento.toString(),
                            this.ParroquiaID.toString(),
                            this.nombrePerfil.toUpperCase()
                    }
            );
            sqLiteDatabase.close();
            Log.d("TAG", "USUARIO INGRESADO LOCALMENTE");
            return Permiso.SaveLista(this.permisos);
        } catch (SQLException ex){
            Log.d("TAG", ex.getMessage());
            return false;
        }
    }

    static public Usuario getUsuario(Integer id){
        Usuario Item = null;
        sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM usuario WHERE idusuario = ?", new String[]{ id.toString() });
        if (cursor.moveToFirst()) {
            Item = AsignaDatos(cursor);
        }
        cursor.close();
        sqLiteDatabase.close();
        return Item;
    }

    private static Usuario AsignaDatos(Cursor cursor){
        Usuario Item = new Usuario();
        Item.IdUsuario = cursor.getInt(0);
        Item.RazonSocial = cursor.getString(1);
        Item.Usuario = cursor.getString(2);
        Item.Clave = cursor.getString(3);
        Item.Autorizacion = cursor.getInt(4);
        Item.Pin = cursor.getString(5);
        Item.Perfil = cursor.getInt(6);
        Item.sucursal =  Sucursal.getSucursal(String.valueOf(cursor.getInt(7)));
        Item.ParroquiaID = cursor.getInt(8);
        Item.nombrePerfil = cursor.getString(9);
        Item.permisos = Permiso.getPermisos(Item.Perfil);

        return Item;
    }

    static public Usuario Login(String Pin)
    {
        Usuario Item = null;
        try {
            sqLiteDatabase = SQLite.sqlDB.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM usuario WHERE pin = ?", new String[]{ Pin });
            if (cursor.moveToFirst()) {
                Item = AsignaDatos(cursor);
            }
            cursor.close();
            sqLiteDatabase.close();
        } catch(SQLException ex)
        {
            Log.d("LOGINERROR",String.valueOf(ex));
            ex.printStackTrace();
        }
        Log.d("LOGINITEM",String.valueOf(Item));
        return Item;
    }

    static public Usuario Login(String User, String Password)
    {
        Usuario Item = null;
        try {
            sqLiteDatabase = SQLite.sqlDB.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM usuario WHERE usuario = ? and clave = ?", new String[]{ User, Password });
            if (cursor.moveToFirst()) {
                Item = AsignaDatos(cursor);
                Item.CapturarPosicion();
            }
            cursor.close();
            sqLiteDatabase.close();
        } catch(SQLException ex)
        {
            Log.d("LOGINERROR",String.valueOf(ex));
            ex.printStackTrace();
        }
        Log.d("LOGINITEM",String.valueOf(Item));
        return Item;
    }

    static public List<Usuario> getUsuarios(){
        List<Usuario> Items = new ArrayList<Usuario>();
        sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM usuario", null);
        Usuario Item;
        if (cursor.moveToFirst()) {
            do {
                Item = AsignaDatos(cursor);
                if (Item != null) Items.add(Item);
            } while(cursor.moveToNext());
        }
        cursor.close();
        sqLiteDatabase.close();
        return Items;
    }

    public boolean removeClientes() {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("DELETE FROM cliente WHERE usuario = ? AND codigosistema <> 0", new String[] {this.Codigo()});
            sqLiteDatabase.close();
            return true;
        } catch (Exception ec) {
            ec.printStackTrace();
        }
        return false;
    }

    public HashMap<String, String> MapLogin() {
        HashMap<String, String> nMap = null;
        nMap = new HashMap<>();
        nMap.put("usuario", this.Usuario);
        nMap.put("clave", this.Clave);
        return nMap;
    }

    public void updatePosicion(int i, int est) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("UPDATE posicion SET estado = ? WHERE idposicion = ?", new String[] {String.valueOf(est), String.valueOf(i)});
            sqLiteDatabase.close();
        } catch (Exception ec) {
            ec.printStackTrace();
        }
    }

    public JsonArray getResumenDocumentos(Context ctx, String fecha){
        JsonArray resumen = new JsonArray();
        try{
            String query = "";
            if(SQLite.usuario.VerificaPermiso(ctx, Constants.PUNTO_VENTA, "lectura"))
                query = "select 'FACTURAS' documento, count(co.idcomprobante) as cantidad, round(ifnull(sum(co.total),0),2) as total, " +
                        "(select count(idcomprobante) from comprobante where tipotransaccion = '01' and estado >= 0 and codigosistema = 0 and fechadocumento = '"+fecha+"'" +
                        " and usuarioid = "+SQLite.usuario.IdUsuario+") as cantidadns " +
                        "from comprobante co where co.tipotransaccion = '01' and estado >= 0 and fechadocumento = '"+fecha+"' and usuarioid =" + SQLite.usuario.IdUsuario;
            if(SQLite.usuario.VerificaPermiso(ctx, Constants.RECEPCION_INVENTARIO, "lectura")) {
                if(query.length()>0) query += " UNION";
                query += " select 'RECEPCIONES', count(co.idcomprobante), round(ifnull(sum(co.total),0),2), 0 from comprobante co where co.tipotransaccion in ('8','23') and estado >= 0 and fechadocumento = '" + fecha + "' and usuarioid =" + SQLite.usuario.IdUsuario;
            }
            if(SQLite.usuario.VerificaPermiso(ctx, Constants.TRANSFERENCIA_INVENTARIO, "lectura")) {
                if(query.length()>0) query += " UNION";
                query += " select 'TRANSFERENCIAS', count(co.idcomprobante), round(ifnull(sum(co.total),0),2), 0 from comprobante co where co.tipotransaccion in ('4','20') and estado >= 0 and fechadocumento = '" + fecha + "' and usuarioid =" + SQLite.usuario.IdUsuario;
            }
            if(SQLite.usuario.VerificaPermiso(ctx, Constants.PEDIDO, "lectura")) {
                if(query.length()>0) query += " UNION";
                query += " select 'PEDIDOS CLIENTE', count(pe.idpedido), round(ifnull(sum(pe.total),0),2), " +
                        "(select count(idpedido) as cantidad from pedido where estado >= 0 and codigosistema = 0 and (fechapedido = '" + fecha + "' or fechacelular like '" + fecha + "%')  and usuarioid =" + SQLite.usuario.IdUsuario +") " +
                        "from pedido pe where pe.estado >= 0 and (fechapedido = '" + fecha + "' or fechacelular like '" + fecha + "%') and usuarioid =" + SQLite.usuario.IdUsuario;
            }
            if(SQLite.usuario.VerificaPermiso(ctx, Constants.PEDIDO_INVENTARIO, "lectura")){
                if(query.length()>0) query += " UNION";
                query += " select 'PEDIDOS INVENTARIO', count(pe.idpedido), 0, " +
                        "(select count(idpedido) as cantidad from pedidoinv where estadomovil = 1 and codigosistema = 0 and fechahora like '"+fecha+"%' and usuarioid = "+SQLite.usuario.IdUsuario+")" +
                        "from pedidoinv pe where pe.estadomovil >= 0 and fechahora like '"+fecha+"%' and usuarioid = " + SQLite.usuario.IdUsuario;
            }

            if(SQLite.usuario.VerificaPermiso(ctx, Constants.REGISTRO_CLIENTE, "lectura")){
                if(query.length()>0) query += " UNION";
                query += " select 'PERSONAS'," +
                        "(select count(*) from cliente where usuarioid = "+SQLite.usuario.IdUsuario+" and fecharegistro like '"+fecha+"%')," +
                        "count(*)," +
                        "(select count(*) from cliente where usuarioid = "+SQLite.usuario.IdUsuario+" and fechamodificacion like '"+fecha+"%' and actualizado = 1) " +
                        "from cliente where usuarioid = " + SQLite.usuario.IdUsuario + " and nip not like '999999999%'";
            }

            if(SQLite.usuario.VerificaPermiso(ctx, Constants.VISITA_GANADERO, "lectura")){
                if(query.length()>0) query += " UNION";
                query += " select 'MASCOTAS'," +
                        "(select count(*) from mascota where usuarioid = "+SQLite.usuario.IdUsuario+" and fechacelular like '"+fecha+"%')," +
                        "count(*)," +
                        "(select count(*) from mascota where usuarioid = "+SQLite.usuario.IdUsuario+" and actualizado = 1) " +
                        "from mascota " +
                        "join cliente cli on cli.idcliente = duenoid and cli.usuarioid = " + SQLite.usuario.IdUsuario;
            }

            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("documento", cursor.getString(0));
                    obj.addProperty("cantidad", cursor.getInt(1));
                    obj.addProperty("total", cursor.getDouble(2));
                    obj.addProperty("cantidadns", cursor.getInt(3));
                    resumen.add(obj);
                } while(cursor.moveToNext());
            }
            cursor.close();
            sqLiteDatabase.close();

        }catch (SQLiteException e){
            Log.d("TAG_USUARIO", "getResumenDocumentos(): " + e.getMessage());
        }
        return resumen;
    }

    public static int numDocNoSincronizados(Integer idUsuario, String tipodoc){
        int cant = 0;
        try{
            String query = "";
            if(tipodoc.equals("01"))  //FACTURAS
                query = "SELECT COUNT(*) FROM comprobante WHERE tipotransaccion = '01' AND codigosistema = 0 AND estado = 0 AND usuarioid = ?";
            else if(tipodoc.equals("PC"))//PEDIDOS CLIENTE
                query = "SELECT count(*) FROM pedido WHERE codigosistema = 0 AND estado = 1 AND usuarioid = ?";
            else if(tipodoc.equals("PI"))//PEDIDOS INVENTARIO
                query = "SELECT count(*) FROM pedidoinv WHERE codigosistema = 0 AND estadomovil = 1 AND usuarioid = ?";

            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery(query, new String[]{idUsuario.toString()});
            if (cursor.moveToFirst())
                cant = cursor.getInt(0);
        }catch (Exception e){
            Log.d("TAGUSUARIO", "numDocNoSincronizados(): " + e.getMessage());
        }
        return cant;
    }

    public boolean GuardarSesionLocal (Context context){
        //Crea preferencia
        SharedPreferences sharedPreferences= context.getSharedPreferences("DatosSesion", MODE_PRIVATE);
        String conexionactual = sharedPreferences.getString("conexionactual","");
        SharedPreferences.Editor editor=sharedPreferences.edit()
                .putInt("idUser", this.IdUsuario)
                .putString("usuario", this.Usuario)
                .putString("pin", this.Pin)
                .putString("ultimaconexion", conexionactual)
                .putString("conexionactual", Utils.getDateFormat("dd MMM yyyy HH:mm"))
                .putString("rucempresa", this.sucursal.RUC);
        return editor.commit();
    }

    public boolean CerrarSesionLocal (Context context){
        //Crea preferencia
        SharedPreferences sharedPreferences= context.getSharedPreferences("DatosSesion", MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit()
                .clear();
        return editor.commit();
    }

    public boolean VerificaPermiso(Context context, String opcion, String permiso){
        boolean retorno = false;
        Permiso mipermiso=null;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                List<Permiso> collect = permisos.stream().filter(i -> i.rutaopcion.trim().equalsIgnoreCase(opcion.trim().toUpperCase())).
                        collect(Collectors.toList());
                if (collect != null && collect.size() > 0)
                    mipermiso = collect.get(0);
            } else {
                for (Permiso detalle : permisos) {
                    if (detalle.rutaopcion.equalsIgnoreCase(permiso.trim().toUpperCase())) {
                        mipermiso = detalle;
                        break;
                    }
                }
            }

            if (mipermiso != null) {
                switch (permiso) {
                    case "lectura":
                        retorno = mipermiso.permisoimpresion.equals("t");
                        break;
                    case "escritura":
                        retorno = mipermiso.permisoescritura.equals("t");
                        break;
                    case "modificacion":
                        retorno = mipermiso.permisomodificacion.equals("t");
                        break;
                    case "borrar":
                        retorno = mipermiso.permisoborrar.equals("t");
                        break;
                }
            }
        }catch (Exception e){
            Log.d("TAGPERMISO", "VerificarPermiso(): " + e.getMessage());
            retorno = false;
        }

        return  retorno;
    }

    public void CapturarPosicion() {
        final Handler handler = new Handler();
        final Usuario usuario = this;
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            //Ejecuta tu AsyncTask!
                            @SuppressLint("StaticFieldLeak") AsyncTask myTask = new AsyncTask() {
                                @Override
                                protected Object doInBackground(Object[] objects) {
                                    if(SQLite.gpsTracker != null) {
                                        SQLite.gpsTracker.getLastKnownLocation();
                                        SQLite.gpsTracker.updateGPSCoordinates();
                                        Ubicacion.Save(usuario.IdUsuario, usuario.sucursal.RUC,
                                                SQLite.gpsTracker.getLatitude(), SQLite.gpsTracker.getLongitude());
                                        Log.d("addPosition", "Agregando position local");
                                    }
                                    return null;
                                }
                            };
                            myTask.execute();
                        } catch (Exception e) {
                            Log.d("erroraddPosition", e.getMessage());
                        }
                    }
                });
            }
        };
        timer.schedule(task, 0, 1000 * 60 * 15);
    }
}

