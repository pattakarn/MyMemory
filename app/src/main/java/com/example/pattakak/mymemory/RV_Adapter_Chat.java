package com.example.pattakak.mymemory;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by pattakak on 05/03/2561.
 */

public class RV_Adapter_Chat extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private List<Object> items;

    private final int TITLE = 0, IMAGE = 1;

    public RV_Adapter_Chat(List<Object> items) {
        this.items = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case 0:
                View v1 = inflater.inflate(R.layout.item_chat, parent, false);
                viewHolder = new ViewHolderChat(v1);
                break;
            default:
//                View v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
//                viewHolder = new RecyclerViewSimpleTextViewHolder(v);
//                View v2 = inflater.inflate(R.layout.card_comment, parent, false);
                View v2 = inflater.inflate(R.layout.item_chat, parent, false);
                viewHolder = new ViewHolderChat(v2);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 0:
                ViewHolderChat vh1 = (ViewHolderChat) holder;
                configureViewHolderChat(vh1, position);
                break;

            default:
                ViewHolderChat vh2 = (ViewHolderChat) holder;
                configureViewHolderChat(vh2, position);
                break;
        }
    }

    private void configureViewHolderChat(final ViewHolderChat vh1, int position) {
        String chat_message = (String) items.get(position);
        if (position%2 == 0){
            vh1.getTvChat().setBackgroundResource(R.drawable.shape_chat_send);
            vh1.getRl().setGravity(Gravity.RIGHT);
        } else {
            vh1.getTvChat().setBackgroundResource(R.drawable.shape_chat_recive);
        }
        vh1.getTvChat().setText(chat_message);
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    @Override
    public int getItemViewType(int position) {
//        if (items.get(position) instanceof Text) {
//            return TITLE;
//        } else if (items.get(position) instanceof String) {
//            return IMAGE;
//        }
        return position;
    }


}
