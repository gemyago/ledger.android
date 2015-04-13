package com.infora.ledger;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;

/**
 * Created by jenya on 14.04.15.
 */
public class EditTransactionDialogTest extends ActivityInstrumentationTestCase2<ReportActivity> {

    public EditTransactionDialogTest() {
        super(ReportActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    private EditTransactionDialog startFragment(PendingTransaction t) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        EditTransactionDialog dialog = new EditTransactionDialog();
        dialog.id = t.getId();
        dialog.amount = t.getAmount();
        dialog.comment = t.getComment();
        transaction.add(dialog, "tag");
        transaction.commit();
        getInstrumentation().waitForIdleSync();
        return dialog;
    }

    public void testInitializeFields() {
        EditTransactionDialog subject = startFragment(new PendingTransaction(100, "100.01", "Comment 100.1"));
        EditText etAmount = (EditText) subject.getDialog().findViewById(R.id.amount);
        EditText etComment = (EditText) subject.getDialog().findViewById(R.id.comment);
        assertEquals("100.01", etAmount.getText().toString());
        assertEquals("Comment 100.1", etComment.getText().toString());
    }
}