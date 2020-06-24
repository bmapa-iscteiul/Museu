package com.example.sid2020.APP.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 204;
    public static final String DATABASE_NAME = "SID.db";
    DatabaseConfig config = new DatabaseConfig();

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(config.SQL_CREATE_DROP_MEDICOES_IFEXISTS);
        sqLiteDatabase.execSQL(config.SQL_CREATE_MEDICOESTEMPERATURA);
        sqLiteDatabase.execSQL(config.SQL_CREATE_DROP_ALERTA_IFEXISTS);
        sqLiteDatabase.execSQL(config.SQL_CREATE_ALERTASGLOBAIS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(config.SQL_CREATE_DROP_MEDICOES_IFEXISTS);
        db.execSQL(config.SQL_CREATE_MEDICOESTEMPERATURA);
        db.execSQL(config.SQL_CREATE_DROP_ALERTA_IFEXISTS);
        db.execSQL(config.SQL_CREATE_ALERTASGLOBAIS);
    }

    public void insert_MedicaoTemperatura(String dataHoraMedicao, double temperatura, int controlo) {
        ContentValues values = new ContentValues();
        values.put(DatabaseConfig.MedicoesTemperatura.COLUMN_NAME_DATAHORAMEDICAO,dataHoraMedicao);
        values.put(DatabaseConfig.MedicoesTemperatura.COLUMN_NAME_VALOR_MEDICAO,temperatura);
        values.put(DatabaseConfig.MedicoesTemperatura.COLUMN_NAME_CONTROLO,controlo);
        getWritableDatabase().insert(DatabaseConfig.MedicoesTemperatura.TABLE_NAME,null,values);
    }

    public void insert_alertaGlobal(String dataHoraMedicao, String tipoSensor, String valorMedicao, double limite, String descricao, int controlo) {
        ContentValues values = new ContentValues();
        values.put(DatabaseConfig.AlertasGlobais.COLUMN_NAME_DATAHORAMEDICAO,dataHoraMedicao);
        values.put(DatabaseConfig.AlertasGlobais.COLUMN_NAME_TIPO_SENSOR,tipoSensor);
        values.put(DatabaseConfig.AlertasGlobais.COLUMN_NAME_VALOR_MEDICAO,valorMedicao);
        values.put(DatabaseConfig.AlertasGlobais.COLUMN_NAME_LIMITE,limite);
        values.put(DatabaseConfig.AlertasGlobais.COLUMN_NAME_DESCRICAO,descricao);
        values.put(DatabaseConfig.AlertasGlobais.COLUMN_NAME_CONTROLO,controlo);
        getWritableDatabase().insert(DatabaseConfig.AlertasGlobais.TABLE_NAME,null,values);
    }

    public void clearAlertasGlobais() {
        getWritableDatabase().execSQL(config.SQL_DELETE_ALERTASGLOBAIS_DATA);
    }

    public void clearMedicoes() {
        getWritableDatabase().execSQL(config.SQL_DELETE_MEDICOESTEMPERATURA_DATA);
    }

}
