package com.example.frontend2.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend2.data.model.Voucher;
import com.example.frontend2.databinding.ItemVoucherBinding;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    private final Context context;
    private List<Voucher> voucherList;
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public VoucherAdapter(Context context, List<Voucher> voucherList) {
        this.context = context;
        this.voucherList = voucherList;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVoucherBinding binding = ItemVoucherBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VoucherViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);
        holder.bind(voucher);
    }

    @Override
    public int getItemCount() {
        return voucherList.size();
    }

    public void updateVouchers(List<Voucher> newVouchers) {
        this.voucherList = newVouchers;
        notifyDataSetChanged();
    }

    class VoucherViewHolder extends RecyclerView.ViewHolder {
        private final ItemVoucherBinding binding;

        public VoucherViewHolder(ItemVoucherBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Voucher voucher) {
            binding.tvVoucherDescription.setText(voucher.getDescription());
            binding.tvVoucherCode.setText("Mã: " + voucher.getCode());

            if (voucher.getEndAt() != null) {
                binding.tvVoucherExpiry.setText("HSD: " + outputFormat.format(voucher.getEndAt()));
            } else {
                binding.tvVoucherExpiry.setText("Không có hạn sử dụng");
            }

            String tier = voucher.getTierRequired();
            if (tier != null && !tier.equalsIgnoreCase("all")) {
                binding.tvTierRequired.setText(tier.substring(0, 1).toUpperCase() + tier.substring(1));
                binding.tvTierRequired.setVisibility(View.VISIBLE);
            } else {
                binding.tvTierRequired.setVisibility(View.GONE);
            }
        }
    }
}
