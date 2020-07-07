package com.spaceapp.space.account;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.spaceapp.space.R;

import java.util.List;

/**
 * When we need to show each contact in message list,
 * this class helps to load layout
 *
 * All these are written according to rules of Android,
 */
public class ContactAdapter extends ArrayAdapter<Contact> {

    private int resourceId;

    public ContactAdapter(Context context, int nameResourceId, List<Contact> objects) {
        super(context, nameResourceId, objects);
        this.resourceId = nameResourceId;
    }

    /**
     * this method will assign correct content to each textview and return the whole view.
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Contact contact = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);

        TextView name = (TextView) view.findViewById(R.id.name);
        TextView lastMsg = (TextView) view.findViewById(R.id.lastMsg);

        name.setText(contact.getName());
        lastMsg.setText(contact.getLastMsg());

        return view;
    }
}
