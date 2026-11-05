package it.ramlibera;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** App di sistema Samsung/Google: blocco in background consigliato o vietato. */
final class Suggested {
    private Suggested() {}

    static final Set<String> SAFE = new HashSet<>(Arrays.asList(
            // Facebook / Microsoft / Netflix preinstallate
            "com.facebook.appmanager", "com.facebook.services", "com.facebook.system",
            "com.facebook.katana", "com.microsoft.skydrive", "com.microsoft.office.officehubrow",
            "com.netflix.partner.activation", "com.netflix.mediaclient",
            // Samsung extra
            "com.samsung.android.app.spage", "com.samsung.android.app.tips",
            "com.samsung.android.arzone", "com.samsung.android.aremoji",
            "com.samsung.android.aremojieditor", "com.samsung.android.game.gamehome",
            "com.samsung.android.kidsinstaller", "com.samsung.android.voc",
            "com.samsung.sree", "com.sec.android.easyMover", "com.sec.android.easyMover.Agent",
            "com.samsung.android.bixby.agent", "com.samsung.android.visionintelligence",
            "com.samsung.android.app.watchmanagerstub", "com.samsung.android.mdx",
            "com.samsung.android.smartswitchassistant", "com.samsung.android.app.sharelive",
            // Google extra
            "com.google.android.youtube", "com.google.android.apps.youtube.music",
            "com.google.android.videos", "com.google.android.apps.tachyon",
            "com.google.android.apps.docs", "com.google.android.apps.photos",
            "com.google.android.apps.maps", "com.google.android.apps.magazines"
    ));

    /** Mai bloccare: notifiche, sveglia, sistema. */
    static final Set<String> NEVER = new HashSet<>(Arrays.asList(
            "com.google.android.gms", "com.android.vending", "com.google.android.gsf",
            "com.android.systemui", "com.android.settings", "com.android.phone",
            "com.sec.android.app.launcher", "com.samsung.android.honeyboard",
            "com.sec.android.app.clockpackage", "com.samsung.android.calendar",
            "com.android.bluetooth", "com.samsung.android.providers.context",
            "com.samsung.android.lool", "com.samsung.android.sm.devicesecurity"
    ));
}
