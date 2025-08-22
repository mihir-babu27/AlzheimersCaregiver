package com.mihir.alzheimerscaregiver.ui.contacts;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mihir.alzheimerscaregiver.R;
import com.mihir.alzheimerscaregiver.data.entity.ContactEntity;

import java.util.ArrayList;
import java.util.List;

public class ContactsEntityAdapter extends RecyclerView.Adapter<ContactsEntityAdapter.ContactViewHolder> {

    public interface OnContactEntityInteractionListener {
        void onItemClicked(ContactEntity contact);
        void onItemSwipedToDelete(ContactEntity contact, int position);
        void onSetPrimary(ContactEntity contact);
    }

    private final List<ContactEntity> contacts = new ArrayList<>();
    private final Context context;
    private OnContactEntityInteractionListener listener;

    public ContactsEntityAdapter(Context context) {
        this.context = context;
    }

    public void setListener(OnContactEntityInteractionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ContactEntity> newContacts) {
        contacts.clear();
        if (newContacts != null) contacts.addAll(newContacts);
        notifyDataSetChanged();
    }

    public ContactEntity getItem(int position) { return contacts.get(position); }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        holder.bind(contacts.get(position));
    }

    @Override
    public int getItemCount() { return contacts.size(); }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView contactNameText;
        TextView phoneNumberText;
        ImageButton callButton;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            contactNameText = itemView.findViewById(R.id.contactNameText);
            phoneNumberText = itemView.findViewById(R.id.phoneNumberText);
            callButton = itemView.findViewById(R.id.callButton);
        }

        void bind(ContactEntity contact) {
            String displayName = contact.name;
            if (contact.relationship != null && !contact.relationship.isEmpty()) {
                displayName += " (" + contact.relationship + ")";
            }
            if (contact.isPrimary) {
                displayName += " (Primary)";
            }
            contactNameText.setText(displayName);
            phoneNumberText.setText(contact.phoneNumber);

            callButton.setOnClickListener(v -> {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                makePhoneCall(contact.phoneNumber, contact.name);
            });

            itemView.setOnClickListener(v -> {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                if (listener != null) listener.onItemClicked(contact);
            });
        }

        private void makePhoneCall(String phoneNumber, String contactName) {
            try {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                if (callIntent.resolveActivity(context.getPackageManager()) != null) {
                    Toast.makeText(context, "Calling " + contactName + "...", Toast.LENGTH_SHORT).show();
                    context.startActivity(callIntent);
                } else {
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.parse("tel:" + phoneNumber));
                    context.startActivity(dialIntent);
                    Toast.makeText(context, "Opening dialer for " + contactName, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(context, "Unable to make call", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


