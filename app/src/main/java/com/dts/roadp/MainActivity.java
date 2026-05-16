package com.dts.roadp;

import static android.util.Base64.NO_WRAP;
import static android.util.Base64.encodeToString;
import androidx.annotation.NonNull;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import Facturacion.HttpClientAPI;
import Facturacion.Token;
import okhttp3.Request;

public class MainActivity extends PBase {

    private EditText txtUser, txtPass;
    private TextView lblRuta, lblRTit, lblLogin, lblVer, lblID;
    private ImageView imgLogo;

    private BaseDatosVersion dbVers;

    private boolean rutapos, scanning = false;
    //private String cs1, cs2, cs3, barcode;

    //Código con monto mínimo
    private final String parNumVer = "10.0.11 / ";
    private final String  parFechaVer = "15-05-2026";
    private final String parTipoVer = "ROAD PRD";

    //RUC Token00100833

    private Token token = new Token();
    private HttpClientAPI htclient;
    private Runnable rnToken;
    private Gson gson;
    private clsClasses.clsEmpresa Empresa = clsCls.new clsEmpresa();


    private static final String PREFS           = "ROAD_PREFS";
    private static final String KEY_MIGRATED_V1 = "db_migrated_v1";
    private static final String KEY_DB_SEEDED   = "db_seeded_v1";

    private static final int REQ_COMWS     = 1001;
    private static final int REQ_PERMS     = 100;   // único requestCode
    private static final int REQ_IMPORT_DB = 2001;

    private static boolean crashHandlerInstalled = false;
    private final java.util.concurrent.atomic.AtomicBoolean seedingInProgress =
            new java.util.concurrent.atomic.AtomicBoolean(false);
    private boolean sessionInited = false;
    private boolean pendingLaunchComWS = false;
    private boolean appReady = true;

    //region Activity lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        installCrashHandler(this);

        // 1) Pedir permisos si faltan
        if (!haveAllInitialPermissions()) {
            askInitialPermissions();
            return; // esperar onRequestPermissionsResult
        }

        // 2) Continuar flujo normal
        startApplication();
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();

            // 1) Conexión DB
            safeOpenDb();

            // 2) Si DB no lista => lanzar ComWS y salir
            if (!ensureSeededOrStartComWS()) {
                if (lblLogin != null) lblLogin.setVisibility(View.INVISIBLE);
                return;
            }

            // 3) DB lista => init de sesión una sola vez por ciclo
            if (!sessionInited) {
                initSession();
                sessionInited = true;
            }

