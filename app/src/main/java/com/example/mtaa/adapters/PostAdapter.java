package com.example.mtaa.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mtaa.databinding.ItemPostBinding;
import com.example.mtaa.models.Post;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private final List<Post> posts;
    private final SimpleDateFormat dateFormat;

    public PostAdapter(List<Post> posts) {
        this.posts = posts;
        this.dateFormat = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostBinding binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new PostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private final ItemPostBinding binding;

        PostViewHolder(ItemPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Post post) {
            binding.userName.setText(post.getUserName());
            binding.postContent.setText(post.getContent());
            binding.postTime.setText(dateFormat.format(post.getTimestamp().toDate()));
            
            // Set like count
            binding.likeButton.setText(String.format(Locale.getDefault(), "Like (%d)", post.getLikes()));
            
            // Set comment count
            binding.commentButton.setText(String.format(Locale.getDefault(), "Comment (%d)", post.getComments()));

            // Handle click listeners
            binding.likeButton.setOnClickListener(v -> {
                // Handle like action
            });

            binding.commentButton.setOnClickListener(v -> {
                // Handle comment action
            });
        }
    }
} 