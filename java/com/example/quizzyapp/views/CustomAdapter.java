package com.example.quizzyapp.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.quizzyapp.R;

import java.util.List;

public class CustomAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> data;

    public CustomAdapter(@NonNull Context context, List<String> data) {
        super(context, R.layout.list_item, data);
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        }

        String item = data.get(position);
        String[] parts = item.split(": ");
        String playerName = parts[0];
        String score = parts[1];

        TextView playerNameTextView = convertView.findViewById(R.id.leaderboardItemText);
        TextView scoreTextView = convertView.findViewById(R.id.leaderboardScoreText);

        playerNameTextView.setText(playerName);
        scoreTextView.setText(score);

        return convertView;
    }
}

