package edu.usm.cs.csc414.pocketfinances;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddRecurringExpensesTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "AddRecurrencesTask";

    private ExpensesViewModel viewModel;
    private List<Expense> expensesList;

    // TODO Need to find better way to instantiate the view model because this leaks context
    @SuppressLint("StaticFieldLeak")
    AppCompatActivity activity;


    public AddRecurringExpensesTask(AppCompatActivity activity) {
        this.activity = activity;
    }


    @Override
    protected Void doInBackground(Void[] params) {

        try {
            Log.i(TAG, "Running background task for adding recurring expenses.");

            if (activity != null) {
                // Initialize the ViewModel
                viewModel = ViewModelProviders.of(activity,
                        new ExpensesViewModelFactory(activity.getApplication(),
                                true, Calendar.getInstance())).get(ExpensesViewModel.class);
            }
            else {
                Log.e(TAG, "Context is null!");
                return null;
            }

            viewModel.getExpensesList().observe(activity, new Observer<List<Expense>>() {
                @Override
                public void onChanged(@Nullable List<Expense> expenseList) {
                    if (expenseList != null) {
                        expensesList = expenseList;
                        addRecurringExpenses();
                    }
                    else
                        Log.e(TAG, "No recurring expenses found!");
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Failed to add recurring expenses to database!", e);
        }


        return null;
    }


    private void addRecurringExpenses() {
        if (expensesList.size() > 0) {

            ArrayList<Expense> updateExpenses = new ArrayList<>();
            ArrayList<Expense> newExpenses = new ArrayList<>();
            ArrayList<Double> accountAmounts = new ArrayList<>();
            ArrayList<Integer> accountIds = new ArrayList<>();

            for (Expense expense : expensesList) {
                //Log.v(TAG, expense.toString());

                if (!accountIds.contains(expense.getAccountId())) {
                    accountIds.add(expense.getAccountId());
                    accountAmounts.add(0.0);
                }

                ArrayList<Calendar> dates = getRecurrenceDates(expense.getRecurrenceRate(), expense.getNextOccurrence());

                if (dates.size() > 1) {


                    for (int i = 0; i < dates.size() - 1; i++) {

                        // Copy expense with new date into array to be added to DB
                        Expense newExpense = new Expense(
                                expense.getAccountId(),
                                expense.getTitle(),
                                expense.getCategory(),
                                expense.getAmount(),
                                dates.get(i),
                                expense.getDepositOrDeduct(),
                                expense.getIsRecurring(),
                                expense.getRecurrenceRate());

                        newExpense.setNextOccurrence(dates.get(i));
                        newExpense.setIsFirstOccurrence(false);

                        Log.v(TAG, newExpense.toString());

                        newExpenses.add(newExpense);

                        int index = accountIds.indexOf(expense.getAccountId());
                        accountAmounts.add(index, accountAmounts.get(index) + (expense.getAmount() * expense.getDepositOrDeduct()));
                    }

                    // Set new nextOccurrence date of original expense
                    expense.setNextOccurrence(dates.get(dates.size() - 1));

                    // Add original expense to array to be updated
                    updateExpenses.add(expense);

                    // If we have expenses to update, dispatch task to do that
                    if (!updateExpenses.isEmpty())
                        updateExpenses(updateExpenses);

                    // If we have new expenses to add to the DB, dispatch task to do that
                    if (!newExpenses.isEmpty())
                        insertExpenses(newExpenses);

                    // If we need to update any account balances, dispatch task to do that
                    if (!accountIds.isEmpty())
                        for (int m = 0; m < accountIds.size(); m++)
                            updateAccountBalance(accountIds.get(m), accountAmounts.get(m));

                }
                else if (dates.size() == 1)
                    Log.i(TAG, "Expense " + expense.getExpenseId() + " : " + expense.getTitle() +" is up to date.  No action necessary.");
            }
        }
        else
            Log.e(TAG, "No recurring expenses found!");

        //viewModel.getExpensesList().removeObservers(activity);
        Log.i(TAG, "Add recurring expenses task complete!");
    }


    private ArrayList<Calendar> getRecurrenceDates(RecurrenceRate rate, Calendar date) {
        ArrayList<Calendar> dates = new ArrayList<>();

        dates.add((Calendar) date.clone());

        while (date.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis()) {
            switch (rate.getValue()) {
                case 1:
                    date.add(Calendar.DAY_OF_YEAR, 1);
                    dates.add((Calendar) date.clone());
                    break;
                case 2:
                    date.add(Calendar.DAY_OF_YEAR, 7);
                    dates.add((Calendar) date.clone());
                    break;
                case 3:
                    date.add(Calendar.DAY_OF_YEAR, 14);
                    dates.add((Calendar) date.clone());
                    break;
                case 4:
                    date.add(Calendar.MONTH, 1);
                    dates.add((Calendar) date.clone());
                    break;
                case 5:
                    date.add(Calendar.MONTH, 3);
                    dates.add((Calendar) date.clone());
                    break;
                case 6:
                    date.add(Calendar.MONTH, 6);
                    dates.add((Calendar) date.clone());
                    break;
                case 7:
                    date.add(Calendar.YEAR, 1);
                    dates.add((Calendar) date.clone());
                    break;
                default:
                    break;
            }
        }

        return dates;
    }


    private void updateExpenses(ArrayList<Expense> expenses) {
        Expense[] array = Arrays.copyOf(expenses.toArray(), expenses.size(), Expense[].class);
        new ExpensesViewModel(activity.getApplication()).updateItem(array);
    }


    private void insertExpenses(ArrayList<Expense> expenses) {
        Expense[] array = Arrays.copyOf(expenses.toArray(), expenses.size(), Expense[].class);
        new ExpensesViewModel(activity.getApplication()).insertItem(array);
    }


    private void updateAccountBalance(int accountId, double amount) {
        new BankAccountsViewModel(activity.getApplication()).updateBalance(accountId, amount);
    }
}