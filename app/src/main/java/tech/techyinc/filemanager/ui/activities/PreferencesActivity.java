/*
 * Copyright (C) 2014-2020 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>,
 * Emmanuel Messulam<emmanuelbendavid@gmail.com>, Raymond Lai <airwave209gt at gmail.com> and Contributors.
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tech.techyinc.filemanager.ui.activities;

import static android.os.Build.VERSION.SDK_INT;

import java.io.File;

import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import tech.techyinc.filemanager.R;
import tech.techyinc.filemanager.ui.activities.superclasses.ThemedActivity;
import tech.techyinc.filemanager.ui.colors.ColorPreferenceHelper;
import tech.techyinc.filemanager.ui.fragments.preference_fragments.AdvancedSearchPref;
import tech.techyinc.filemanager.ui.fragments.preference_fragments.ColorPref;
import tech.techyinc.filemanager.ui.fragments.preference_fragments.FoldersPref;
import tech.techyinc.filemanager.ui.fragments.preference_fragments.PrefFrag;
import tech.techyinc.filemanager.ui.fragments.preference_fragments.PreferencesConstants;
import tech.techyinc.filemanager.ui.fragments.preference_fragments.QuickAccessPref;
import tech.techyinc.filemanager.ui.theme.AppTheme;
import tech.techyinc.filemanager.utils.PreferenceUtils;
import tech.techyinc.filemanager.utils.Utils;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class PreferencesActivity extends ThemedActivity
    implements FolderChooserDialog.FolderCallback {

  // Start is the first activity you see
  public static final int START_PREFERENCE = 0;
  public static final int COLORS_PREFERENCE = 1;
  public static final int FOLDERS_PREFERENCE = 2;
  public static final int QUICKACCESS_PREFERENCE = 3;
  public static final int ADVANCEDSEARCH_PREFERENCE = 4;

  private boolean restartActivity = false;
  // The preference fragment currently selected
  private int selectedItem = 0;

  private PreferenceFragmentCompat currentFragment;

  private static final String KEY_CURRENT_FRAG_OPEN = "current_frag_open";
  private static final int NUMBER_OF_PREFERENCES = 5;
  private AdView mAdView;
  private static final String AD_UNIT_ID = "YOUR_ADMOB_INTERSTITIAL_ID";
  private static final String TAG = "PreferencesActivity";

  private InterstitialAd interstitialAd;

  private Parcelable[] fragmentsListViewParcelables = new Parcelable[NUMBER_OF_PREFERENCES];

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.prefsfrag);
    Toolbar toolbar = findViewById(R.id.toolbar);
    invalidateRecentsColorAndIcon();
    setSupportActionBar(toolbar);
    getSupportActionBar()
        .setDisplayOptions(
            androidx.appcompat.app.ActionBar.DISPLAY_HOME_AS_UP
                | androidx.appcompat.app.ActionBar.DISPLAY_SHOW_TITLE);

    if (savedInstanceState != null) {
      selectedItem = savedInstanceState.getInt(KEY_CURRENT_FRAG_OPEN, 0);
    } else if (getIntent().getExtras() != null) {
      selectItem(getIntent().getExtras().getInt(KEY_CURRENT_FRAG_OPEN));
    } else {
      selectItem(0);
    }
    initStatusBarResources(findViewById(R.id.preferences));
    mAdView = findViewById(R.id.adView);
    AdRequest adRequest = new AdRequest.Builder().build();
    mAdView.loadAd(adRequest);
    loadAd();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(KEY_CURRENT_FRAG_OPEN, selectedItem);
  }

  @Override
  public void onBackPressed() {
    if (currentFragment instanceof ColorPref) {
      if (((ColorPref) currentFragment).onBackPressed()) return;
    }

    if (selectedItem != START_PREFERENCE && restartActivity) {
      restartActivity(this);
    } else if (selectedItem != START_PREFERENCE) {
      selectItem(START_PREFERENCE);
    } else {
      Intent in = new Intent(PreferencesActivity.this, MainActivity.class);
      in.setAction(Intent.ACTION_MAIN);
      in.setAction(Intent.CATEGORY_LAUNCHER);
      this.startActivity(in);
      this.finish();
    }
    showInterstitial();
  }

  public void loadAd() {
    AdRequest adRequest = new AdRequest.Builder().build();
    InterstitialAd.load(
            this,
            AD_UNIT_ID,
            adRequest,
            new InterstitialAdLoadCallback() {
              @Override
              public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                PreferencesActivity.this.interstitialAd = interstitialAd;
                Log.i(TAG, "onAdLoaded");
                interstitialAd.setFullScreenContentCallback(
                        new FullScreenContentCallback() {
                          @Override
                          public void onAdDismissedFullScreenContent() {
                            // Called when fullscreen content is dismissed.
                            // Make sure to set your reference to null so you don't
                            // show it a second time.
                            PreferencesActivity.this.interstitialAd = null;
                            Log.d("TAG", "The ad was dismissed.");
                          }

                          @Override
                          public void onAdFailedToShowFullScreenContent(AdError adError) {
                            // Called when fullscreen content failed to show.
                            // Make sure to set your reference to null so you don't
                            // show it a second time.
                            PreferencesActivity.this.interstitialAd = null;
                            Log.d("TAG", "The ad failed to show.");
                          }

                          @Override
                          public void onAdShowedFullScreenContent() {
                            // Called when fullscreen content is shown.
                            Log.d("TAG", "The ad was shown.");
                          }
                        });
              }

              @Override
              public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Log.i(TAG, loadAdError.getMessage());
                interstitialAd = null;

                String error =
                        String.format(
                                "domain: %s, code: %d, message: %s",
                                loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
                Toast.makeText(
                        PreferencesActivity.this, "onAdFailedToLoad() with error: " + error, Toast.LENGTH_SHORT)
                        .show();
              }
            });
  }

  private void showInterstitial() {
    // Show the ad if it's ready. Otherwise toast and restart the game.
    if (interstitialAd != null) {
      interstitialAd.show(this);
    } else {
      Log.i(TAG, "onAdLoaded");
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        if (currentFragment.onOptionsItemSelected(item)) return true;

        if (selectedItem != START_PREFERENCE && restartActivity) {
          restartActivity(this);
        } else if (selectedItem != START_PREFERENCE) {
          selectItem(START_PREFERENCE);
        } else {
          Intent in = new Intent(PreferencesActivity.this, MainActivity.class);
          in.setAction(Intent.ACTION_MAIN);
          in.setAction(Intent.CATEGORY_LAUNCHER);

          final int enter_anim = android.R.anim.fade_in;
          final int exit_anim = android.R.anim.fade_out;
          Activity activity = this;
          activity.overridePendingTransition(enter_anim, exit_anim);
          activity.finish();
          activity.overridePendingTransition(enter_anim, exit_anim);
          activity.startActivity(in);
        }
        return true;
    }
    return false;
  }

  /**
   * This is a hack, each PreferenceFragment has a ListView that loses it's state (specifically the
   * scrolled position) when the user accesses another PreferenceFragment. To prevent this, the
   * Activity saves the ListView's state, so that it can be restored when the user returns to the
   * PreferenceFragment.
   *
   * <p>We cannot use the normal save/restore state functions because they only get called when the
   * OS kills the fragment, not the user. See https://stackoverflow.com/a/12793395/3124150 for a
   * better explanation.
   *
   * <p>We cannot save the Parcelable in the fragment because the fragment is destroyed.
   */
  public void saveListViewState(int prefFragment, Parcelable listViewState) {
    fragmentsListViewParcelables[prefFragment] = listViewState;
  }

  /** This is a hack see {@link PreferencesActivity#saveListViewState(int, Parcelable)} */
  public Parcelable restoreListViewState(int prefFragment) {
    return fragmentsListViewParcelables[prefFragment];
  }

  public void setRestartActivity() {
    restartActivity = true;
  }

  public boolean getRestartActivity() {
    return restartActivity;
  }

  public void invalidateRecentsColorAndIcon() {
    if (SDK_INT >= 21) {
      @ColorInt
      int primaryColor =
          ColorPreferenceHelper.getPrimary(getCurrentColorPreference(), MainActivity.currentTab);

      ActivityManager.TaskDescription taskDescription =
          new ActivityManager.TaskDescription(
              "Amaze",
              ((BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher)).getBitmap(),
              primaryColor);
      setTaskDescription(taskDescription);
    }
  }

  public void invalidateToolbarColor() {
    @ColorInt
    int primaryColor =
        ColorPreferenceHelper.getPrimary(getCurrentColorPreference(), MainActivity.currentTab);
    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(primaryColor));
  }

  public void invalidateNavBar() {
    @ColorInt
    int primaryColor =
        ColorPreferenceHelper.getPrimary(getCurrentColorPreference(), MainActivity.currentTab);

    if (SDK_INT == 20 || SDK_INT == 19) {
      SystemBarTintManager tintManager = new SystemBarTintManager(this);
      tintManager.setStatusBarTintEnabled(true);
      tintManager.setStatusBarTintColor(primaryColor);

      FrameLayout.MarginLayoutParams p =
          (ViewGroup.MarginLayoutParams) findViewById(R.id.preferences).getLayoutParams();
      SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
      p.setMargins(0, config.getStatusBarHeight(), 0, 0);
    } else if (SDK_INT >= 21) {
      boolean colourednavigation = getBoolean(PreferencesConstants.PREFERENCE_COLORED_NAVIGATION);
      Window window = getWindow();
      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
      int tabStatusColor = PreferenceUtils.getStatusColor(primaryColor);
      window.setStatusBarColor(tabStatusColor);
      if (colourednavigation) {
        window.setNavigationBarColor(tabStatusColor);
      } else if (window.getNavigationBarColor() != Color.BLACK) {
        window.setNavigationBarColor(Color.BLACK);
      }
    }

    if (getAppTheme().equals(AppTheme.BLACK))
      getWindow().getDecorView().setBackgroundColor(Utils.getColor(this, android.R.color.black));
  }

  /**
   * This 'elegantly' destroys the activity and recreates it so that the different widgets and texts
   * change their inner states's colors.
   */
  public void restartActivity(final Activity activity) {
    if (activity == null) throw new NullPointerException();

    final int enter_anim = android.R.anim.fade_in;
    final int exit_anim = android.R.anim.fade_out;
    activity.overridePendingTransition(enter_anim, exit_anim);
    activity.finish();
    activity.overridePendingTransition(enter_anim, exit_anim);
    if (selectedItem != START_PREFERENCE) {
      Intent i = activity.getIntent();
      i.putExtra(KEY_CURRENT_FRAG_OPEN, selectedItem);
    }
    activity.startActivity(activity.getIntent());
  }

  /**
   * When a Preference (that requires an independent fragment) is selected this is called.
   *
   * @param item the Preference in question
   */
  public void selectItem(int item) {
    selectedItem = item;
    switch (item) {
      case START_PREFERENCE:
        loadPrefFragment(new PrefFrag(), R.string.setting);
        break;
      case COLORS_PREFERENCE:
        loadPrefFragment(new ColorPref(), R.string.color_title);
        break;
      case FOLDERS_PREFERENCE:
        loadPrefFragment(new FoldersPref(), R.string.sidebar_folders_title);
        break;
      case QUICKACCESS_PREFERENCE:
        loadPrefFragment(new QuickAccessPref(), R.string.sidebar_quick_access_title);
        break;
      case ADVANCEDSEARCH_PREFERENCE:
        loadPrefFragment(new AdvancedSearchPref(), R.string.advanced_search);
        break;
    }
  }

  private void loadPrefFragment(PreferenceFragmentCompat fragment, @StringRes int titleBarName) {
    currentFragment = fragment;

    FragmentTransaction t = getSupportFragmentManager().beginTransaction();
    t.replace(R.id.prefsfragment, fragment);
    t.commit();
    getSupportActionBar().setTitle(titleBarName);
  }

  /**
   * Update preference key with selected path.
   *
   * @see PrefFrag
   * @see FolderChooserDialog
   * @see com.afollestad.materialdialogs.folderselector.FolderChooserDialog.FolderCallback
   * @param dialog
   * @param folder selected folder
   */
  @Override
  public void onFolderSelection(@NonNull FolderChooserDialog dialog, @NonNull File folder) {
    // Just to be safe
    if (folder.exists() && folder.isDirectory()) {
      // Write settings to preferences
      SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
      sharedPref.edit().putString(dialog.getTag(), folder.getAbsolutePath()).apply();
    }
    dialog.dismiss();
  }

  /**
   * Do nothing other than dismissing the folder selection dialog.
   *
   * @see FolderChooserDialog
   * @see com.afollestad.materialdialogs.folderselector.FolderChooserDialog.FolderCallback
   * @param dialog
   */
  @Override
  public void onFolderChooserDismissed(@NonNull FolderChooserDialog dialog) {
    dialog.dismiss();
  }
}
