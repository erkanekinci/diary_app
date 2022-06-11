package com.example.diaryapp.entities;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

import java.io.Serializable;



@Entity (tableName = "anilar")
public class Ani implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ColumnInfo(name = "baslik")
    private String baslik;


    @ColumnInfo(name = "tarih")
    private String tarih;

    @ColumnInfo(name = "mood")
    private String mood;

    @ColumnInfo(name = "ani")
    private String ani;

    @ColumnInfo(name = "resim")
    private String resim;

    @ColumnInfo(name = "renk")
    private String renk;

    public String getRenk() {
        return renk;
    }

    public void setRenk(String imagePath) {
        this.renk = imagePath;
    }

    public String getBaslik() {
        return baslik;
    }

    public void setBaslik(String baslik) {
        this.baslik = baslik;
    }

    public String getResim() {
        return resim;
    }

    public void setResim(String resim) {
        this.resim = resim;
    }


    public String getTarih() {
        return tarih;
    }

    public void setTarih(String tarih) {
        this.tarih = tarih;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getAni() {
        return ani;
    }

    public void setAni(String ani) {
        this.ani = ani;
    }

    @NonNull
    @Override
    public String toString() {
        return baslik + " : " + tarih ;
    }
}
