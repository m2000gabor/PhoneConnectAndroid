package hu.elte.sbzbxr.phoneconnect.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import hu.elte.sbzbxr.phoneconnect.R;

public class LoadingDialog extends DialogFragment {
    private final String loadingMessage;

    public LoadingDialog(String loadingMessage) {
        this.loadingMessage=loadingMessage;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.loading_screen,container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView loadingTextView = (TextView) view.findViewById(R.id.loadingTextView);
        loadingTextView.setText(loadingMessage);
    }
}
