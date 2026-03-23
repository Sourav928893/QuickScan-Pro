package com.sourav.qrscan;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sourav.qrscan.databinding.ActivityHistoryBinding;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;
    private DatabaseHelper dbHelper;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);
        
        binding.btnHistoryClear.setOnClickListener(v -> onClearHistoryClick());
        
        loadHistory();
    }

    private void loadHistory() {
        List<HistoryItem> history = dbHelper.getAllHistory();
        if (history.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvHistory.setVisibility(View.GONE);
            binding.btnHistoryClear.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvHistory.setVisibility(View.VISIBLE);
            binding.btnHistoryClear.setVisibility(View.VISIBLE);
            binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
            adapter = new HistoryAdapter(history);
            binding.rvHistory.setAdapter(adapter);
        }
    }

    private void onClearHistoryClick() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_history)
                .setMessage(R.string.clear_history_confirm)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    dbHelper.clearHistory();
                    loadHistory();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private final List<HistoryItem> items;

        public HistoryAdapter(List<HistoryItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HistoryItem item = items.get(position);
            holder.tvContent.setText(item.getContent());
            holder.tvTime.setText(item.getTimestamp());
            
            holder.itemView.setOnClickListener(v -> showOptions(item.getContent()));
        }

        private void showOptions(String content) {
            String[] options = {"Copy", "Share", "Open URL"};
            new AlertDialog.Builder(HistoryActivity.this)
                    .setTitle("Options")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("Scanned Text", content);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(HistoryActivity.this, "Copied", Toast.LENGTH_SHORT).show();
                        } else if (which == 1) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_TEXT, content);
                            startActivity(Intent.createChooser(intent, "Share via"));
                        } else if (which == 2) {
                            if (content.startsWith("http") || content.startsWith("www")) {
                                String url = content.startsWith("www") ? "http://" + content : content;
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                            } else {
                                Toast.makeText(HistoryActivity.this, "Not a valid URL", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).show();
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvContent, tvTime;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvContent = itemView.findViewById(R.id.tvHistoryItem);
                tvTime = itemView.findViewById(R.id.tvHistoryTime);
            }
        }
    }
}
