package com.example.diaryapp.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.diaryapp.R;
import com.example.diaryapp.adapters.AnilarAdapter;
import com.example.diaryapp.database.AnilarDB;
import com.example.diaryapp.entities.Ani;
import com.example.diaryapp.listeners.AnilarListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AnilarListener {


    public static final int REQUEST_CODE_ADD_ANI = 1;
    public static final int REQUEST_CODE_UPDATE_ANI = 2;
    public static final int REQUEST_CODE_VIEW_ANI = 3;


    private RecyclerView anilarRecyclerView;
    private List<Ani> aniList;
    private AnilarAdapter anilarAdapter;

    private int aniClickedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ImageView imageNotEkle = findViewById(R.id.imageAnaNotEkle);
        imageNotEkle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(getApplicationContext(), AniOlusturma.class),
                        REQUEST_CODE_ADD_ANI

                );
            }
        });

        anilarRecyclerView  = findViewById(R.id.anilarRecyclerView);
        anilarRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        );

        aniList = new ArrayList<>();
        anilarAdapter = new AnilarAdapter(aniList,this);
        anilarRecyclerView.setAdapter(anilarAdapter);

        getAnilar(REQUEST_CODE_VIEW_ANI,false);

        EditText inputSearch = findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                anilarAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (aniList.size() != 0){
                    anilarAdapter.searchAni(editable.toString());
                }
            }
        });
    }

    @Override
    public void onAniClicked(Ani ani, int position) {
        aniClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(),AniOlusturma.class);
        intent.putExtra("isViewOrUpdate",true);
        intent.putExtra("ani", ani);
        startActivityForResult(intent,REQUEST_CODE_UPDATE_ANI);

    }

    private void getAnilar(final int requestCode, final boolean isAniDeleted){

        class getAnilarTask extends AsyncTask<Void, Void , List<Ani>>{
            @Override
            protected List<Ani> doInBackground(Void... voids) {
                return AnilarDB.getDataBase(getApplicationContext()).aniDao().getAllAni();
            }

            @Override
            protected void onPostExecute(List<Ani> anis) {
                super.onPostExecute(anis);
                if (requestCode == REQUEST_CODE_VIEW_ANI){
                    aniList.addAll(anis);
                    anilarAdapter.notifyDataSetChanged();
                }else if (requestCode == REQUEST_CODE_ADD_ANI){
                    aniList.add(0,anis.get(0));
                    anilarAdapter.notifyItemInserted(0);
                    anilarRecyclerView.smoothScrollToPosition(0);
                }else if (requestCode == REQUEST_CODE_UPDATE_ANI){
                    
                    aniList.remove(aniClickedPosition);

                    if (isAniDeleted){
                       anilarAdapter.notifyItemRemoved(aniClickedPosition);
                    }else{
                        aniList.add(aniClickedPosition, anis.get(aniClickedPosition));
                        anilarAdapter.notifyItemChanged(aniClickedPosition);

                    }
                }
            }
        }
        new getAnilarTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_ANI && resultCode == RESULT_OK){
            getAnilar(REQUEST_CODE_ADD_ANI,false);
        }else if (requestCode == REQUEST_CODE_UPDATE_ANI && resultCode == RESULT_OK){
            if(data != null){
                getAnilar(REQUEST_CODE_UPDATE_ANI ,data.getBooleanExtra("isAniDeleted",false));
            }
        }
    }
}