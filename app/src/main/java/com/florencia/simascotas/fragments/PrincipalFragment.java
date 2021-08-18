package com.florencia.simascotas.fragments;

import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.florencia.simascotas.BuildConfig;
import com.florencia.simascotas.MainActivity;
import com.florencia.simascotas.R;
import com.florencia.simascotas.activities.ComprobanteActivity;
import com.florencia.simascotas.adapters.ResumenAdapter;
import com.florencia.simascotas.interfaces.ProductoInterface;
import com.florencia.simascotas.interfaces.UsuarioInterface;
import com.florencia.simascotas.models.Ubicacion;
import com.florencia.simascotas.services.SQLite;
import com.florencia.simascotas.utils.Constants;
import com.florencia.simascotas.utils.DownloadFile;
import com.florencia.simascotas.utils.FileDownloader;
import com.florencia.simascotas.utils.Utils;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PrincipalFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    public final static String TAG = "TAG_PRINCIPAL_FRAGMENT";
    private View view;
    private Toolbar toolbar;
    private RecyclerView rvResumen;
    private ResumenAdapter adapterResumen;
    private Button btnFecha;
    private ImageButton btnFechaSig, btnFechaAnt;
    TextView lblVersion, lblUpdate;
    Calendar calendar;
    DatePickerDialog dtpDialog;
    ProgressBar progressBar;

    private OkHttpClient okHttpClient;

    public PrincipalFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_principal, container, false);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        rvResumen = view.findViewById(R.id.rvResumen);
        btnFecha = view.findViewById(R.id.btnFecha);
        btnFechaAnt = view.findViewById(R.id.btnFechaAnterior);
        btnFechaSig = view.findViewById(R.id.btnFechaSiguiente);
        lblVersion = view.findViewById(R.id.lblVersion);
        lblUpdate = view.findViewById(R.id.lblUpdate);
        progressBar = view.findViewById(R.id.progressBar);

        String fecha = Utils.getDateFormat("yyyy-MM-dd");
        String[] fecA = fecha.split("-");
        btnFecha.setText(fecA[2] + "-" + Utils.getMes(Integer.valueOf(fecA[1])-1,true) + "-" + fecA[0]);
        btnFecha.setTag(fecha);

        lblVersion.setText("v".concat(BuildConfig.VERSION_NAME));
        lblUpdate.setVisibility(View.GONE);
        lblVersion.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        btnFecha.setOnClickListener(this::onClick);
        btnFechaAnt.setOnClickListener(this::onClick);
        btnFechaSig.setOnClickListener(this::onClick);
        lblUpdate.setOnClickListener(this::onClick);
        lblUpdate.setOnLongClickListener(this::onLongClick);
        okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        return view;
    }

    public void showDatePickerDialog() {
        Locale l = new Locale("ES-es");
        calendar = Calendar.getInstance(l);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        String[] fecha = btnFecha.getTag().toString().split("-");
        day = Integer.valueOf(fecha[2]);
        month = Integer.valueOf(fecha[1]) - 1;
        year = Integer.valueOf(fecha[0]);
        dtpDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                String dia = (day >= 0 && day < 10 ? "0" + (day) : String.valueOf(day));
                String mes = (month >= 0 && month < 10 ? "0" + (month + 1) : String.valueOf(month + 1));

                String mitextoU = year + "-" + mes + "-" + dia;
                btnFecha.setTag(mitextoU);
                btnFecha.setText(dia + "-" + Utils.getMes(month, true) + "-" + year);
                BuscaResumen(mitextoU);
            }
        }, year, month, day);
        dtpDialog.show();
    }

    public void BuscaResumen(String fecha) {
        try {
            if (fecha.equals(""))
                fecha = btnFecha.getTag().toString().trim();
            if (fecha.equals(""))
                fecha = Utils.getDateFormat("yyyy-MM-dd");
            JsonArray jDatos = SQLite.usuario.getResumenDocumentos(getContext(), fecha);
            if (jDatos != null) {
                adapterResumen = new ResumenAdapter(getActivity(), jDatos, fecha);
                rvResumen.setAdapter(adapterResumen);
            }
        } catch (Exception e) {
            Log.d(TAG, "BuscaResumen(): " + e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.btnFecha:
                    showDatePickerDialog();
                    break;
                case R.id.btnFechaAnterior:
                    CambiarFecha(-1);
                    break;
                case R.id.btnFechaSiguiente:
                    CambiarFecha(1);
                    break;
                case R.id.lblUpdate:
                    //progressBar.setVisibility(View.VISIBLE);
                    //new DownloadFile2().execute(SQLite.configuracion.url_ws.concat(Constants.URL_DOWNLOAD_APK), "erpv_" + SQLite.newversion + ".apk",
                      //      getActivity().getExternalFilesDir(null).toString());
                    DescargaApk();
                    /*Log.d(TAG,"PATH(): " + getActivity().getExternalFilesDir(null).toString());
                    new DownloadFile().execute(SQLite.configuracion.url_ws.concat(Constants.URL_DOWNLOAD_APK), "erp-realese.apk",
                            getActivity().getExternalFilesDir(null).toString());
                    if(Utils.CopyToClipboard(v.getContext(), SQLite.configuracion.url_ws.concat(Constants.URL_DOWNLOAD_APK)))
                        Utils.showMessage(v.getContext(), "URL copiada al portapapeles, utilice el navegador por defecto del celular.");
                    */
                    break;
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()){
            case R.id.lblUpdate:
                return Utils.CopyToClipboard(v.getContext(), SQLite.linkdescarga);
        }
        return false;
    }

    private void DescargaApk() {
        try {
            Log.d(TAG, SQLite.linkdescarga);
            //Uri uri = Uri.parse(SQLite.configuracion.url_ws.concat(Constants.URL_DOWNLOAD_APK));
            Uri uri = Uri.parse(SQLite.linkdescarga);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(uri, "text/html");
            i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(i);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public void getApk(Context context) {
        Retrofit.Builder retrofit = new Retrofit.Builder()
                .baseUrl(SQLite.configuracion.url_ws)
                .client(okHttpClient);
        Retrofit retro = retrofit.build();
        UsuarioInterface interfa = retro.create(UsuarioInterface.class);

        Call<ResponseBody> call = interfa.downloadApk(SQLite.configuracion.url_ws.concat(Constants.URL_DOWNLOAD_APK));
        try {
            Response<ResponseBody> response = call.execute();
            InputStream is = response.body().byteStream();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    // Request code for selecting a PDF document.
    private static final int PICK_PDF_FILE = 2;

    public void openFile(Uri pickerInitialUri) {
        Log.d(TAG,pickerInitialUri.toString());
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("application/vnd.android.package-archive");

        // Optionally, specify a URI for the file that should appear in the
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivity(intent);
    }
    public void openFolder(Uri pickerInitialUri){
        //Uri selectedUri = Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/tuCarpeta");
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pickerInitialUri, "application/vnd.android.package-archive");
            //intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivity(intent);//Intent.createChooser(intent, "Open folder"));
        }catch (Exception e){
            Log.d(TAG, e.getMessage());
        }
    }

    public static boolean installPackage(Context context, InputStream in, String packageName)
            throws IOException {
        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setAppPackageName(packageName);
        // set params
        int sessionId = packageInstaller.createSession(params);
        PackageInstaller.Session session = packageInstaller.openSession(sessionId);
        OutputStream out = session.openWrite("COSU", 0, -1);
        byte[] buffer = new byte[65536];
        int c;
        while ((c = in.read(buffer)) != -1) {
            out.write(buffer, 0, c);
        }
        session.fsync(out);
        in.close();
        out.close();

        session.commit(createIntentSender(context, sessionId));
        return true;
    }


    private static IntentSender createIntentSender(Context context, int sessionId) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                new Intent(Constants.ACTION_INSTALL_COMPLETE),
                0);
        return pendingIntent.getIntentSender();
    }

    private void CambiarFecha(int operador) {
        try {
            String newFecha = Utils.CambiarFecha(btnFecha.getTag().toString().trim(),
                    Calendar.DAY_OF_YEAR, operador);
            String[] newF = newFecha.split("-");
            btnFecha.setText(newF[2] + "-" + Utils.getMes(Integer.valueOf( newF[1])-1,true) + "-" + newF[0] );
            btnFecha.setTag(newFecha);
            BuscaResumen(newFecha);
        } catch (Exception e) {
            Log.d(TAG, "CambiarFecha(): " + e.getMessage());
        }
    }

    @Override
    public void onResume() {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        String titulo = "Inicio";
        toolbar.setTitle(titulo);
        toolbar.setTitleTextColor(Color.WHITE);
        BuscaResumen(""); //Utils.getDateFormat("yyyy-MM-dd"));
        lblVersion.setText("v".concat(BuildConfig.VERSION_NAME));
        //verificarVersion(getContext());
        super.onResume();
    }

    private void verificarVersion(final Context context) {
        try {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SQLite.configuracion.url_ws)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient)
                    .build();
            UsuarioInterface miInterface = retrofit.create(UsuarioInterface.class);

            Call<JsonObject> call = null;
            call = miInterface.getLastVersion();
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (!response.isSuccessful()) {
                        return;
                    }
                    try {
                        if (response.body() != null) {
                            JsonObject obj = response.body();
                            if (obj.has("versionapp")) {
                                if (!obj.get("versionapp").getAsString().equals(BuildConfig.VERSION_NAME)) {
                                    SQLite.newversion = obj.get("versionapp").getAsString();
                                    SQLite.linkdescarga = obj.get("linkapp").getAsString();
                                    lblUpdate.setVisibility(View.VISIBLE);
                                    lblVersion.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                                    lblUpdate.setText(Html.fromHtml(getResources().getString(R.string.update)));
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d(TAG, t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public class DownloadFile2 extends AsyncTask<String, Integer, Boolean> {

        private static final int  MEGABYTE = 2048 * 2048;
        private String ruta = "";
        File pdfFile, folder;
        @Override
        protected Boolean doInBackground(String... strings) {
            String fileUrl = strings[0];   // -> http://maven.apache.org/maven-1.x/maven.pdf
            String fileName = strings[1];  // -> maven.pdf
            String extStorageDirectory = strings[2];//Environment.getExternalStorageDirectory().toString();
            folder = new File(extStorageDirectory, Environment.DIRECTORY_DOWNLOADS);
            folder.mkdir();
            pdfFile = new File(folder, fileName);
            Log.d(TAG,"");
            try {
                pdfFile.createNewFile();
            } catch (IOException e) {
                Log.d("TAG", e.getMessage());
                e.printStackTrace();
            }

            try {
                URL url = new URL(fileUrl);
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(pdfFile);
                int totalSize = urlConnection.getContentLength();
                Log.d("TAG", "tamaio: " + totalSize);
                byte[] buffer = new byte[MEGABYTE];
                int bufferLength = 0;
                //progressBar.setProgress(0);
                int i = 1;
                //installPackage(getContext(),inputStream,Constants.ACTION_INSTALL_COMPLETE);
                while((bufferLength = inputStream.read(buffer))>0 ){
                    fileOutputStream.write(buffer, 0, bufferLength);
                    publishProgress(i, totalSize);
                    i++;
                }
                fileOutputStream.close();
                ruta = folder.getAbsolutePath();
                Log.d("TAGDOWN", "Archivo descargado");
                return true;
            } catch (FileNotFoundException e) {
                Log.d("TAGDOWN", "NotFound(): "+ e.getMessage());
                e.printStackTrace();
            } catch (MalformedURLException e) {
                Log.d("TAGDOWN", "Malformed(): "+ e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.d("TAGDOWN", "IO(): "+ e.getMessage());
                e.printStackTrace();
            }
            return false;
            //return FileDownloader.downloadFile(fileUrl, pdfFile, progressBar);
            //return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if(values[0]<=1)
                progressBar.setMax((values[1]/2048));
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result) {
                progressBar.setVisibility(View.GONE);
                Snackbar.make(view, "Se descargó un archivo. ", Snackbar.LENGTH_INDEFINITE)
                        .setActionTextColor(Color.WHITE)
                        .setAction("Instalar", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.d(TAG, "Pulsada acción snackbar!: " + folder.getAbsolutePath());
                                //File ar = new File(folder, "image.jpg");
                                Uri photoURI = FileProvider.getUriForFile(view.getContext(), view.getContext().getApplicationContext().getPackageName() + ".provider", pdfFile);
                                openFolder(Uri.parse("content://"+photoURI.toString()));
                                //installPackage(view.getContext(), pdfFile.)
                            }
                        })
                        .show();
            }
        }

        @Override
        protected void onCancelled() {
            progressBar.setVisibility(View.GONE);
        }
    }

}