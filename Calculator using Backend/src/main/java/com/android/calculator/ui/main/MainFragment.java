package com.android.calculator.ui.main;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.calculator.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class MainFragment extends Fragment {

    private final static String URL_TEMPLATE = "http://10.0.2.2:8080/test/Calculator?f=%f&s=%f&o=%d";

    private final StringBuffer sb = new StringBuffer();
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    private TextView display;
    private Float first;
    private Float second;
    private Float ans;
    private Character operator;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_fragment, container, false);
        display = rootView.findViewById(R.id.display);
        resetStringBuilder();
        display.setText(sb.toString());

        rootView.findViewById(R.id.zero).setOnClickListener(
                (v) -> {
                    if (sb.length() == 1 && sb.charAt(0) == '0') {
                        return;
                    }

                    appendToDisplay('0', false);
                });
        rootView.findViewById(R.id.one).setOnClickListener(
                (v) -> appendToDisplay('1', true));
        rootView.findViewById(R.id.two).setOnClickListener(
                (v) -> appendToDisplay('2', true));
        rootView.findViewById(R.id.three).setOnClickListener(
                (v) -> appendToDisplay('3', true));
        rootView.findViewById(R.id.four).setOnClickListener(
                (v) -> appendToDisplay('4', true));
        rootView.findViewById(R.id.five).setOnClickListener(
                (v) -> appendToDisplay('5', true));
        rootView.findViewById(R.id.six).setOnClickListener(
                (v) -> appendToDisplay('6', true));
        rootView.findViewById(R.id.seven).setOnClickListener(
                (v) -> appendToDisplay('7', true));
        rootView.findViewById(R.id.eight).setOnClickListener(
                (v) -> appendToDisplay('8', true));
        rootView.findViewById(R.id.nine).setOnClickListener(
                (v) -> appendToDisplay('9', true));
        rootView.findViewById(R.id.dot).setOnClickListener(
                (v) -> appendToDisplay('.', false));
        rootView.findViewById(R.id.ac).setOnClickListener(
                (v) -> {
                    resetStringBuilder();
                    display.setText(sb.toString());
                    first = null;
                    second = null;
                    operator = null;
                    ans = null;
                });
        rootView.findViewById(R.id.equal).setOnClickListener((v) -> tryPerformCalculation());
        rootView.findViewById(R.id.plus).setOnClickListener((v) -> applyOperator('+'));
        rootView.findViewById(R.id.minus).setOnClickListener((v) -> applyOperator('-'));
        rootView.findViewById(R.id.multiply).setOnClickListener((v) -> applyOperator('x'));
        rootView.findViewById(R.id.divide).setOnClickListener((v) -> applyOperator('/'));

        return rootView;
    }

    private void appendToDisplay(char c, boolean clearIfPossible) {
        ans = null;
        if (clearIfPossible && sb.length() == 1 && sb.charAt(0) == '0') {
            sb.setLength(0);
        }

        sb.append(c);
        display.setText(sb);
    }

    private void applyOperator(char operator) {
        if (tryPerformCalculation()) {
            first = ans;
        } else if (first == null && ans == null) {
            first = Float.parseFloat(sb.toString());
            resetStringBuilder();
        } else if (first == null) {
            first = ans;
        }

        this.operator = operator;
    }

    private boolean tryPerformCalculation() {
        if (first != null && operator != null) {
            second = Float.parseFloat(sb.toString());
            ans = calculate(first, second, operator);
            int intValue = (int) ans.floatValue();
            display.setText(ans == intValue ? Integer.toString(intValue) : ans.toString());

            resetStringBuilder();
            first = null;
            second = null;
            operator = null;
            return true;
        }

        return false;
    }

    private void resetStringBuilder() {
        sb.setLength(0);
        sb.append('0');
    }

    private float calculate(float first, float second, char operator) {
        Future<Float> future = executor.submit(() -> calculateAsync(first, second, operator));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException ex) {
            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            return 0;
        }
    }

    private float calculateAsync(float first, float second, char operator) throws IOException {
        URL url = new URL(String.format(URL_TEMPLATE, first, second, convertOperator(operator)));
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            try (Scanner scanner = new Scanner(in)) {
                return scanner.nextFloat();
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    private int convertOperator(char operator) {
        switch (operator) {
            case '+':
                return 1;
            case '-':
                return 2;
            case 'x':
                return 3;
            case '/':
                return 4;
            default:
                throw new RuntimeException("Unknown operator " + operator);
        }
    }
}
