package it.ramlibera;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import rikka.shizuku.Shizuku;

/** Esegue comandi shell con i permessi ADB tramite Shizuku. */
final class Sh {
    private Sh() {}

    static String run(String cmd) {
        try {
            Method m = Shizuku.class.getDeclaredMethod("newProcess",
                    String[].class, String[].class, String.class);
            m.setAccessible(true);
            Process p = (Process) m.invoke(null, new String[]{"sh", "-c", cmd}, null, null);
            String out = read(p.getInputStream()) + read(p.getErrorStream());
            p.waitFor();
            return out;
        } catch (Exception e) {
            return "ERR " + e;
        }
    }

    private static String read(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append('\n');
        }
        return sb.toString();
    }

    static String block(String pkg) {
        return "cmd appops set " + pkg + " RUN_ANY_IN_BACKGROUND ignore; "
                + "cmd appops set " + pkg + " RUN_IN_BACKGROUND ignore; "
                + "am set-standby-bucket " + pkg + " restricted; "
                + "am force-stop " + pkg + "; ";
    }

    static String unblock(String pkg) {
        return "cmd appops set " + pkg + " RUN_ANY_IN_BACKGROUND allow; "
                + "cmd appops set " + pkg + " RUN_IN_BACKGROUND allow; "
                + "am set-standby-bucket " + pkg + " active; ";
    }
}
