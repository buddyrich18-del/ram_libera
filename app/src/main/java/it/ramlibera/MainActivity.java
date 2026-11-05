package it.ramlibera;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rikka.shizuku.Shizuku;

public class MainActivity extends Activity implements AppAdapter.Listener {

    private static final int REQ = 42;
    private static final String KEY_EXCLUDED = "excluded";

    private final ExecutorService bg = Executors.newSingleThreadExecutor();
    private final Handler ui = new Handler(Looper.getMainLooper());
    private final Set<String> protectedPkgs = new HashSet<>();

    private TextView status, ram;
    private Button grant;
    private CheckBox showSys;
    private AppAdapter adapter;
    private SharedPreferences prefs;

    private final Shizuku.OnBinderReceivedListener binderReceived = () -> ui.post(this::refreshStatus);
    private final Shizuku.OnBinderDeadListener binderDead = () -> ui.post(this::refreshStatus);
    private final Shizuku.OnRequestPermissionResultListener permResult =
            (code, result) -> ui.post(this::refreshStatus);

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        status = findViewById(R.id.status);
        ram = findViewById(R.id.ram);
        grant = findViewById(R.id.grant);
        showSys = findViewById(R.id.showSys);
        ListView list = findViewById(R.id.list);

        adapter = new AppAdapter(this, this);
        list.setAdapter(adapter);

        grant.setOnClickListener(v -> {
            try { Shizuku.requestPermission(REQ); } catch (Exception e) { toast(notReadyMsg()); }
        });
        findViewById(R.id.freeRam).setOnClickListener(v -> freeRam());
        findViewById(R.id.blockAll).setOnClickListener(v -> confirmBlockAll());
        findViewById(R.id.unblockAll).setOnClickListener(v -> unblockAll());
        showSys.setOnCheckedChangeListener((x, c) -> loadApps());

