package com.example.diaryapp.adapters;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diaryapp.R;
import com.example.diaryapp.entities.Ani;
import com.example.diaryapp.listeners.AnilarListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class AnilarAdapter extends RecyclerView.Adapter<AnilarAdapter.AniViewHolder> {

    private List<Ani> anis;
    private AnilarListener anilarListener;
    private Timer timer;
    private List<Ani> aniSource;


    public AnilarAdapter(List<Ani> anis , AnilarListener anilarListener ) {
        this.anis = anis;
        this.anilarListener = anilarListener;
        aniSource = anis;

    }
    @NonNull
    @Override
    public AniViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AniViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_ani,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull AniViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.setAni(anis.get(position));
        holder.layoutAni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anilarListener.onAniClicked(anis.get(position),position);

            }
        });
    }

    @Override
    public int getItemCount() {
        return anis.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class AniViewHolder extends RecyclerView.ViewHolder{
        TextView textBaslik,textMood,textTarih;
        LinearLayout layoutAni;
        RoundedImageView imageAni;


         AniViewHolder(@NonNull View itemView) {
            super(itemView);
            textBaslik = itemView.findViewById(R.id.textBaslik);
            textMood = itemView.findViewById(R.id.textMood);
            textTarih = itemView.findViewById(R.id.textTarih);
            imageAni = itemView.findViewById(R.id.imageAni);
            layoutAni = itemView.findViewById(R.id.layoutAni);
        }

        void setAni(Ani ani){
             textBaslik.setText(ani.getBaslik());
             if(ani.getMood().trim().isEmpty()){
                 textMood.setVisibility(View.GONE);
             }else{
                 textMood.setText(ani.getMood());
             }
             textTarih.setText(ani.getTarih());

             if (ani.getResim() != null){
                 imageAni.setImageBitmap(BitmapFactory.decodeFile(ani.getResim()));
                 imageAni.setVisibility(View.VISIBLE);
             }else{
                 imageAni.setVisibility(View.GONE);
             }


        }
    }

    public void searchAni(final String searchKeyword){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(searchKeyword.trim().isEmpty()){
                    anis = aniSource;

                }else{
                    ArrayList<Ani> temp  = new ArrayList<>();
                    for(Ani ani : aniSource){
                        if (ani.getBaslik().toLowerCase().contains(searchKeyword.toLowerCase())){
                            temp.add(ani);

                        }
                    }
                    anis = temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });

            }
        },500 );
    }

    public void cancelTimer(){
        if (timer != null){
            timer.cancel();
        }
    }
}
