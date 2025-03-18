package com.example.mtaa.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mtaa.R;
import com.example.mtaa.models.Report;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {
    private List<Report> reports;
    private SimpleDateFormat dateFormat;
    private boolean isAdminMode;

    public ReportAdapter(List<Report> reports, boolean isAdminMode) {
        this.reports = reports;
        this.isAdminMode = isAdminMode;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reports.get(position);
        holder.categoryText.setText(report.getCategory());
        holder.descriptionText.setText(report.getDescription());
        holder.statusText.setText(report.getStatus());
        
        String formattedDate = dateFormat.format(new Date(report.getTimestamp()));
        holder.timestampText.setText(formattedDate);

        // Show additional information in admin mode
        if (isAdminMode) {
            holder.categoryText.setVisibility(View.VISIBLE);
            holder.statusText.setVisibility(View.VISIBLE);
        } else {
            holder.categoryText.setVisibility(View.GONE);
            holder.statusText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView categoryText;
        TextView descriptionText;
        TextView statusText;
        TextView timestampText;

        ReportViewHolder(View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.report_category);
            descriptionText = itemView.findViewById(R.id.report_description);
            statusText = itemView.findViewById(R.id.report_status);
            timestampText = itemView.findViewById(R.id.report_timestamp);
        }
    }
}