        computeProtected();
        Shizuku.addBinderReceivedListenerSticky(binderReceived);
        Shizuku.addBinderDeadListener(binderDead);
        Shizuku.addRequestPermissionResultListener(permResult);
        refreshStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
        updateRam();
    }

    @Override
    protected void onDestroy() {
        Shizuku.removeBinderReceivedListener(binderReceived);
        Shizuku.removeBinderDeadListener(binderDead);
        Shizuku.removeRequestPermissionResultListener(permResult);
        bg.shutdown();
        super.onDestroy();
    }

    // ---------- Shizuku ----------

    private boolean isReady() {
        try {
            return Shizuku.pingBinder()
                    && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            return false;
        }
    }

    private String notReadyMsg() {
        boolean alive;
        try { alive = Shizuku.pingBinder(); } catch (Exception e) { alive = false; }
        return alive ? "Permesso Shizuku mancante" : "Shizuku non attivo";
    }

    private void refreshStatus() {
        boolean alive;
        try { alive = Shizuku.pingBinder(); } catch (Exception e) { alive = false; }
        boolean ok = isReady();
        status.setText(ok ? "Shizuku attivo"
                : alive ? "Concedi il permesso a Shizuku"
                : "Shizuku non attivo: avvialo, poi chiudi e riapri questa app");
        grant.setVisibility(alive && !ok ? View.VISIBLE : View.GONE);
        loadApps();
    }

    // ---------- Elenco app ----------

    private void computeProtected() {
        protectedPkgs.add(getPackageName());
        protectedPkgs.add("moe.shizuku.privileged.api");
        protectedPkgs.addAll(Suggested.NEVER);
        String ime = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        if (ime != null && ime.contains("/")) protectedPkgs.add(ime.substring(0, ime.indexOf('/')));
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfo ri = getPackageManager().resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY);
        if (ri != null && ri.activityInfo != null) protectedPkgs.add(ri.activityInfo.packageName);
    }

    private Set<String> excluded() {
        return new HashSet<>(prefs.getStringSet(KEY_EXCLUDED, new HashSet<>()));
    }

    private Set<String> queryBlocked() {
        Set<String> out = new HashSet<>();
        if (!isReady()) return out;
        String res = Sh.run("cmd appops query-op --user 0 RUN_ANY_IN_BACKGROUND ignore");
        for (String line : res.split("\n")) {
            String p = line.trim();
            if (p.contains(".") && !p.contains(" ")) out.add(p);
        }
        return out;
    }

    private void loadApps() {
        final boolean sys = showSys.isChecked();
        bg.execute(() -> {
            PackageManager pm = getPackageManager();
            Set<String> blocked = queryBlocked();
            Set<String> ex = excluded();
            List<AppItem> items = new ArrayList<>();
            for (ApplicationInfo ai : pm.getInstalledApplications(0)) {
                boolean isSys = (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                if (isSys && !sys) continue;
                if (ai.packageName.equals(getPackageName())) continue;
                AppItem it = new AppItem();
                it.pkg = ai.packageName;
                it.label = ai.loadLabel(pm).toString();
                it.icon = ai.loadIcon(pm);
                it.system = isSys;
                it.blocked = blocked.contains(ai.packageName);
                it.excluded = ex.contains(ai.packageName);
                it.prot = protectedPkgs.contains(ai.packageName);
                it.suggested = isSys && Suggested.SAFE.contains(ai.packageName);
                items.add(it);
            }
            items.sort((a, c) -> a.suggested != c.suggested ? (a.suggested ? -1 : 1)
                    : a.label.compareToIgnoreCase(c.label));
            ui.post(() -> adapter.setItems(items));
        });
    }

    // ---------- Azioni ----------

    @Override
    public void onToggle(AppItem it, boolean block) {
        if (!isReady()) {
            toast(notReadyMsg());
            adapter.notifyDataSetChanged();
            return;
        }
        bg.execute(() -> {
            Sh.run(block ? Sh.block(it.pkg) : Sh.unblock(it.pkg));
            it.blocked = queryBlocked().contains(it.pkg);
            ui.post(() -> { adapter.notifyDataSetChanged(); updateRam(); });
        });
    }

    @Override
    public void onExclude(AppItem it, boolean ex) {
        Set<String> s = excluded();
        if (ex) s.add(it.pkg); else s.remove(it.pkg);
        prefs.edit().putStringSet(KEY_EXCLUDED, s).apply();
        it.excluded = ex;
    }

    private List<String> blockTargets() {
        List<String> t = new ArrayList<>();
        for (AppItem it : adapter.getItems()) {
            if (!it.system && !it.excluded && !it.prot && !it.blocked) t.add(it.pkg);
        }
        return t;
    }

    private void confirmBlockAll() {
        if (!isReady()) { toast(notReadyMsg()); return; }
        List<String> t = blockTargets();
        if (t.isEmpty()) { toast("Nessuna app da bloccare"); return; }
        new AlertDialog.Builder(this)
                .setMessage("Bloccare in background " + t.size()
                        + " app? Le escluse (spunta) restano libere.")
                .setPositiveButton("Blocca", (d, w) -> runBatch(t, true))
                .setNegativeButton("Annulla", null)
                .show();
    }

    private void unblockAll() {
        if (!isReady()) { toast(notReadyMsg()); return; }
        bg.execute(() -> {
            List<String> t = new ArrayList<>(queryBlocked());
            ui.post(() -> {
                if (t.isEmpty()) toast("Nessuna app bloccata");
                else runBatch(t, false);
            });
        });
    }

    private void runBatch(List<String> pkgs, boolean block) {
        bg.execute(() -> {
            StringBuilder sb = new StringBuilder();
            for (String p : pkgs) sb.append(block ? Sh.block(p) : Sh.unblock(p));
            Sh.run(sb.toString());
            ui.post(() -> {
                toast((block ? "Bloccate " : "Sbloccate ") + pkgs.size() + " app");
                loadApps();
                updateRam();
            });
        });
    }

    private void freeRam() {
        if (!isReady()) { toast(notReadyMsg()); return; }
        final long before = availMb();
        bg.execute(() -> {
            Sh.run("am kill-all");
            ui.postDelayed(() -> {
                long diff = Math.max(0, availMb() - before);
                toast("Liberati " + diff + " MB");
                updateRam();
            }, 1500);
        });
    }

    // ---------- RAM ----------

    private ActivityManager.MemoryInfo memInfo() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi;
    }

    private long availMb() { return memInfo().availMem / (1024 * 1024); }

    private void updateRam() {
        ActivityManager.MemoryInfo mi = memInfo();
        double gb = 1024.0 * 1024 * 1024;
        ram.setText(String.format(Locale.ITALY, "RAM libera %.1f / %.1f GB",
                mi.availMem / gb, mi.totalMem / gb));
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}
