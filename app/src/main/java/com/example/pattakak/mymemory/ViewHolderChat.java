package com.example.pattakak.mymemory;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by pattakak on 05/03/2561.
 */

public class ViewHolderChat extends RecyclerView.ViewHolder  {

    private TextView tvChat;
    private RelativeLayout rl;

    public ViewHolderChat(View itemView) {
        super(itemView);
        this.rl = (RelativeLayout) itemView.findViewById(R.id.rl);
        this.tvChat = (TextView) itemView.findViewById(R.id.tv_chat);
    }

    public RelativeLayout getRl() {
        return rl;
    }

    public void setRl(RelativeLayout rl) {
        this.rl = rl;
    }

    public TextView getTvChat() {
        return tvChat;
    }

    public void setTvChat(TextView tvChat) {
        this.tvChat = tvChat;
    }
}
