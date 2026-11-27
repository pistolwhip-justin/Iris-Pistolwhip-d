/*
 * Copyright (C) 2026 Pistol Whip Productions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nabu.iris.keyboard.latin;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import nabu.iris.keyboard.compat.PreferenceManagerCompat;
import android.view.View;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Local suggestion strip and personal vocabulary for Iris Keyboard Pistolwhip'd.
 *
 * Base spelling candidates are requested from Android's selected spell-checker service.
 * Personal vocabulary and next-word frequency data are stored locally in app preferences.
 */
public final class AutocorrectBarController
        implements SpellCheckerSession.SpellCheckerSessionListener {
    public interface CandidateClickListener {
        void onCandidateSelected(String candidate, boolean isPrediction);
    }

    public interface AutoCorrectionCallback {
        void onCorrection(String originalWord, String correction);
    }

    private static final String PREF_AUTOCORRECT_ENABLED = "pref_autocorrect_enabled";
    private static final String PREF_LEARNING_ENABLED = "pref_autocorrect_learning_enabled";
    private static final String PREF_CUSTOM_WORDS = "pref_autocorrect_custom_words";
    private static final String PREF_LEARNED_WORDS = "autocorrect_learned_words";
    private static final String PREF_COUNT_PREFIX = "autocorrect_count:";
    private static final String PREF_BIGRAM_PREFIX = "autocorrect_bigram:";

    private final Context mContext;
    private final SharedPreferences mPreferences;
    private final LinearLayout mStrip;
    private final TextView[] mCandidateViews;
    private final CandidateClickListener mClickListener;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    private SpellCheckerSession mSpellCheckerSession;
    private String mRequestedWord = "";
    private String mAutoCorrection;
    private boolean mAutoCorrectEnabled = true;
    private boolean mPredictionMode;
    private String mPreviousCommittedWord = "";
    private int mRequestSequence;
    private String mPendingCorrectionWord;
    private int mPendingCorrectionSequence;
    private AutoCorrectionCallback mPendingCorrectionCallback;

    public AutocorrectBarController(Context context, View root,
            CandidateClickListener clickListener, Locale locale) {
        mContext = context.getApplicationContext();
        mPreferences = PreferenceManagerCompat.getDeviceSharedPreferences(mContext);
        mStrip = root.findViewById(nabu.iris.keyboard.R.id.autocorrect_suggestion_strip);
        mCandidateViews = new TextView[] {
                root.findViewById(nabu.iris.keyboard.R.id.autocorrect_candidate_1),
                root.findViewById(nabu.iris.keyboard.R.id.autocorrect_candidate_2),
                root.findViewById(nabu.iris.keyboard.R.id.autocorrect_candidate_3)
        };
        mClickListener = clickListener;
        for (int i = 0; i < mCandidateViews.length; i++) {
            final int index = i;
            mCandidateViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CharSequence value = mCandidateViews[index].getText();
                    if (value != null && value.length() > 0 && mClickListener != null) {
                        String candidate = value.toString();
                        if (!mPredictionMode && !candidate.equalsIgnoreCase(mRequestedWord)) {
                            learnWord(candidate);
                        }
                        mClickListener.onCandidateSelected(candidate, mPredictionMode);
                    }
                }
            });
        }
        mPreviousCommittedWord = mPreferences.getString("autocorrect_last_word", "");
        openSpellChecker(locale);
        hide();
    }

    private void openSpellChecker(Locale locale) {
        closeSpellChecker();
        try {
            TextServicesManager manager =
                    (TextServicesManager) mContext.getSystemService(Context.TEXT_SERVICES_MANAGER_SERVICE);
            if (manager != null && manager.getCurrentSpellCheckerInfo() != null) {
                mSpellCheckerSession = manager.newSpellCheckerSession(
                        null, locale == null ? Locale.getDefault() : locale, this, true);
            }
        } catch (RuntimeException ignored) {
            mSpellCheckerSession = null;
        }
    }

    public void refresh(String textBeforeCursor, boolean fieldAllowsSuggestions,
            boolean fieldAllowsAutoCorrection) {
        mAutoCorrectEnabled = mPreferences.getBoolean(PREF_AUTOCORRECT_ENABLED, true)
                && fieldAllowsAutoCorrection;

        if (!fieldAllowsSuggestions || !mPreferences.getBoolean(PREF_AUTOCORRECT_ENABLED, true)) {
            hide();
            return;
        }

        // Keep the suggestion row's height stable while typing. Hiding it between
        // words makes the utility toolbar jump vertically on every Space press.
        if (mStrip != null) mStrip.setVisibility(View.VISIBLE);
        if (textBeforeCursor == null || textBeforeCursor.length() == 0) {
            showEmptyStrip();
            return;
        }

        String trimmed = textBeforeCursor;
        if (Character.isWhitespace(trimmed.charAt(trimmed.length() - 1))) {
            String previous = lastWord(trimmed.substring(0, trimmed.length() - 1));
            if (previous.length() > 0) {
                showPredictions(previous);
            } else {
                showEmptyStrip();
            }
            return;
        }

        String word = lastWord(trimmed);
        if (word.length() < 2) {
            showEmptyStrip();
            return;
        }

        mPredictionMode = false;
        if (word.equals(mRequestedWord)) {
            return;
        }
        mRequestedWord = word;
        mAutoCorrection = null;
        int sequence = ++mRequestSequence;

        ArrayList<String> candidates = new ArrayList<>();
        candidates.add(word);
        candidates.addAll(getPrefixCandidates(word));
        if (isKnownWord(word)) {
            showCandidates(candidates);
            return;
        }

        showCandidates(candidates);
        if (mSpellCheckerSession != null) {
            try {
                mSpellCheckerSession.getSentenceSuggestions(
                        new TextInfo[] { new TextInfo(word, 0, sequence) }, 5);
            } catch (RuntimeException ignored) {
                // Keep the typed word visible if the system spell checker is unavailable.
            }
        }
    }

    private void showPredictions(String previousWord) {
        mPredictionMode = true;
        mRequestedWord = "";
        mAutoCorrection = null;
        ArrayList<String> predictions = new ArrayList<>();
        Map<String, ?> all = mPreferences.getAll();
        String prefix = PREF_BIGRAM_PREFIX + previousWord.toLowerCase(Locale.ROOT) + ":";
        ArrayList<Map.Entry<String, ?>> matches = new ArrayList<>();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            if (entry.getKey().startsWith(prefix) && entry.getValue() instanceof Integer) {
                matches.add(entry);
            }
        }
        Collections.sort(matches, new Comparator<Map.Entry<String, ?>>() {
            @Override
            public int compare(Map.Entry<String, ?> left, Map.Entry<String, ?> right) {
                return ((Integer) right.getValue()).compareTo((Integer) left.getValue());
            }
        });
        for (Map.Entry<String, ?> entry : matches) {
            String candidate = entry.getKey().substring(prefix.length());
            if (!candidate.isEmpty() && !predictions.contains(candidate)) {
                predictions.add(candidate);
            }
            if (predictions.size() == 3) break;
        }

        // Until a user has enough bigram history, use locally learned frequent
        // words as useful next-word candidates rather than collapsing the strip.
        if (predictions.isEmpty()) {
            ArrayList<Map.Entry<String, ?>> frequent = new ArrayList<>();
            for (Map.Entry<String, ?> entry : all.entrySet()) {
                if (entry.getKey().startsWith(PREF_COUNT_PREFIX)
                        && entry.getValue() instanceof Integer) {
                    frequent.add(entry);
                }
            }
            Collections.sort(frequent, new Comparator<Map.Entry<String, ?>>() {
                @Override
                public int compare(Map.Entry<String, ?> left, Map.Entry<String, ?> right) {
                    return ((Integer) right.getValue()).compareTo((Integer) left.getValue());
                }
            });
            for (Map.Entry<String, ?> entry : frequent) {
                String candidate = entry.getKey().substring(PREF_COUNT_PREFIX.length());
                if (!candidate.equalsIgnoreCase(previousWord) && !predictions.contains(candidate)) {
                    predictions.add(candidate);
                }
                if (predictions.size() == 3) break;
            }
        }
        showCandidates(predictions);
    }

    @Override
    public void onGetSentenceSuggestions(final SentenceSuggestionsInfo[] results) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (results == null || results.length == 0 || results[0] == null) {
                    completePendingCorrection(null);
                    return;
                }
                SentenceSuggestionsInfo sentence = results[0];
                if (sentence.getSuggestionsCount() == 0) {
                    completePendingCorrection(null);
                    return;
                }
                SuggestionsInfo suggestions = sentence.getSuggestionsInfoAt(0);
                if (suggestions == null) {
                    completePendingCorrection(null);
                    return;
                }

                // A Space key can arrive before the asynchronous spell-check result.
                // Resolve that just-completed word independently of the live toolbar
                // request, which may already have switched to next-word prediction.
                if (mPendingCorrectionCallback != null
                        && suggestions.getSequence() == mPendingCorrectionSequence) {
                    String pendingWord = mPendingCorrectionWord;
                    String pendingCorrection = firstUsableSpellSuggestion(suggestions, pendingWord);
                    completePendingCorrection(pendingCorrection);
                }

                if (mPredictionMode || mRequestedWord.isEmpty()
                        || suggestions.getSequence() != mRequestSequence) {
                    return;
                }

                ArrayList<String> candidates = new ArrayList<>();
                candidates.add(mRequestedWord);
                String firstSpellSuggestion = null;
                for (int i = 0; i < suggestions.getSuggestionsCount(); i++) {
                    String suggestion = suggestions.getSuggestionAt(i);
                    if (suggestion == null || suggestion.trim().isEmpty()
                            || suggestion.equalsIgnoreCase(mRequestedWord)
                            || isKnownWord(suggestion)) {
                        continue;
                    }
                    if (!candidates.contains(suggestion)) candidates.add(suggestion);
                    if (firstSpellSuggestion == null) firstSpellSuggestion = suggestion;
                }

                for (String prefixCandidate : getPrefixCandidates(mRequestedWord)) {
                    if (!candidates.contains(prefixCandidate)) candidates.add(prefixCandidate);
                }

                // Android spell-checkers disagree on which result flags they set. A
                // high-confidence, close spelling candidate is still suitable for
                // correction on Space even when those optional flags are absent.
                if (mAutoCorrectEnabled
                        && firstSpellSuggestion != null
                        && editDistance(mRequestedWord.toLowerCase(Locale.ROOT),
                                firstSpellSuggestion.toLowerCase(Locale.ROOT))
                                <= maximumCorrectionDistance(mRequestedWord)
                        && !startsWithUppercase(mRequestedWord)) {
                    mAutoCorrection = firstSpellSuggestion;
                } else {
                    mAutoCorrection = null;
                }
                showCandidates(candidates);
            }
        });
    }

    @Override
    public void onGetSuggestions(SuggestionsInfo[] results) {
        // Sentence suggestions are used so that candidate offsets remain available.
    }

    /**
     * Requests a fresh spell-check result for a word whose Space key was already
     * pressed. Android spell-checkers respond asynchronously, so the caller must
     * verify the editor still ends with the original word and separator.
     */
    public void requestCorrection(String word, AutoCorrectionCallback callback) {
        if (callback == null) return;
        if (mSpellCheckerSession == null || word == null || word.isEmpty()
                || !mAutoCorrectEnabled) {
            callback.onCorrection(word, null);
            return;
        }
        mPendingCorrectionWord = word;
        mPendingCorrectionCallback = callback;
        mPendingCorrectionSequence = ++mRequestSequence;
        try {
            mSpellCheckerSession.getSentenceSuggestions(
                    new TextInfo[] { new TextInfo(word, 0, mPendingCorrectionSequence) }, 5);
        } catch (RuntimeException ignored) {
            completePendingCorrection(null);
        }
    }

    private String firstUsableSpellSuggestion(SuggestionsInfo suggestions, String word) {
        if (suggestions == null || word == null || word.isEmpty()) return null;
        for (int i = 0; i < suggestions.getSuggestionsCount(); i++) {
            String candidate = suggestions.getSuggestionAt(i);
            if (candidate == null || candidate.trim().isEmpty()
                    || candidate.equalsIgnoreCase(word) || isKnownWord(candidate)) {
                continue;
            }
            if (editDistance(word.toLowerCase(Locale.ROOT),
                    candidate.toLowerCase(Locale.ROOT)) <= maximumCorrectionDistance(word)
                    && !startsWithUppercase(word)) {
                return candidate;
            }
        }
        return null;
    }

    private void completePendingCorrection(String correction) {
        AutoCorrectionCallback callback = mPendingCorrectionCallback;
        String word = mPendingCorrectionWord;
        mPendingCorrectionCallback = null;
        mPendingCorrectionWord = null;
        mPendingCorrectionSequence = 0;
        if (callback != null) callback.onCorrection(word, correction);
    }

    static int maximumCorrectionDistance(String word) {
        if (word == null) return 0;
        return word.length() >= 8 ? 3 : 2;
    }

    public String getAutoCorrection(String word) {
        if (!mAutoCorrectEnabled || word == null || !word.equals(mRequestedWord)
                || isKnownWord(word)) {
            return null;
        }
        return mAutoCorrection;
    }

    public void recordCommittedWord(String word) {
        if (word == null || word.trim().isEmpty()) return;
        String normalized = word.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() < 2) return;

        if (mPreferences.getBoolean(PREF_LEARNING_ENABLED, true)) {
            int count = mPreferences.getInt(PREF_COUNT_PREFIX + normalized, 0) + 1;
            mPreferences.edit().putInt(PREF_COUNT_PREFIX + normalized, count).apply();
            if (count >= 2) learnWord(normalized);
            if (!mPreviousCommittedWord.isEmpty()
                    && !mPreviousCommittedWord.equals(normalized)) {
                String key = PREF_BIGRAM_PREFIX + mPreviousCommittedWord + ":" + normalized;
                int pairCount = mPreferences.getInt(key, 0) + 1;
                mPreferences.edit().putInt(key, pairCount).apply();
            }
        }
        mPreviousCommittedWord = normalized;
        mPreferences.edit().putString("autocorrect_last_word", normalized).apply();
    }

    private void learnWord(String word) {
        if (word == null || word.trim().isEmpty()) return;
        Set<String> words = new HashSet<>(mPreferences.getStringSet(PREF_LEARNED_WORDS,
                Collections.<String>emptySet()));
        words.add(word.trim().toLowerCase(Locale.ROOT));
        mPreferences.edit().putStringSet(PREF_LEARNED_WORDS, words).apply();
    }

    private boolean isKnownWord(String word) {
        String normalized = word.toLowerCase(Locale.ROOT);
        Set<String> learned = mPreferences.getStringSet(PREF_LEARNED_WORDS,
                Collections.<String>emptySet());
        if (learned.contains(normalized)) return true;

        String custom = mPreferences.getString(PREF_CUSTOM_WORDS, "");
        if (custom.isEmpty()) return false;
        String[] entries = custom.split("[,;\\n\\r]+");
        for (String entry : entries) {
            if (normalized.equals(entry.trim().toLowerCase(Locale.ROOT))) return true;
        }
        return false;
    }

    private List<String> getPrefixCandidates(String prefix) {
        ArrayList<String> candidates = new ArrayList<>();
        String normalizedPrefix = prefix.toLowerCase(Locale.ROOT);
        Map<String, ?> all = mPreferences.getAll();
        ArrayList<Map.Entry<String, ?>> matches = new ArrayList<>();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            if (entry.getKey().startsWith(PREF_COUNT_PREFIX)
                    && entry.getValue() instanceof Integer) {
                String word = entry.getKey().substring(PREF_COUNT_PREFIX.length());
                if (word.startsWith(normalizedPrefix) && !word.equals(normalizedPrefix)) {
                    matches.add(entry);
                }
            }
        }
        Collections.sort(matches, new Comparator<Map.Entry<String, ?>>() {
            @Override
            public int compare(Map.Entry<String, ?> left, Map.Entry<String, ?> right) {
                return ((Integer) right.getValue()).compareTo((Integer) left.getValue());
            }
        });
        for (Map.Entry<String, ?> entry : matches) {
            String word = entry.getKey().substring(PREF_COUNT_PREFIX.length());
            if (!candidates.contains(word)) candidates.add(word);
            if (candidates.size() == 2) return candidates;
        }

        Set<String> learned = mPreferences.getStringSet(PREF_LEARNED_WORDS,
                Collections.<String>emptySet());
        for (String word : learned) {
            if (word.startsWith(normalizedPrefix) && !word.equals(normalizedPrefix)
                    && !candidates.contains(word)) {
                candidates.add(word);
            }
            if (candidates.size() == 2) return candidates;
        }

        String custom = mPreferences.getString(PREF_CUSTOM_WORDS, "");
        if (!custom.isEmpty()) {
            String[] customWords = custom.split("[,;\\n\\r]+");
            for (String entry : customWords) {
                String word = entry.trim();
                String normalized = word.toLowerCase(Locale.ROOT);
                if (normalized.startsWith(normalizedPrefix) && !normalized.equals(normalizedPrefix)
                        && !candidates.contains(word)) {
                    candidates.add(word);
                }
                if (candidates.size() == 2) break;
            }
        }
        return candidates;
    }

    private void showEmptyStrip() {
        if (mStrip == null) return;
        for (TextView view : mCandidateViews) {
            view.setText("");
            view.setVisibility(View.INVISIBLE);
        }
        mStrip.setVisibility(View.VISIBLE);
    }

    private void showCandidates(List<String> candidates) {
        if (mStrip == null) return;
        for (int i = 0; i < mCandidateViews.length; i++) {
            TextView view = mCandidateViews[i];
            if (i < candidates.size()) {
                view.setText(candidates.get(i));
                view.setVisibility(View.VISIBLE);
                view.setTypeface(null, i == 1 ? Typeface.BOLD : Typeface.NORMAL);
            } else {
                view.setText("");
                view.setVisibility(View.INVISIBLE);
            }
        }
        mStrip.setVisibility(View.VISIBLE);
    }

    private void hide() {
        mRequestSequence++;
        mRequestedWord = "";
        mAutoCorrection = null;
        if (mStrip != null) mStrip.setVisibility(View.GONE);
    }

    static String lastWord(String text) {
        if (text == null || text.isEmpty()) return "";
        int end = text.length();
        int start = end;
        while (start > 0) {
            int codePoint = text.codePointBefore(start);
            if (!Character.isLetterOrDigit(codePoint) && codePoint != '\'' && codePoint != 0x2019) {
                break;
            }
            start -= Character.charCount(codePoint);
        }
        return text.substring(start, end);
    }

    private static boolean startsWithUppercase(String word) {
        return !word.isEmpty() && Character.isUpperCase(word.codePointAt(0));
    }

    static int editDistance(String left, String right) {
        int[] previous = new int[right.length() + 1];
        int[] current = new int[right.length() + 1];
        for (int j = 0; j <= right.length(); j++) previous[j] = j;
        for (int i = 1; i <= left.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= right.length(); j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1,
                        previous[j] + 1), previous[j - 1] + cost);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[right.length()];
    }

    public void setLocale(Locale locale) {
        openSpellChecker(locale);
        mRequestedWord = "";
        mAutoCorrection = null;
    }

    private void closeSpellChecker() {
        if (mSpellCheckerSession != null) {
            try {
                mSpellCheckerSession.close();
            } catch (RuntimeException ignored) {
            }
            mSpellCheckerSession = null;
        }
    }

    public void destroy() {
        closeSpellChecker();
    }
}