            if (txtUser != null) txtUser.requestFocus();
            if (lblLogin != null) lblLogin.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (Con != null && Con.vDatabase != null && Con.vDatabase.isOpen()) {
                Con.vDatabase.close();
            }
        } catch (Exception ignore) {}
        setAddlog("onDestroy", "->", "");
    }

    @Override protected void onPause() { super.onPause(); setAddlog("onPause", "->", ""); }
    @Override protected void onStop()  { super.onStop();  setAddlog("onStop",  "->", ""); }

    //endregion

    //region Crash handler

    private static void installCrashHandler(Context appCtx) {
        if (crashHandlerInstalled) return;
        final Thread.UncaughtExceptionHandler prev = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                File base = appCtx.getExternalFilesDir(null);
                if (base == null) base = appCtx.getFilesDir(); // fallback interno
                File logsDir = new File(new File(base, "ROAD"), "Logs");
                if (!logsDir.exists()) logsDir.mkdirs();
                File logFile = new File(logsDir, "crash.txt");

                try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(logFile, true))) {
                    String ts = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            java.util.Locale.getDefault()).format(new java.util.Date());
                    pw.println(ts + " | Thread: " + t.getName());
                    e.printStackTrace(pw);
                    pw.println("----");
                }
            } catch (Exception ignored) {}

            if (prev != null) {
                prev.uncaughtException(t, e);
            } else {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(10);
            }
        });

        crashHandlerInstalled = true;
    }

    //endregion

    //region Permisos

    private boolean haveAllInitialPermissions() {
        boolean ok = true;

        ok &= (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
        ok &= (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED);
        ok &= (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED);
        ok &= (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ok &= (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ok &= (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    == PackageManager.PERMISSION_GRANTED);
            ok &= (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED);
        }else {
            ok &= (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                    == PackageManager.PERMISSION_GRANTED);
            ok &= (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                    == PackageManager.PERMISSION_GRANTED);
        }

        return ok;
    }

    private void askInitialPermissions() {
        ArrayList<String> toAsk = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) toAsk.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) toAsk.add(Manifest.permission.CALL_PHONE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) toAsk.add(Manifest.permission.READ_PHONE_STATE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) toAsk.add(Manifest.permission.CAMERA);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) toAsk.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) toAsk.add(Manifest.permission.BLUETOOTH_SCAN);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) toAsk.add(Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                    != PackageManager.PERMISSION_GRANTED) toAsk.add(Manifest.permission.BLUETOOTH);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                    != PackageManager.PERMISSION_GRANTED) toAsk.add(Manifest.permission.BLUETOOTH_ADMIN);
        }

        if (toAsk.isEmpty()) {
            startApplication();
            return;
        }

        logBeforeAsking();
        ActivityCompat.requestPermissions(this, toAsk.toArray(new String[0]), REQ_PERMS);
    }

    // 1) Helper para ver el estado real de permisos (útil con SOTI)
    private void logPerms(String from) {
        String[] perms;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        } else {
            perms = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
            };
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Permisos [").append(from).append("]\n");
        for (String p : perms) {
            int st = ContextCompat.checkSelfPermission(this, p);
            sb.append(" • ").append(p).append(" = ")
                    .append(st == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED")
                    .append('\n');
        }
        Log.d("PERMS", sb.toString());
    }

    // 2) (Opcional) atajo para nombre corto de permiso en mensajes
    private String shortPerm(String full) {
        int i = full.lastIndexOf('.');
        return (i >= 0 && i < full.length() - 1) ? full.substring(i + 1) : full;
    }

    // 3) Llama esto justo ANTES de pedir permisos (por ejemplo en askInitialPermissions)
    private void logBeforeAsking() {
        logPerms("before requestPermissions");
    }

    // 4) Tu callback, mejorado con logging y detalle de denegados
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_PERMS) {
            // Log global del estado real tras el callback
            logPerms("onRequestPermissionsResult");

            boolean allGranted = grantResults != null && grantResults.length > 0;
            if (allGranted) {
                for (int r : grantResults) {
                    if (r != PackageManager.PERMISSION_GRANTED) { allGranted = false; break; }
                }
            }

            if (allGranted) {
                startApplication();
            } else {
                // Construir lista de denegados y marcar cuáles son “permanentes” (no volver a preguntar / política MDM)
                ArrayList<String> denied = new ArrayList<>();
                ArrayList<String> deniedPermanent = new ArrayList<>();

                if (permissions != null && grantResults != null) {
                    for (int i = 0; i < Math.min(permissions.length, grantResults.length); i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            String p = permissions[i];
                            denied.add(shortPerm(p));

                            // Si retorna false, Android no mostrará más prompt. En MDM suele significar denegado por política.
                            boolean shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(this, p);
                            if (!shouldShow) deniedPermanent.add(shortPerm(p));
                        }
                    }
                }

                // Log en detalle
                Log.w("PERMS", "Denegados: " + denied + " | Permanentes/MDM: " + deniedPermanent);

                // Mensaje visible al usuario/soporte
                String msg;
                if (!deniedPermanent.isEmpty()) {
                    msg = "Faltan permisos: " + denied +
                            ". Algunos están denegados por política o 'No volver a preguntar': " + deniedPermanent +
                            ". Si el dispositivo está gestionado, pida a TI que habilite estos permisos.";
                } else {
                    msg = "Faltan permisos: " + denied + ". Otórguelos para continuar.";
                }
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

                // Si quieres, abre ajustes de la app cuando haya permanentes:
                // if (!deniedPermanent.isEmpty()) {
                //     Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                //             Uri.parse("package:" + getPackageName()));
                //     i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //     startActivity(i);
                // }

                pendingLaunchComWS = false;
            }
        }
    }

    //endregion

    //region DB boot / seed

    private void safeOpenDb() {
        if (Con == null) Con = new BaseDatos(this);
        if (Con.vDatabase == null || !Con.vDatabase.isOpen()) Con.open();
    }

    /** Devuelve true si ya hay datos; si no, lanza ComWS y devuelve false. */
    private boolean ensureSeededOrStartComWS() {
        if (!haveAllInitialPermissions()) {
            pendingLaunchComWS = true;
            askInitialPermissions();
            return false;
        }

        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean seeded = sp.getBoolean(KEY_DB_SEEDED, false);
        boolean vacia  = dbVacia();

        if (!seeded && !vacia) {
            sp.edit().putBoolean(KEY_DB_SEEDED, true).apply();
            setAddlog("ensureSeeded", "auto-fix seeded", "");
            return true;
        }

        if (!seeded || vacia) {
            if (seedingInProgress.compareAndSet(false, true)) {
                setAddlog("ensureSeeded", "launch ComWS", "");

                // Valores mínimos por si la UI los usa antes de seed
                gl.ruta = ""; gl.rutanom = ""; gl.vend = "0";
                gl.rutatipog = "V";
                gl.wsURL = "http://192.168.1.1/wsAndr/wsAndr.asmx";
                gl.permitir_cantidad_mayor = false;
                gl.permitir_producto_nuevo = false;
                gl.emp = "";
                if (lblRuta != null) lblRuta.setText("");
                gl.devol = false;
                gl.peEnvioParcial = false;
                gl.peModal = "-";
                gl.modoadmin = true;
                gl.autocom = 0;
                gl.deviceId   = ((appGlobals) getApplication()).deviceId;
                gl.devicename = ((appGlobals) getApplication()).devicename;

                toastcent("¡La base de datos está vacía! Cargando datos…\n¡No se pudo cargar configuración de la empresa!");

                lanzarComWS();
            }
            return false;
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    private void lanzarComWS() {
        Intent i = new Intent(this, ComWS.class);
        startActivityForResult(i, REQ_COMWS);
    }

    //endregion

    //region Arranque de app

    private void startApplication() {
        try {
            // A) Si faltan permisos, pedir y salir
            if (!haveAllInitialPermissions()) {
                askInitialPermissions();
                return;
            }

            // B) Init base (PBase)
            super.InitBase();

            setTitle("ROAD");
            gl.parNumVer   = parNumVer;
            gl.parFechaVer = parFechaVer;
            gl.parTipoVer  = parTipoVer;

            txtUser  = findViewById(R.id.txtUser);
            txtPass  = findViewById(R.id.txtMonto);
            lblRuta  = findViewById(R.id.lblCDisp);
            lblRTit  = findViewById(R.id.lblCUsed);
            lblLogin = findViewById(R.id.lblDir);
            lblVer   = findViewById(R.id.textView10);
            lblID    = findViewById(R.id.textView81);
            imgLogo  = findViewById(R.id.imgNext);

            lblVer.setText(gl.parTipoVer + " Version " + gl.parNumVer + gl.parFechaVer);

            // C) Flag debug (carpeta segura) + migración opcional desde legacy
/*            try {
                File debugFile = new File(AppPaths.road(this), "debug.txt");
                if (!debugFile.exists()) {
                    File legacy = new File("/sdcard/debug.txt");
                    if (legacy.exists() && legacy.canRead()) {
                        try (FileInputStream in = new FileInputStream(legacy);
                             FileOutputStream out = new FileOutputStream(debugFile)) {
                            byte[] buf = new byte[8192]; int n;
                            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                            out.getFD().sync();
                        } catch (Exception ignored) {}
                    }
                }
                gl.debug = debugFile.exists();
            } catch (Exception e) { gl.debug = false; }*/

            try {
                File debugFile = new File(AppPaths.road(this), "debug.txt");

                if (!debugFile.exists()) {
                    File legacy = new File(Environment.getExternalStorageDirectory(), "debug.txt");

                    if (legacy.exists()) {
                        try (FileInputStream in = new FileInputStream(legacy);
                             FileOutputStream out = new FileOutputStream(debugFile)) {

                            byte[] buf = new byte[8192];
                            int n;

                            while ((n = in.read(buf)) != -1) {
                                out.write(buf, 0, n);
                            }

                            out.flush();
                            out.getFD().sync();

                        } catch (Exception ex) {
                            Log.e("DEBUG_FILE", "No se pudo copiar debug.txt", ex);
                        }
                    }
                }

                gl.debug = debugFile.exists();

            } catch (Exception e) {
                gl.debug = false;
                Log.e("DEBUG_FILE", "Error general", e);
            }

            // D) Migraciones una vez
            SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
            if (!sp.getBoolean(KEY_MIGRATED_V1, false)) {
                BaseDatos.ensureMigration(this);
                BaseDatos.migrateLegacyPrintDocs(this);
                sp.edit().putBoolean(KEY_MIGRATED_V1, true).apply();
            }

            // E) Abrir DB y decidir seed
            safeOpenDb();
            if (!ensureSeededOrStartComWS()) {
                return; // ComWS lanzado, esperar resultado
            }

            // F) DB lista: sesión, licencia, etc.
            initSession();
            sessionInited = true;

            if (!validaLicencia()) {
                startActivity(new Intent(this, comWSLic.class));
                return;
            } else {
                supervisorRuta();
            }

            // G) Resto
            setHandlers();
            if (txtUser != null) txtUser.requestFocus();
            gl.tolsuper = false;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                gl.PathDataDir = getApplicationContext().getDataDir().getPath();
            }

            String fname = gl.PathDataDir + "/" + gl.archivo_p12;
            File cert = new File(fname);
            if (!cert.exists()) {
                CopiarArchivo(gl.archivo_p12);
            }

            if (gl.debug) {
                txtUser.setText("00110698");
                txtPass.setText("inicio01");
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }
    }

    //endregion

    //region Importar BD (Downloads -> app)

    public void onClickImportarDb(View v) {
        msgAskRestaurarBaseDatos("¿Restaurar base de datos?");
    }

    private void msgAskRestaurarBaseDatos(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle(R.string.app_name);
        dialog.setMessage(msg);
        dialog.setIcon(R.drawable.ic_quest);

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                entraRestaurarBaseDatos();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        dialog.show();
    }

    private void entraRestaurarBaseDatos() {

        try {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Contraseña de administrador");//	alert.setMessage("Serial");

            final EditText input = new EditText(this);
            alert.setView(input);

            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            input.setText("");
            input.requestFocus();

            alert.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String s;

                    try {
                        s = input.getText().toString();

                        if (s.equalsIgnoreCase("1965")) {
                            iniciarImportacionDb();
                        } else {
                            mu.msgbox("Contraseña incorrecta");
                            return;
                        }

                    } catch (Exception e) {
                        addlog(new Object() {
                        }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
                    }
                }
            });

            alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            alert.show();
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }


    }

    private void iniciarImportacionDb() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "application/octet-stream",
                "application/x-sqlite3",
                "application/vnd.sqlite3",
                "application/db",
                "application/x-db",
                "application/x-sqlite"
        });
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                i.putExtra("android.provider.extra.INITIAL_URI",
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI);
            }
        } catch (Exception ignored) {}
        startActivityForResult(i, REQ_IMPORT_DB);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_COMWS) {
            if (resultCode == RESULT_OK) {
                getSharedPreferences(PREFS, MODE_PRIVATE)
                        .edit().putBoolean(KEY_DB_SEEDED, true).apply();

                safeOpenDb();
                initSession();
                sessionInited = true;

                gl.modoadmin = false;
                if (txtUser != null) txtUser.setText("");
                if (txtPass != null) txtPass.setText("");
                if (lblLogin != null) lblLogin.setVisibility(View.VISIBLE);
                if (txtUser != null) txtUser.requestFocus();
                toastcent("Datos cargados. Inicie sesión.");
            } else if (data != null && data.hasExtra("error_msg")) {
                msgbox(data.getStringExtra("error_msg"));
            }
            seedingInProgress.set(false);

        } else if (requestCode == REQ_IMPORT_DB) {
            if (resultCode != RESULT_OK || data == null || data.getData() == null) {
                toast("Importación cancelada.");
                return;
            }
            Uri uri = data.getData();

            final int takeFlags = data.getFlags() &
                    (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception ignored) {}

            boolean ok = importarDbDesdeUri(uri);
            if (ok) {
                getSharedPreferences(PREFS, MODE_PRIVATE)
                        .edit().putBoolean(KEY_DB_SEEDED, true).apply();

                safeOpenDb();
                initSession();
                sessionInited = true;

                gl.modoadmin = false;
                if (txtUser != null) txtUser.setText("");
                if (txtPass != null) txtPass.setText("");
                if (lblLogin != null) lblLogin.setVisibility(View.VISIBLE);
                if (txtUser != null) txtUser.requestFocus();
                toastcent("Base de datos importada. Inicie sesión.");
            } else {
                msgbox("No se pudo importar la base de datos.");
            }
        }
    }

    private boolean importarDbDesdeUri(Uri srcUri) {
        File destDb = getDatabasePath("road.db");
        File wal = new File(destDb.getPath() + "-wal");
        File shm = new File(destDb.getPath() + "-shm");

        cerrarDbParaReemplazo();

        // 1) Copiar a tmp
        File tmp = new File(getCacheDir(), "import_tmp.db");
        try (InputStream in = getContentResolver().openInputStream(srcUri);
             OutputStream out = new FileOutputStream(tmp)) {
            if (in == null) return false;
            byte[] buf = new byte[8192]; int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            out.flush();
        } catch (Exception e) {
            return false;
        }

        // 2) Validar cabecera SQLite
        if (!esProbableSQLite(tmp)) { tmp.delete(); return false; }

        // 3) Backup actual
        try {
            File backupsDir = new File(getExternalFilesDir(null), "ROAD/Backups");
            if (!backupsDir.exists()) backupsDir.mkdirs();
            if (destDb.exists()) {
                File backup = new File(backupsDir, "road_backup_" +
                        new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".db");
                copyFile(destDb, backup);
            }
        } catch (Exception ignored) {}

        // 4) Limpiar sidecars y actual
        try { if (wal.exists()) wal.delete(); } catch (Exception ignored) {}
        try { if (shm.exists()) shm.delete(); } catch (Exception ignored) {}
        try { if (destDb.exists()) destDb.delete(); } catch (Exception ignored) {}

        // 5) Reemplazar
        boolean ok = tmp.renameTo(destDb);
        if (!ok) {
            try { copyFile(tmp, destDb); ok = true; } catch (Exception ignored) {}
            finally { tmp.delete(); }
        }
        if (!ok) return false;

        // 6) Reabrir
        try { reabrirDbDespuesDeReemplazo(); } catch (Exception e) { return false; }

        return true;
    }

    private void cerrarDbParaReemplazo() {
        try {
            if (Con != null && Con.vDatabase != null && Con.vDatabase.isOpen()) Con.vDatabase.close();
        } catch (Exception ignored) {}
        try {
            if (db != null && db.isOpen()) db.close();
        } catch (Exception ignored) {}
    }

    private void reabrirDbDespuesDeReemplazo() {
        try {
            if (Con == null) Con = new BaseDatos(this);
            Con.open();
        } catch (Exception e) {
            msgbox("BD importada pero no se pudo abrir: " + e.getMessage());
        }
    }

    private boolean esProbableSQLite(File f) {
        if (f == null || !f.exists() || f.length() < 100) return false;
        try (FileInputStream in = new FileInputStream(f)) {
            byte[] hdr = new byte[16];
            int n = in.read(hdr);
            if (n < 16) return false;
            String h = new String(hdr, java.nio.charset.StandardCharsets.US_ASCII);
            return h.startsWith("SQLite format 3");
        } catch (Exception e) {
            return false;
        }
    }

    private void copyFile(File src, File dst) throws IOException {
        try (FileInputStream in = new FileInputStream(src);
             FileOutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            out.getFD().sync();
        }
    }

    //endregion

    //region Init sesión + parámetros

    private void initSession() {
        Cursor DT;
        String s, vCellCom = "";

        // Seguridad: si la DB está vacía, salir (el flujo de seed lo maneja ensureSeededOrStartComWS)
        if (dbVacia()) return;

        try {
            sql = "SELECT CODIGO,NOMBRE,VENDEDOR,VENTA,WLFOLD,IMPRESION,SUCURSAL,CELULAR," +
                    "PERMITIR_PRODUCTO_NUEVO, PERMITIR_CANTIDAD_MAYOR, VALIDAR_POSICION_GEOREFERENCIAL," +
                    "IMPORTA_CLIENTES_EN_RUTA, RUTA_RECOLECTORA " +
                    "FROM P_RUTA";
            DT = Con.OpenDT(sql);

            if (DT != null && DT.getCount() > 0) {
                DT.moveToFirst();

                gl.ruta = DT.getString(0);
                gl.rutanom = DT.getString(1);
                gl.vend = DT.getString(2);
                gl.rutatipog = DT.getString(3);
                s = DT.getString(3);
                gl.wsURL = DT.getString(4);
                gl.impresora = DT.getString(5);
                gl.sucur = DT.getString(6);

                if (!mu.emptystr(DT.getString(7))) vCellCom = DT.getString(7);
                gl.CellCom = vCellCom.equalsIgnoreCase("S");

                rutapos = s.equalsIgnoreCase("R");
                gl.permitir_cantidad_mayor  = DT.getInt(8) == 1;
                gl.permitir_producto_nuevo  = DT.getInt(9) == 1;
                gl.validar_posicion_georef  = DT.getInt(10) == 1;
                gl.importa_clientes_en_ruta = DT.getInt(11)==1;
                gl.ruta_recolectora = DT.getInt(12)==1;
            } else {
                gl.ruta = ""; gl.rutanom = ""; gl.vend = "0";
                gl.rutatipog = "V";
                gl.wsURL = "http://192.168.1.1/wsAndr/wsAndr.asmx";
                gl.permitir_cantidad_mayor = false;
                gl.permitir_producto_nuevo = false;
                gl.importa_clientes_en_ruta = false;
                gl.ruta_recolectora=false;
            }
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
        }

        if (rutapos) {
            lblRTit.setText("POS No. " + gl.ruta);
            imgLogo.setImageResource(R.drawable.retail_logo);
        } else {
            lblRTit.setText("Ruta No. " + gl.ruta);
        }

        try {
            sql = " SELECT EMPRESA,NOMBRE,DEVOLUCION_MERCANCIA,USARPESO,FIN_DIA,DEPOSITO_PARCIAL,UNIDAD_MEDIDA_PESO," +
                    " INCIDENCIA_NO_LECTURA, LOTE_POR_DEFECTO, URL_TOKEN, USUARIO_API, CLAVE_API, URL_EMISION_NC_B2C," +
                    " URL_EMISION_ND_B2C,URL_EMISION_FACTURA_B2C,QR_API,URL_BASE, ARCHIVO_P12,URL_B2C_HH,QR_CLAVE, URL_DOC, " +
                    " URL_EMISION_NC_B2B_HH, URL_EMISION_ND_B2B_HH, UNIDAD_MEDIDA_DEFECTO, AMBIENTE, URL_CONSULTAR_DOCUMENTO_POR_RUTA," +
                    " URL_LOTE_RUC_DV FROM P_EMPRESA ";
            DT = Con.OpenDT(sql);

            if (DT != null && DT.getCount() > 0) {
                DT.moveToFirst();
                gl.emp = DT.getString(0);
                lblRuta.setText(DT.getString(1));
                gl.empnom = DT.getString(1);
                gl.devol = DT.getInt(2) == 1;
                s = DT.getString(3);
                gl.usarpeso = s.equalsIgnoreCase("S");
                gl.banderafindia = DT.getInt(4) == 1;
                gl.umpeso = DT.getString(6);
                gl.incNoLectura = DT.getInt(7) == 1;
                gl.depparc = DT.getInt(5) == 1;
                gl.lotedf = DT.getString(8);

                gl.url_token = DT.getString(9);
                gl.usuario_api = DT.getString(10);
                gl.clave_api = DT.getString(11);
                gl.url_emision_nc_b2c = DT.getString(12);
                gl.url_emision_nd_b2c = DT.getString(13);
                gl.url_emision_factura_b2c = DT.getString(14);
                gl.qr_api = DT.getString(15);
                gl.url_base = DT.getString(16);
                gl.archivo_p12 = DT.getString(17);
                gl.url_b2c_hh = DT.getString(18);
                gl.qr_clave = DT.getString(19);
                gl.url_doc = DT.getString(20);
                gl.url_emision_nc_b2b_hh = DT.getString(21);
                gl.url_emision_nd_b2b_hh = DT.getString(22);
                gl.unidad_medida_defecto = DT.getString(23);
                gl.ambiente = DT.getString(24);
                gl.url_consultar_documento_por_ruta = DT.getString(25);
                gl.url_lote_ruc_dv = DT.getString(26);
            } else {
                gl.emp = "";
                lblRuta.setText("");
                gl.devol = false;
                msgbox("¡No se pudo cargar configuración de la empresa!");
            }
            if (DT != null) DT.close();

        } catch (Exception e) {
            msgbox("¡No se pudo cargar configuración de la empresa!");
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
        }

        gl.vendnom = "Vendedor 1";

        // Asegura carpetas (crea si no existen)
        AppPaths.syncFold(this);
        AppPaths.roadFotos(this);
        AppPaths.roadFotosCliNue(this);
        AppPaths.roadFotosCliDocs(this);
        AppPaths.roadPedidos(this);
        AppPaths.printDir(this);

        // Dispositivo
        gl.deviceId   = ((appGlobals) getApplication()).deviceId;
        gl.devicename = ((appGlobals) getApplication()).devicename;
        lblID.setText(gl.devicename);

        try {
            AppMethods app = new AppMethods(this, gl, Con, db);
            app.parametrosExtra();
            app.parametrosGlobales();
            app.parametrosBarras();
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox(e.getMessage());
        }

        try {
            htclient = new HttpClientAPI();
            gson = new com.google.gson.Gson();
            rnToken = this::cbToken;
            getHttpToken();
        } catch (Exception e) {
            msgbox(e.getMessage());
        }
    }

    private boolean dbVacia() {
        Cursor c = null;
        try {
            // 1) ¿Existe la tabla?
            c = Con.OpenDT("SELECT name FROM sqlite_master WHERE type='table' AND name='P_RUTA'");
            boolean existe = (c != null && c.moveToFirst());
            if (c != null) c.close();

            if (!existe) return true;

            // 2) ¿Tiene al menos una fila?
            c = Con.OpenDT("SELECT 1 FROM P_RUTA LIMIT 1");
            return (c == null) || !c.moveToFirst();

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(), e.getMessage(), "sqlite_master / EXISTS");
            return true;
        } finally {
            if (c != null) try { c.close(); } catch (Exception ignored) {}
        }
    }

    //endregion

    //region Login / navegación

    public void comMan(View view) {
        try {
            entraComunicacion();
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }
    }

    public void gotoMenu() {
        try {
            txtUser.setText("");
            txtPass.setText("");
            txtUser.requestFocus();

            Intent intent = new Intent(this, Menu.class);
            startActivity(intent);
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }
    }

    public void doLogin(View view) {
        try {
            if (!appReady) { toast("Inicializando, por favor espera…"); return; }
            if (gl == null) { try { super.InitBase(); } catch (Exception ignored) {} }
            if (!validaLicencia()) { startActivity(new Intent(this, comWSLic.class)); return; }
            processLogIn();
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }
    }

    private void setHandlers() {
        try {
            txtUser.setOnKeyListener((arg0, keyCode, arg2) -> {
                if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        txtPass.requestFocus();
                        return true;
                    }
                }
                return false;
            });

            txtPass.setOnKeyListener((arg0, keyCode, arg2) -> {
                if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        processLogIn();
                        return true;
                    }
                }
                return false;
            });
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }
    }

    private void processLogIn() {
        if (lblLogin != null) lblLogin.setVisibility(View.INVISIBLE);

        if (!validaLicencia()) {
            if (lblLogin != null) lblLogin.setVisibility(View.VISIBLE);
            mu.msgbox("¡Licencia invalida!");
            startActivity(new Intent(this, comWSLic.class));
            return;
        }

        if (checkUser()) gotoMenu();
        else if (lblLogin != null) lblLogin.setVisibility(View.VISIBLE);
    }

    private boolean checkUser() {
        Cursor DT;
        String usr, pwd, dpwd;

        try {
            usr = txtUser.getText().toString().trim();
            pwd = txtPass.getText().toString().trim();

            if (mu.emptystr(usr)) { mu.msgbox("Usuario incorrecto."); return false; }
            if (mu.emptystr(pwd)) { mu.msgbox("Contraseña incorrecta."); return false; }

            if (usr.equalsIgnoreCase("DTS") && pwd.equalsIgnoreCase("DTS")) {
                gl.vendnom = "DTS";
                gl.vend = "DTS";
                gl.vnivel = 1;
                gl.vnivprec = 1;
                return true;
            }

            if (usr.equalsIgnoreCase("Venta") && pwd.equalsIgnoreCase("Venta")) {
                showDemoMenu();
                return false;
            }

            sql = "SELECT NOMBRE,CLAVE,NIVEL,NIVELPRECIO FROM P_VENDEDOR WHERE CODIGO='" + usr + "'";
            DT = Con.OpenDT(sql);
            if (DT.getCount() == 0) { mu.msgbox("Usuario incorrecto !"); return false; }

            DT.moveToFirst();
            dpwd = DT.getString(1);
            if (!pwd.equalsIgnoreCase(dpwd)) { mu.msgbox("Contraseña incorrecta !"); return false; }

            gl.vendnom = DT.getString(0);
            gl.vend = usr;
            gl.vnivel = DT.getInt(2);
            gl.vnivprec = DT.getInt(3);

            gl.tolsuper = false;
            if (gl.peModal.equalsIgnoreCase("TOL")) {
                if (gl.vnivel == 2) gl.tolsuper = true;
            }

            return true;

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            return false;
        }
    }

    public void supervisorRuta() {
        Cursor DT;
        try {
            sql = "SELECT CODIGO FROM P_VENDEDOR WHERE RUTA = '" + gl.ruta + "' AND NIVEL = 2";
            DT = Con.OpenDT(sql);
            DT.moveToFirst();

            if (DT.getCount() > 0) gl.codSupervisor = DT.getString(0);
            else gl.codSupervisor = "";

            if (DT != null) DT.close();

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            Log.d("supervisorRuta error: ", e.getMessage());
        }
    }

    //endregion

    //region Demo / utilidades

    public void showDemoMenu() {
        try {
            final AlertDialog Dialog;
            final String[] selitems = {"Datos de cliente", "Base de datos original", "Borrar datos de venta"};

            AlertDialog.Builder menudlg = new AlertDialog.Builder(this);
            menudlg.setTitle("Datos de demo");

            menudlg.setItems(selitems, (dialog, item) -> {
                switch (item) {
                    case 0:
                        startActivity(new Intent(MainActivity.this, DemoData.class));
                        break;
                    case 1:
                        copyRawFile();
                        break;
                    case 2:
                        borrarDatos(1);
                        break;
                }
                dialog.cancel();
            });

            menudlg.setNegativeButton("Salir", (dialog, which) -> dialog.cancel());
            Dialog = menudlg.create();
            Dialog.show();
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }
    }

    private void copyRawFile() {
        String fn;
        int rid, rslt;
        try {
            Field[] fields = R.raw.class.getFields();
            for (Field f : fields) try {
                fn = f.getName();
                if (fn.equalsIgnoreCase("rd_param")) {
                    rid = f.getInt(null);
                    rslt = copyRawFile(rid);
                    if (rslt == 1) startActivity(new Intent(this, ComDrop.class));
                }
            } catch (Exception e) {
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            }
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }
    }

    private int copyRawFile(int rawid) {
        try {
            InputStream in = getResources().openRawResource(rawid);
            File file = new File(AppPaths.syncFold(this), "rd_param.txt");
            FileOutputStream out = new FileOutputStream(file);

            byte[] buff = new byte[1024];
            int read;
            try {
                while ((read = in.read(buff)) > 0) {
                    out.write(buff, 0, read);
                }
            } finally {
                in.close();
                out.close();
            }
            return 1;
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            mu.msgbox("Error : " + e.getMessage());
            return 0;
        }
    }

    private void borrarDatos(int showmsg) {
        try {
            db.beginTransaction();

            sql = "DELETE FROM D_FACTURA"; db.execSQL(sql);
            sql = "DELETE FROM D_FACTURAD"; db.execSQL(sql);
            sql = "DELETE FROM D_FACTURAP"; db.execSQL(sql);
            sql = "DELETE FROM D_FACTURAD_LOTES"; db.execSQL(sql);
            sql = "DELETE FROM D_FACTURA_BARRA"; db.execSQL(sql);
            sql = "DELETE FROM D_FACTURA_STOCK"; db.execSQL(sql);
            sql = "DELETE FROM D_FACTURAF"; db.execSQL(sql);
            sql = "DELETE FROM D_FACTURAD_MODIF"; db.execSQL(sql);

            sql = "DELETE FROM D_PEDIDO"; db.execSQL(sql);
            sql = "DELETE FROM D_PEDIDOD"; db.execSQL(sql);

            sql = "DELETE FROM D_BONIF"; db.execSQL(sql);
            sql = "DELETE FROM D_BONIF_LOTES"; db.execSQL(sql);
            sql = "DELETE FROM D_REL_PROD_BON"; db.execSQL(sql);
            sql = "DELETE FROM D_BONIFFALT"; db.execSQL(sql);

            sql = "DELETE FROM D_DEPOS"; db.execSQL(sql);
            sql = "DELETE FROM D_DEPOSD"; db.execSQL(sql);

            sql = "DELETE FROM D_MOV"; db.execSQL(sql);
            sql = "DELETE FROM D_MOVD"; db.execSQL(sql);

            sql = "DELETE FROM D_ATENCION"; db.execSQL(sql);

            sql = "DELETE FROM D_CANASTAS"; db.execSQL(sql);

            sql = "DELETE FROM D_CLIENTE_MODIF"; db.execSQL(sql);

            sql = "DELETE FROM D_CLINUEVOT"; db.execSQL(sql);

            sql = "DELETE FROM D_RATING"; db.execSQL(sql);

            sql = "DELETE FROM D_DESPACHOD_NO_ENTREGADO"; db.execSQL(sql);

            db.setTransactionSuccessful();
            db.endTransaction();

            if (showmsg == 1) Toast.makeText(this, "Datos de venta borrados", Toast.LENGTH_SHORT).show();

        } catch (SQLException e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            db.endTransaction();
            mu.msgbox("Error : " + e.getMessage());
        }
    }

    //endregion

    //region WS Token

    private void getHttpToken() {
        try {
            gl.RUC_token = "";
            getDatosEmpresa();

            if (mu.emptystr(gl.url_base)) return;

            String base = Empresa.usuarioApi + ":" + Empresa.claveApi;
            String credenciales = "Basic " + encodeToString(base.getBytes(), NO_WRAP);

            Request request = new Request.Builder()
                    .url(gl.url_base + "Autenticacion/Api/ServicioEDOC?Id=3")
                    .get()
                    .addHeader("Accept", "*/*")
                    .addHeader("Accept-Encoding", "gzip,deflate,br")
                    .addHeader("Authorization", credenciales)
                    .build();

            htclient.makeGetRequest(request, rnToken);

        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }
    }

    private void cbToken() {
        try {
            if (htclient.retcode != 1) {
                toast("Error: " + htclient.data);
                return;
            }

            String rs = htclient.data;
            try {
                token = gson.fromJson(rs, Token.class);
                gl.RUC_token = token.getToken();
            } catch (JsonSyntaxException e) {
                msgbox(new Object(){}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
            }
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }
    }

    private void getDatosEmpresa() {
        Cursor DT;
        try {
            sql = "SELECT URL_AUTENTICACION, URL_ANULACION, USUARIO_API, CLAVE_API FROM P_EMPRESA";
            DT = Con.OpenDT(sql);
            DT.moveToFirst();

            if (DT.getCount() > 0) {
                Empresa.urlToken    = DT.getString(0);
                Empresa.urlAnulacion= DT.getString(1);
                Empresa.usuarioApi  = DT.getString(2);
                Empresa.claveApi    = DT.getString(3);
            }

            if (DT != null) DT.close();

        } catch (Exception e) {
            mu.msgbox(new Object(){}.getClass().getEnclosingMethod().getName() + " - " + e.getMessage());
        }
    }

    //endregion

    //region Varios

    private void entraComunicacion() {

        try {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Contraseña de administrador");//	alert.setMessage("Serial");

            final EditText input = new EditText(this);
            alert.setView(input);

            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            input.setText("");
            input.requestFocus();

            alert.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String s;

                    try {
                        s = input.getText().toString();

                        if (s.equalsIgnoreCase("1965")) {
                            gl.modoadmin = true;
                            gl.autocom = 0;
                            startActivity(new Intent(MainActivity.this, ComWS.class));
                        } else {
                            mu.msgbox("Contraseña incorrecta");
                            return;
                        }

                    } catch (Exception e) {
                        addlog(new Object() {
                        }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
                    }
                }
            });

            alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            alert.show();
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

    }

    private void CopiarArchivo(String filename) {
        AssetManager assetManager = getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            File outFile = new File(gl.PathDataDir, filename);
            out = new FileOutputStream(outFile);

            GeneraArchivo(in, out);

            Toast.makeText(this, "Certificado guardado!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "No se pudo copiar el certificado!", Toast.LENGTH_SHORT).show();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void GeneraArchivo(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (Exception e) {
            Toast.makeText(this, e + "", Toast.LENGTH_LONG).show();
        }
    }

    public void doRegister(View view) {
        try {
            startActivity(new Intent(this, comWSLic.class));
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }
    }

    private void compareSC(CharSequence s) {
        try {
            String os, bc;

            bc = txtUser.getText().toString();
            if (mu.emptystr(bc) || bc.length() < 2) {
                txtUser.setText("");
                scanning = false;
                return;
            }
            os = s.toString();

            if (bc.equalsIgnoreCase(os)) {
                //Toast.makeText(this,"Codigo barra : " +bc, Toast.LENGTH_SHORT).show();
                msgbox("Barra: " + bc);
            }

            txtUser.setText("");
            scanning = false;
        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

    }

    private static void copyDir(File src, File dst) {
        if (src == null || !src.exists() || !src.isDirectory()) return;

        if (!dst.exists()) dst.mkdirs();
        File[] list = src.listFiles();
        if (list == null) return;

        for (File f : list) {
            File t = new File(dst, f.getName());
            if (f.isDirectory()) {
                copyDir(f, t);
            } else {
                try (FileInputStream in = new FileInputStream(f);
                     FileOutputStream out = new FileOutputStream(t)) {

                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) > 0) {
                        out.write(buf, 0, n);   // <-- sin "off:"
                    }
                    out.getFD().sync();
                } catch (Exception ignored) {}
            }
        }
    }

    private boolean validaLicencia() {

        CryptUtil cu = new CryptUtil();
        Cursor dt;
        String lic, lickey, licruta, rutaencrypt;
        Integer msgLic = 0;

        if (gl.debug) return true;

        try {

            lickey = cu.encrypt(gl.deviceId);
            rutaencrypt = cu.encrypt(gl.ruta);

            sql = "SELECT lic, licparam FROM Params";
            dt = Con.OpenDT(sql);
            dt.moveToFirst();
            lic = dt.getString(0);
            licruta = dt.getString(1);

            if (!gl.debug) {

                if (mu.emptystr(lic)) {
                    toastlong("El dispositivo no tiene licencia válida de handheld");
                    return false;
                }

                if (mu.emptystr(licruta)) {
                    toastlong("El dispositivo no tiene licencia válida de ruta");
                    return false;
                }

                if (lic.equalsIgnoreCase(lickey) && licruta.equalsIgnoreCase(rutaencrypt)) {
                    return true;
                }

                if (!lic.equalsIgnoreCase(lickey) && !licruta.equalsIgnoreCase(rutaencrypt)) {
                    toastlong("El dispositivo no tiene licencia válida de handheld, ni de ruta");
                    return false;
                }

                if (!lic.equalsIgnoreCase(lickey)) {
                    toastlong("El dispositivo no tiene licencia válida de handheld");
                    return false;
                }

                if (!licruta.equalsIgnoreCase(rutaencrypt)) {
                    toastlong("El dispositivo no tiene licencia válida de ruta");
                    return false;
                }

            } else {
                return true;
            }

        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            mu.msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " : " + e.getMessage());
        }

        return false;

    }

    //endregion
}



