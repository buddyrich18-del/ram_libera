package it.ramlibera;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Una riga per app: spunta = bloccata in background. */
final class AppAdapter extends BaseAdapter {

    interface Listener {
        void onToggle(AppItem it, boolean block);
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
        CheckBox cb = v.findViewById(R.id.block);

        icon.setImageDrawable(it.icon);
        name.setText(it.label);
        String info = it.prot ? "protetta" : (it.blocked ? "bloccata" : "libera");
        if (it.system) info += it.suggested ? " · sistema · consigliata" : " · sistema";
        sub.setText(info);

        cb.setOnCheckedChangeListener(null);
        cb.setChecked(it.blocked);
        cb.setEnabled(!it.prot);
        cb.setOnCheckedChangeListener((b, c) -> listener.onToggle(it, c));

        // Tocco sull'intera riga = spunta
        v.setOnClickListener(x -> { if (!it.prot) cb.toggle(); });
        return v;
    }
}
