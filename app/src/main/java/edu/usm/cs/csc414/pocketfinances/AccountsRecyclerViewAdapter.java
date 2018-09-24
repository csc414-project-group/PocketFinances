package edu.usm.cs.csc414.pocketfinances;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class AccountsRecyclerViewAdapter extends RecyclerView.Adapter<AccountsRecyclerViewAdapter.RecyclerViewHolder> {

    private List<BankAccount> bankAccountList;

    public AccountsRecyclerViewAdapter(List<BankAccount> bankAccountList) {
        this.bankAccountList = bankAccountList;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.accounts_recyclerview_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, int position) {
        BankAccount bankAccount = bankAccountList.get(position);

        if (bankAccount.getAccountBalance() < 0)
            holder.balanceTextView.setText(String.format( Locale.US,"-$%.2f", Math.abs(bankAccount.getAccountBalance())));

        holder.nameTextView.setText(bankAccount.getAccountName());
        holder.bankTextView.setText(bankAccount.getBankName());
        holder.balanceTextView.setText(String.format("$%s", bankAccount.getAccountBalance()));
        holder.itemView.setTag(bankAccount);
    }

    @Override
    public int getItemCount() {
        return bankAccountList.size();
    }

    public void addItems(List<BankAccount> bankAccountList) {
        this.bankAccountList = bankAccountList;
        notifyDataSetChanged();
    }

    static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView bankTextView;
        private TextView balanceTextView;

        RecyclerViewHolder(View view) {
            super(view);
            nameTextView = (TextView) view.findViewById(R.id.accounts_recyclerview_textview_accountname);
            bankTextView = (TextView) view.findViewById(R.id.accounts_recyclerview_textview_bankname);
            balanceTextView = (TextView) view.findViewById(R.id.accounts_recyclerview_textview_accountbalance);
        }
    }

}