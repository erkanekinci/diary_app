package com.example.diaryapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diaryapp.R;
import com.example.diaryapp.database.AnilarDB;
import com.example.diaryapp.entities.Ani;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.acl.LastOwnerException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AniOlusturma extends AppCompatActivity {

    private EditText inputNotBaslik,inputMood,inputAni;
    private TextView textTarih;
    private ImageView imageAni;
    private TextView textSifreAni;
    private LinearLayout layoutSifre;
    private Bitmap bitmap;
    private String resim;
    private ScrollView linear;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;


    private AlertDialog dialogDeleteAni;
    private AlertDialog dialogSifre;
    private AlertDialog dialogCheckSifre;
    private Ani alreadyExistsAni;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ani_olusturma);


        ImageView imageGeri = findViewById(R.id.imageGeri);
        imageGeri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        inputNotBaslik = findViewById(R.id.inputNotBaslik);
        inputMood = findViewById(R.id.inputMood);
        inputAni = findViewById(R.id.inputAni);
        textTarih = findViewById(R.id.textTarih);
        imageAni = findViewById(R.id.imageAni);
        textSifreAni = findViewById(R.id.textSifreAni);
        layoutSifre = findViewById(R.id.layoutSifre);

        textTarih.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", new Locale("tr").getDefault()).format(new Date())
        );


        ImageView imageKaydet = findViewById(R.id.imageKaydet);
        imageKaydet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aniKaydet();
            }
        });

        resim = "";

        if(getIntent().getBooleanExtra("isViewOrUpdate" , false)){
            alreadyExistsAni = (Ani) getIntent().getSerializableExtra("ani");
            if(alreadyExistsAni.getRenk()!=null && !alreadyExistsAni.getRenk().trim().isEmpty()) {
                showSifreCheckDialog(alreadyExistsAni);
            }
            setViewOrUpdateAni();

        }


        findViewById(R.id.imageŞifreEkle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSifreDialog();
            }
        });



        findViewById(R.id.imageMedyaEkle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            AniOlusturma.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION

                    );
                }else{
                    selectImage();
                }
            }
        });

        if(alreadyExistsAni != null){
            findViewById(R.id.deleteAni).setVisibility(View.VISIBLE);
            findViewById(R.id.pdfAni).setVisibility(View.VISIBLE);
            findViewById(R.id.deleteAni).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDeleteAniDialog();

                }
            });

            findViewById(R.id.pdfAni).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    linear = findViewById(R.id.scrollView2);
                    bitmap = LoadBitmap(linear, linear.getWidth(), linear.getHeight());
                    createPdf();
                }
            });

        }

    }

    private void showSifreDialog(){
        if(dialogSifre == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(AniOlusturma.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_lock,
                    findViewById(R.id.layoutLock)
            );
            builder.setView(view);
            dialogSifre = builder.create();
            if(dialogSifre.getWindow()  != null){
                dialogSifre.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final EditText inputSifre = view.findViewById(R.id.inputSifre);
            inputSifre.requestFocus();

            view.findViewById(R.id.textKaydetSifre).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    textSifreAni.setText(inputSifre.getText().toString());
                    layoutSifre.setVisibility(View.VISIBLE);
                    dialogSifre.dismiss();
                }
            });

            view.findViewById(R.id.textVazgeç).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogSifre.dismiss();
                }
            });

            dialogSifre.show();
        }

    }

    private Bitmap LoadBitmap(View v, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);

        return bitmap;
    }

    private void createPdf() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        float hight = displaymetrics.heightPixels;
        float width = displaymetrics.widthPixels;

        int convertHighet = (int) hight, convertWidth = (int) width;

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(convertWidth, convertHighet, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        canvas.drawPaint(paint);
        bitmap = Bitmap.createScaledBitmap(bitmap, convertWidth, convertHighet, true);

        canvas.drawBitmap(bitmap, 0, 0, paint);
        document.finishPage(page);


        String targetPdf =  "/storage/emulated/0/Download/page.pdf";
        Log.d("PATH",targetPdf);
        File filePath;
        filePath = new File(targetPdf);
        try {

            document.writeTo(new FileOutputStream(filePath));

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Hata var: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        document.close();
        Toast.makeText(this, " PDF Oluşturuldu ", Toast.LENGTH_SHORT).show();


    }





    private void showDeleteAniDialog(){
        if(dialogDeleteAni == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(AniOlusturma.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_ani,
                    findViewById(R.id.layoutDeleteAni)
            );
            builder.setView(view);
            dialogDeleteAni = builder.create();
            if (dialogDeleteAni.getWindow() != null){
                dialogDeleteAni.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.textDeleteAni).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    class DeleteAniTask extends AsyncTask<Void, Void, Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            AnilarDB.getDataBase(getApplicationContext()).aniDao()
                                    .deleteAni(alreadyExistsAni);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                            Intent intent = new Intent();
                            intent.putExtra("isAniDeleted",true);
                            setResult(RESULT_OK, intent);
                            finish();

                        }
                    }
                    new DeleteAniTask().execute();
                    Toast.makeText(getApplicationContext(),"Anı Silindi",Toast.LENGTH_SHORT).show();
                }
            });

            view.findViewById(R.id.textVazgeç).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogDeleteAni.dismiss();
                }
            });

        }

        dialogDeleteAni.show();
    }

    private void  showSifreCheckDialog(Ani ani){

        if(dialogCheckSifre == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(AniOlusturma.this,0);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_lock_check,
                    findViewById(R.id.layoutLockCheck)
            );
            builder.setView(view);
            dialogCheckSifre = builder.create();
            dialogCheckSifre.setCanceledOnTouchOutside(false);
           // dialogCheckSifre.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

             if(dialogCheckSifre.getWindow()  != null){
                dialogCheckSifre.getWindow().setBackgroundDrawable(null);
            //    dialogCheckSifre.getWindow().setBackgroundBlurRadius(1);
            }
            final EditText inputSifre = view.findViewById(R.id.inputSifre);

            inputSifre.requestFocus();

            view.findViewById(R.id.textGirisSifre).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   if(ani.getRenk().equals(inputSifre.getText().toString())){
                       dialogCheckSifre.dismiss();
                   }else{
                       Toast.makeText(AniOlusturma.this,"Yanlış Şifre",Toast.LENGTH_SHORT).show();
                       finish();

                   }

                }
            });

            view.findViewById(R.id.textVazgeç).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    finish();
                }
            });


            dialogCheckSifre.show();
            dialogCheckSifre.getWindow().setBackgroundDrawable(null);

        }



    }



    private void setViewOrUpdateAni() {

        inputNotBaslik.setText(alreadyExistsAni.getBaslik());
        inputMood.setText(alreadyExistsAni.getMood());
        inputAni.setText(alreadyExistsAni.getAni());
        textTarih.setText(alreadyExistsAni.getTarih());
        if(alreadyExistsAni.getResim() != null && !alreadyExistsAni.getResim().trim().isEmpty()){
            imageAni.setImageBitmap(BitmapFactory.decodeFile(alreadyExistsAni.getResim()));
               imageAni.setVisibility(View.VISIBLE);
               resim = alreadyExistsAni.getResim();

           }
        if(alreadyExistsAni.getRenk() != null && !alreadyExistsAni.getRenk().trim().isEmpty()){
            textSifreAni.setText(alreadyExistsAni.getRenk());



        }

    }



    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else{
                Toast.makeText(this, "Erişim izni verilmedi", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if(data != null){
                Uri selectedImageUri = data.getData();
                if(selectedImageUri !=null){
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageAni.setImageBitmap(bitmap);
                        imageAni.setVisibility(View.VISIBLE);

                        resim =getPathFromUri(selectedImageUri);

                    }catch (Exception exception){
                        Toast.makeText(this, exception.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String getPathFromUri (Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver()
                .query(contentUri,null,null,null,null);
        if(cursor == null){
            filePath = contentUri.getPath();
        }else{
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    


    private void aniKaydet() {
        if(inputNotBaslik.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Lütfen başlık giriniz!", Toast.LENGTH_SHORT).show();
            return;
        }else if (inputAni.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Anı boş olamaz!", Toast.LENGTH_SHORT).show();
            return;
        }



        final Ani ani = new Ani();
        ani.setBaslik(inputNotBaslik.getText().toString());
        ani.setMood(inputMood.getText().toString());
        ani.setAni(inputAni.getText().toString());
        ani.setTarih(textTarih.getText().toString());
        ani.setResim(resim);

        if(layoutSifre.getVisibility() == View.VISIBLE){
            ani.setRenk(textSifreAni.getText().toString());
        }


        if(alreadyExistsAni != null){
            ani.setId(alreadyExistsAni.getId());
        }


        @SuppressLint("StaticFieldLeak")
        class aniKaydet extends AsyncTask<Void, Void, Void>{
            @Override
            protected Void doInBackground(Void... voids) {
                AnilarDB.getDataBase(getApplicationContext()).aniDao().insertAni(ani);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        new aniKaydet().execute();
        if(alreadyExistsAni != null){
            Toast.makeText(getApplicationContext(),"Anı Güncellendi",Toast.LENGTH_SHORT).show();

        }else{
            Toast.makeText(getApplicationContext(),"Anı Eklendi",Toast.LENGTH_SHORT).show();

        }
    }


}
