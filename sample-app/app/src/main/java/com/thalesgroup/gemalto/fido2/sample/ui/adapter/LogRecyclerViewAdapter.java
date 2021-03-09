package com.thalesgroup.gemalto.fido2.sample.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.thalesgroup.gemalto.fido2.sample.R;

import java.util.List;

public class LogRecyclerViewAdapter extends RecyclerView.Adapter<LogRecyclerViewAdapter.LogRVHolder> {

    private List<String> items;

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public LogRVHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LogRVHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LogRVHolder holder, int position) {
        String item = items.get(position);
        holder.textView.setText(item);
        holder.textView.setOnClickListener(v -> {
            if (LogRecyclerViewAdapter.this.onItemClickListener != null) {
                LogRecyclerViewAdapter.this.onItemClickListener.onClickCopy(items.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    static class LogRVHolder extends RecyclerView.ViewHolder {

        TextView textView;

        public LogRVHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.textView);
        }
    }

    public interface OnItemClickListener {
        void onClickCopy(String item);
    }

}

