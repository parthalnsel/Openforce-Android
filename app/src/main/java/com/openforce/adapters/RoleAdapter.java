package com.openforce.adapters;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.openforce.R;
import com.openforce.model.Role;

import java.util.List;

public class RoleAdapter extends RecyclerView.Adapter<RoleAdapter.RoleViewHolder> {

    private List<Role> roles;
    private RoleItemClickListener clickListener;

    public RoleAdapter(List<Role> roles) {
        this.roles = roles;
    }

    @NonNull
    @Override
    public RoleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.role_item, parent, false);

        RoleViewHolder roleViewHolder = new RoleViewHolder(view);

        return roleViewHolder;
    }

    @Override
    public int getItemCount() {
        return roles.size();
    }

    @Override
    public void onBindViewHolder(@NonNull RoleViewHolder holder, final int position) {
        final Role role = roles.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RoleAdapter.this.clickListener != null) {
                    clickListener.onItemClick(role, position);
                }
            }
        });
        holder.roleLabel.setText(role.getName());
        if (role.isFeatured()) {
            holder.roleLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
            holder.roleLabel.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.yellow));
        } else {
            holder.roleLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
            holder.roleLabel.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        }
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
        notifyDataSetChanged();
    }

    public static class RoleViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView roleLabel;
        public RoleViewHolder(View v) {
            super(v);
            roleLabel = v.findViewById(R.id.role_label);
        }
    }


    public void setItemClickListener(RoleItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface RoleItemClickListener {
        void onItemClick(Role role, int position);
    }

}
