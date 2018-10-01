package edu.usm.cs.csc414.pocketfinances;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpensesFragment extends Fragment {

    private static final String TAG = "ExpensesFragment";

    // Declare Typeface for custom font
    Typeface FONT_WALKWAY;

    // Declare UI elements
    TextView titleTextView, parentAccountBalanceTextView;
    ImageButton addExpenseBtn, backBtn;
    RecyclerView recyclerView;
    ExpensesRecyclerViewAdapter recyclerViewAdapter;
    ExpensesViewModel viewModel;

    int activeAccountId;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);

        // Initialize UI elements
        titleTextView = view.findViewById(R.id.fragment_expenses_title_textview);
        addExpenseBtn = view.findViewById(R.id.fragment_expenses_add_btn);
        backBtn = view.findViewById(R.id.fragment_expenses_back_btn);
        parentAccountBalanceTextView = view.findViewById(R.id.fragment_expenses_accountbalance_textview);

        try {
            activeAccountId = getArguments().getInt("activeAccountId");

            Log.d(TAG, "Parent account id: " + activeAccountId);

            BankAccountsViewModel accountsViewModel = ViewModelProviders.of(this).get(BankAccountsViewModel.class);

            observeActiveAccount(accountsViewModel);
        } catch(Exception e) {
            Log.e(TAG, "Unable to get bank account ID.", e);
            getActivity().getSupportFragmentManager().popBackStack();
        }

        try {
            // Initialize custom font from resource
            FONT_WALKWAY = Typeface.createFromAsset(getActivity().getAssets(), getString(R.string.font_walkway_black));

            // Set the font for UI elements to custom font
            titleTextView.setTypeface(FONT_WALKWAY);
        } catch(Exception e) {
            Log.e(TAG, "Unable to set font of application text.");
        }
        Log.d(TAG, "Font of UI text successfully set.");


        // Initialize the RecyclerView for holding the list of accounts
        recyclerView = view.findViewById(R.id.fragment_expenses_recyclerview);

        // Create the RecyclerViewAdapter for the RecyclerView
        recyclerViewAdapter = new ExpensesRecyclerViewAdapter(new ArrayList<Expense>());

        // Set the LayoutManager to be the LinearLayoutManager
        // This makes the list display linearly
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Attach the RecyclerViewAdapter to the RecyclerView
        recyclerView.setAdapter(recyclerViewAdapter);

        // Initialize the ViewModel
        viewModel = ViewModelProviders.of(this,
                new ExpenseViewModelFactory(this.getActivity().getApplication(),
                        activeAccountId, null)).get(ExpensesViewModel.class);

        // Set the ViewModel to observe the live BankAccount data list
        //      so that the UI will update whenever the data or config changes
        viewModel.getExpensesList().observe(getActivity(), new Observer<List<Expense>>() {
            @Override
            public void onChanged(@Nullable List<Expense> expenseList) {
                recyclerViewAdapter.addItems(expenseList);
            }
        });

        // set the listeners for any UI elements that need them
        setListeners();


        return view;
    }


    // Method for setting all the listeners
    private void setListeners() {
        addExpenseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                NewExpenseDialog newExpenseDialog = new NewExpenseDialog(getActivity(), activeAccountId);
                newExpenseDialog.show();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccountsFragment accountsFragment = new AccountsFragment();

                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction
                        .replace(R.id.activity_main_framelayout, accountsFragment)
                        .commit();
            }
        });

        recyclerViewAdapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int itemPosition = recyclerView.getChildLayoutPosition(view);
                Expense clickedExpense = viewModel.getExpensesList().getValue().get(itemPosition);

                EditDeleteExpenseDialog editDeleteExpenseDialog = new EditDeleteExpenseDialog(getActivity(), viewModel,clickedExpense);
                editDeleteExpenseDialog.show();
                return true;
            }
        });
    }


    private void observeActiveAccount(BankAccountsViewModel accountsViewModel) {
        accountsViewModel.getBankAccount(activeAccountId).observe(this, new Observer<BankAccount>() {
            @Override
            public void onChanged(@Nullable BankAccount bankAccount) {
                if (bankAccount.getAccountBalance() < 0.0) {
                    parentAccountBalanceTextView.setText(String.format(Locale.US, "-$%.2f", Math.abs(bankAccount.getAccountBalance())));
                    parentAccountBalanceTextView.setTextColor(getResources().getColor(R.color.colorRed));
                }
                    else {
                    parentAccountBalanceTextView.setText(String.format(Locale.US, "$%.2f", bankAccount.getAccountBalance()));
                    parentAccountBalanceTextView.setTextColor(getResources().getColor(R.color.colorGreen));
                }
                titleTextView.setText(bankAccount.getAccountName());
            }

        });
    }


    private void showToastMessage(String message) {
        Toast.makeText(getActivity().getBaseContext(), message,
                Toast.LENGTH_SHORT).show();
    }
}