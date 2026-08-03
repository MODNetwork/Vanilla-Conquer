package dev.pricharda.commandpost;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Credits, attribution and licence text.
 *
 * D-29. This is not decoration - it is how several GPL v3 and EA Section 7
 * obligations are actually discharged in a shipped build:
 *
 *   GPL v3 s5(a) and EA s7   mark the build as modified, not the original
 *   EA s7                    include the copyright notice and terms
 *   GPL v3 s6                state where corresponding source can be obtained
 *   zlib (SDL2) clause 1     acknowledge the software in product documentation
 *   LGPL v2 (openal-soft)    state the relinking position
 *   courtesy                 credit upstream and the reference port
 *
 * The licence bodies are read from assets at runtime rather than pasted into
 * this file. build.gradle copies License.txt and THIRD-PARTY-LICENSES.txt from
 * the repo root into assets at build time, so what a user reads here is
 * byte-identical to the authoritative files and cannot drift from them.
 */
public class CreditsActivity extends Activity {

    private static final int COLOR_BG = Color.parseColor("#0B0B0C");
    private static final int COLOR_FG = Color.parseColor("#E8C46A");
    private static final int COLOR_BODY = Color.parseColor("#C9C9CE");
    private static final int COLOR_DIM = Color.parseColor("#8A8A8F");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setBackgroundColor(COLOR_BG);
        final int pad = (int) (getResources().getDisplayMetrics().density * 24);
        col.setPadding(pad, pad, pad, pad);

        col.addView(heading(getString(R.string.app_name)));
        col.addView(body(getString(R.string.credits_modified_notice)));

        col.addView(heading("THE GAMES"));
        col.addView(body(getString(R.string.credits_games)));

        col.addView(heading("THIS PORT"));
        col.addView(body(getString(R.string.credits_port)));

        col.addView(heading("BUILT ON"));
        col.addView(body(getString(R.string.credits_upstream)));

        col.addView(heading("GAME DATA"));
        col.addView(body(getString(R.string.credits_assets)));

        col.addView(heading("TRADEMARKS"));
        col.addView(body(getString(R.string.credits_trademarks)));

        col.addView(heading("SOURCE CODE"));
        col.addView(body(getString(R.string.credits_source)));

        col.addView(heading("THIRD PARTY LICENCES"));
        col.addView(mono(readAsset("THIRD-PARTY-LICENSES.txt")));

        col.addView(heading("LICENCE (GPL v3 + EA ADDITIONAL TERMS)"));
        col.addView(mono(readAsset("License.txt")));

        final ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(COLOR_BG);
        scroll.addView(col);
        setContentView(scroll);
    }

    private TextView heading(String text) {
        final TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(COLOR_FG);
        t.setTextSize(16);
        t.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        t.setLetterSpacing(0.14f);
        t.setPadding(0, (int) (getResources().getDisplayMetrics().density * 22), 0,
                     (int) (getResources().getDisplayMetrics().density * 6));
        t.setGravity(Gravity.START);
        return t;
    }

    private TextView body(String text) {
        final TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(COLOR_BODY);
        t.setTextSize(13);
        t.setLineSpacing(0f, 1.25f);
        return t;
    }

    /** Verbatim licence text: monospace, smaller, visually distinct from prose. */
    private TextView mono(String text) {
        final TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(COLOR_DIM);
        t.setTextSize(9);
        t.setTypeface(Typeface.MONOSPACE);
        t.setHorizontallyScrolling(false);
        return t;
    }

    /**
     * Read a licence file from assets.
     *
     * A failure here is reported in place rather than swallowed. If the licence
     * text is missing from a build, that build is not distributable, and hiding
     * the fact behind an empty view would be the worst possible outcome.
     */
    private String readAsset(String name) {
        final AssetManager assets = getAssets();
        final StringBuilder sb = new StringBuilder();

        try (InputStream in = assets.open(name);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            return "*** " + name + " IS MISSING FROM THIS BUILD ***\n\n"
                 + "This build does not carry its required licence text and must not be\n"
                 + "distributed. Rebuild with the copyLicenceAssets task enabled.\n\n"
                 + e;
        }

        return sb.toString();
    }
}
