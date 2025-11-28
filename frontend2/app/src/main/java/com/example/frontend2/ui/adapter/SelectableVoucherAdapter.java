package com.example.frontend2.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend2.data.model.Voucher;
import com.example.frontend2.databinding.ItemVoucherSelectableBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SelectableVoucherAdapter extends RecyclerView.Adapter<SelectableVoucherAdapter.VoucherHolder> {

    private List<Voucher> voucherList;
    private final OnVoucherSelectListener listener;

    public interface OnVoucherSelectListener {
        void onVoucherSelected(String code);
    }

    public SelectableVoucherAdapter(List<Voucher> voucherList, OnVoucherSelectListener listener) {
        this.voucherList = voucherList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoucherHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVoucherSelectableBinding binding = ItemVoucherSelectableBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new VoucherHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherHolder holder, int position) {
        Voucher voucher = voucherList.get(position);
        holder.bind(voucher);
    }

    @Override
    public int getItemCount() {
        return voucherList.size();
    }

    public void updateVouchers(List<Voucher> newVouchers) {
        this.voucherList.clear();
        this.voucherList.addAll(newVouchers);
        notifyDataSetChanged();
    }

    class VoucherHolder extends RecyclerView.ViewHolder {
        private final ItemVoucherSelectableBinding binding;

        public VoucherHolder(@NonNull ItemVoucherSelectableBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.btnSelectVoucher.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onVoucherSelected(voucherList.get(position).getCode());
                }
            });
        }

        void bind(Voucher voucher) {
            binding.tvVoucherCode.setText(voucher.getCode());
            binding.tvVoucherDescription.setText(voucher.getDescription());

            String formattedDate = formatDate(voucher.getEndDate());
            binding.tvVoucherExpiry.setText("HSD: " + formattedDate);
        }

        private String formatDate(String isoDate) {
            if (isoDate == null || isoDate.isEmpty()) {
                return "N/A";
            }
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                Date date = inputFormat.parse(isoDate);
                return outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return "N/A";
            }
        }
    }
}
