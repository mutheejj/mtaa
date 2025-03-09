package com.example.mtaa.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mtaa.R;
import com.example.mtaa.models.Achievement;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {
    private List<Achievement> achievements = new ArrayList<>();

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);
        holder.achievementTitle.setText(achievement.getTitle());
        holder.achievementDescription.setText(achievement.getDescription());
        
        // Load achievement icon using Glide
        if (achievement.getIconUrl() != null && !achievement.getIconUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(achievement.getIconUrl())
                    .placeholder(R.drawable.ic_achievement_placeholder)
                    .error(R.drawable.ic_achievement_placeholder)
                    .into(holder.achievementIcon);
        }
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    public void setAchievements(List<Achievement> achievements) {
        this.achievements = achievements;
        notifyDataSetChanged();
    }

    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        ImageView achievementIcon;
        TextView achievementTitle;
        TextView achievementDescription;

        AchievementViewHolder(View itemView) {
            super(itemView);
            achievementIcon = itemView.findViewById(R.id.achievementIcon);
            achievementTitle = itemView.findViewById(R.id.achievementTitle);
            achievementDescription = itemView.findViewById(R.id.achievementDescription);
        }
    }
}