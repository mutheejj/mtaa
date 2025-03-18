package com.example.mtaa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResponseAdapter extends RecyclerView.Adapter<ResponseAdapter.ResponseViewHolder> {
    private List<Response> responseList;
    private SimpleDateFormat dateFormat;
    private boolean isOfficial;

    public ResponseAdapter(List<Response> responseList, boolean isOfficial) {
        this.responseList = responseList;
        this.isOfficial = isOfficial;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ResponseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_response, parent, false);
        return new ResponseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResponseViewHolder holder, int position) {
        Response response = responseList.get(position);
        holder.contentView.setText(response.getContent());
        holder.typeView.setText(response.getType());
        holder.timestampView.setText(dateFormat.format(new Date(response.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return responseList.size();
    }

    static class ResponseViewHolder extends RecyclerView.ViewHolder {
        TextView contentView;
        TextView typeView;
        TextView timestampView;

        ResponseViewHolder(View itemView) {
            super(itemView);
            contentView = itemView.findViewById(R.id.response_content);
            typeView = itemView.findViewById(R.id.response_type);
            timestampView = itemView.findViewById(R.id.response_timestamp);
        }
    }
}