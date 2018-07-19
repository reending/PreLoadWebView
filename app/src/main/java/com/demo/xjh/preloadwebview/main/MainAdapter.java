package com.demo.xjh.preloadwebview.main;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.demo.xjh.preloadwebview.R;

import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<UrlItem> mList;
    private OnItemClickListener mOnItemClickListener;

    public void setData(List<UrlItem> mList) {
        this.mList = mList;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_url, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TextView tvUrl = holder.itemView.findViewById(R.id.tvUrl);
        TextView tvState = holder.itemView.findViewById(R.id.tvState);
        UrlItem urlItem = mList.get(position);
        tvUrl.setText(urlItem.url);
        tvState.setText(urlItem.state);
        holder.itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(urlItem.url));
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }


    interface OnItemClickListener {
        void onItemClick(String url);
    }

    private static class Holder extends RecyclerView.ViewHolder {
        Holder(View itemView) {
            super(itemView);
        }
    }
}
