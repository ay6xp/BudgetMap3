package com.example.ahmedyoussef.budgetmap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


public class BudgetDialog extends DialogFragment {

    //widgets
    private EditText mBudget;
    private EditText mRadius;


    //implementing interface so that classes that use this dialog can recieve data
    public interface BudgetDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, String budget, String radius);
        public void onDialogNegativeClick(DialogFragment dialog);



    }
    //instance that delivers events
    BudgetDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the BudgetDialogListener
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the BudgetDialogListener so we can send events to the host
            mListener = (BudgetDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getTargetFragment().toString()
                    + " must implement BudgetDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.budgetdialog,null);
        mBudget = (EditText) view.findViewById(R.id.budget);
        mRadius = (EditText) view.findViewById(R.id.radius);


        builder.setView(view).setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //submit
                Log.v("Update", "Sending data to fragment");
                mListener.onDialogPositiveClick(BudgetDialog.this, mBudget.getText().toString(), mRadius.getText().toString());

                dismiss();


            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //cancel
                mListener.onDialogNegativeClick(BudgetDialog.this);

                dismiss();

            }
        });


    return builder.create();
    }
}
