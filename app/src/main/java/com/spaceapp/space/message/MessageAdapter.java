package com.spaceapp.space.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.spaceapp.space.R;

import java.util.List;

/**
 * This class helps to fit message objects into correct layout file.
 *
 * This class is written according to rule of Android.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<Message> msgItemList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;

        TextView msgTime;

        public ViewHolder(View view) {
            super(view);
            this.leftLayout = (LinearLayout) view.findViewById(R.id.left_chatbox);
            this.rightLayout = (LinearLayout) view.findViewById(R.id.right_chatbox);
            this.leftMsg = (TextView) view.findViewById(R.id.left_msg);
            this.rightMsg = (TextView) view.findViewById(R.id.right_msg);
            this.msgTime = (TextView) view.findViewById(R.id.message_time);
        }

    }

    public MessageAdapter(List<Message> msgItemList) {
        this.msgItemList = msgItemList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageAdapter.ViewHolder holder, int position) {
        Message msgItem = msgItemList.get(position);

        holder.msgTime.setText(msgItem.getTime().toDate().toString());

        if (msgItem.type) {
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftMsg.setText(msgItem.getContent());
        } else {
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.rightMsg.setText(msgItem.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return msgItemList.size();
    }
}
