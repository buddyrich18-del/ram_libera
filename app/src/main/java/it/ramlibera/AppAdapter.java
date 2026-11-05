package it.ramlibera;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

final class AppAdapter extends BaseAdapter {

    interface Listener {
        void onToggle(AppItem it, boolean block);
        void onExclude(AppItem it, boolean excluded);
    }

    private final LayoutInflater inflater;
    private final Listener listener;
    private List<AppItem> items = new ArrayList<>();

    AppAdapter(Context ctx, Listener l) {
        inflater = LayoutInflater.from(ctx);
        listener = l;
    }

    void setItems(List<AppItem> list) {
        items = list;
        notifyDataSetChanged();
    }

    List<AppItem> getItems() { return items; }

    @Override public int getCount() { return items.size(); }
    @Override public Object getItem(int i) { return items.get(i); }
    @Override public long getItemId(int i) { return i; }

    @Override
    public View getView(int pos, View v, ViewGroup parent) {
        if (v == null) v = inflater.inflate(R.layout.item_app, parent, false);
        AppItem it = items.get(pos);

        ImageView icon = v.findViewById(R.id.icon);
        TextView name = v.findViewById(R.id.name);
        TextView sub = v.findViewById(R.id.sub);
        CheckBox ex = v.findViewById(R.id.exclude);
        Switch sw = v.findViewById(R.id.block);

        icon.setImageDrawable(it.icon);
        name.setText(it.label);
        String info = it.prot ? "protetta" : (it.blocked ? "bloccata in background" : "libera");
        if (it.system) info += it.suggested ? " · sistema · consigliata" : " · sistema";
        sub.setText(info);

        ex.setOnCheckedChangeListener(null);
        ex.setChecked(it.excluded || it.prot);
        ex.setEnabled(!it.prot);
        ex.setOnCheckedChangeListener((b, c) -> listener.onExclude(it, c));

        sw.setOnCheckedChangeListener(null);
        sw.setChecked(it.blocked);
        sw.setEnabled(!it.prot);
        sw.setOnCheckedChangeListener((b, c) -> listener.onToggle(it, c));
        return v;
    }
}
