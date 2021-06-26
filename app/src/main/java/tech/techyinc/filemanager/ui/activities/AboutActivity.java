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

import static tech.techyinc.filemanager.utils.Utils.openURL;

import tech.techyinc.filemanager.BuildConfig;
import tech.techyinc.filemanager.LogHelper;
import tech.techyinc.filemanager.R;
import tech.techyinc.filemanager.ui.activities.superclasses.BasicActivity;
import tech.techyinc.filemanager.ui.theme.AppTheme;
import tech.techyinc.filemanager.utils.Billing;
import tech.techyinc.filemanager.utils.Utils;

import com.cloudrail.si.servicecode.commands.Return;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.palette.graphics.Palette;
import androidx.preference.PreferenceManager;

/** Created by vishal on 27/7/16. */
public class AboutActivity extends BasicActivity implements View.OnClickListener {

  private static final String TAG = "AboutActivity";

  private static final int HEADER_HEIGHT = 1024;
  private static final int HEADER_WIDTH = 500;

  private AppBarLayout mAppBarLayout;
  private CollapsingToolbarLayout mCollapsingToolbarLayout;
  private TextView mTitleTextView;
  private int mCount = 0;
  private Snackbar snackbar;
  private SharedPreferences mSharedPref;
  private Billing billing;
  private int primaryOrange = R.color.primary_orange;

  private static final String KEY_PREF_STUDIO = "studio";
  private static final String URL_REPO_SHARE =
      "market://details?id=tech.techyinc.filemanager";

  private static final String URL_REPO = "https://github.com/amanv4/vxfilemanager";
  private static final String URL_REPO_TRANSLATE =
      "https://localazy.com/p/vx-file-manager/";
  private static final String URL_REPO_RATE = "market://details?id=tech.techyinc.filemanager";

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getAppTheme().equals(AppTheme.DARK)) {
      setTheme(R.style.aboutDark);
    } else if (getAppTheme().equals(AppTheme.BLACK)) {
      setTheme(R.style.aboutBlack);
    } else {
      setTheme(R.style.aboutLight);
    }

    setContentView(R.layout.activity_about);

    mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);

    mAppBarLayout = findViewById(R.id.appBarLayout);
    mCollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar_layout);
    mTitleTextView = findViewById(R.id.text_view_title);

    mAppBarLayout.setLayoutParams(calculateHeaderViewParams());

    Toolbar mToolbar = findViewById(R.id.toolBar);
    setSupportActionBar(mToolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.md_nav_back));
    getSupportActionBar().setDisplayShowTitleEnabled(false);

    switchIcons();

    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.about_header);

    // It will generate colors based on the image in an AsyncTask.
    Palette.from(bitmap)
        .generate(
            palette -> {
              int mutedColor =
                  palette.getMutedColor(Utils.getColor(AboutActivity.this, R.color.primary_orange));
              int darkMutedColor =
                  palette.getDarkMutedColor(
                      Utils.getColor(AboutActivity.this, R.color.primary_orange));
              mCollapsingToolbarLayout.setContentScrimColor(mutedColor);
              mCollapsingToolbarLayout.setStatusBarScrimColor(darkMutedColor);
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(darkMutedColor);
              }
            });

    mAppBarLayout.addOnOffsetChangedListener(
        (appBarLayout, verticalOffset) -> {
          mTitleTextView.setAlpha(
              Math.abs(verticalOffset / (float) appBarLayout.getTotalScrollRange()));
        });
    mAppBarLayout.setOnFocusChangeListener(
        (v, hasFocus) -> {
          mAppBarLayout.setExpanded(hasFocus, true);
        });
  }

  /**
   * Calculates aspect ratio for the Amaze header
   *
   * @return the layout params with new set of width and height attribute
   */
  private CoordinatorLayout.LayoutParams calculateHeaderViewParams() {

    // calculating cardview height as per the youtube video thumb aspect ratio
    CoordinatorLayout.LayoutParams layoutParams =
        (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
    float vidAspectRatio = (float) HEADER_WIDTH / (float) HEADER_HEIGHT;
    Log.d(TAG, vidAspectRatio + "");
    int screenWidth = getResources().getDisplayMetrics().widthPixels;
    float reqHeightAsPerAspectRatio = (float) screenWidth * vidAspectRatio;
    Log.d(TAG, reqHeightAsPerAspectRatio + "");

    Log.d(TAG, "new width: " + screenWidth + " and height: " + reqHeightAsPerAspectRatio);

    layoutParams.width = screenWidth;
    layoutParams.height = (int) reqHeightAsPerAspectRatio;
    return layoutParams;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  /** Method switches icon resources as per current theme */
  private void switchIcons() {
    if (getAppTheme().equals(AppTheme.DARK) || getAppTheme().equals(AppTheme.BLACK)) {

    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.relative_layout_version:
        mCount++;
        if (mCount >= 5) {
          String text = getResources().getString(R.string.easter_egg_title) + " : " + mCount;

          if (snackbar != null && snackbar.isShown()) {
            snackbar.setText(text);
          } else {
            snackbar = Snackbar.make(v, text, Snackbar.LENGTH_SHORT);
          }

          snackbar.show();
          mSharedPref
              .edit()
              .putInt(KEY_PREF_STUDIO, Integer.parseInt(Integer.toString(mCount) + "000"))
              .apply();
        } else {
          mSharedPref.edit().putInt(KEY_PREF_STUDIO, 0).apply();
        }
        break;


      case R.id.relative_layout_issues:
        final Intent i = Utils.buildEmailIntent("VX File Manager: Issue Report", Utils.EMAIL_NOREPLY_REPORTS);
        if (i.resolveActivity(getPackageManager()) != null) {
          startActivity(i);
        }
        break;

      case R.id.relative_layout_share:
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, "VX File Manager");
        String shareMessage = "\nLet me recommend you this application\n\n";
        shareMessage = shareMessage + "market://details?id=" + BuildConfig.APPLICATION_ID+ "\n";
        share.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(share, "Share via"));
        break;

      case R.id.relative_layout_licenses:
        LibsBuilder libsBuilder =
            new LibsBuilder()
                .withLibraries("apachemina") // Not auto-detected for some reason
                .withActivityTitle(getString(R.string.libraries))
                .withAboutIconShown(true)
                .withAboutVersionShownName(true)
                .withAboutVersionShownCode(false)
                .withAboutDescription(getString(R.string.about_amaze))
                .withAboutSpecial1(getString(R.string.license))
                .withAboutSpecial1Description(getString(R.string.amaze_license))
                .withLicenseShown(true);

        switch (getAppTheme().getSimpleTheme()) {
          case LIGHT:
            libsBuilder.withActivityTheme(R.style.AboutLibrariesTheme_Light);
            break;
          case DARK:
            libsBuilder.withActivityTheme(R.style.AboutLibrariesTheme_Dark);
            break;
          case BLACK:
            libsBuilder.withActivityTheme(R.style.AboutLibrariesTheme_Black);
            break;
          default:
            LogHelper.logOnProductionOrCrash(TAG, "Incorrect value for switch");
        }

        libsBuilder.start(this);

        break;

      case R.id.relative_layout_rate:
        try {
          startActivity(new Intent(Intent.ACTION_VIEW,
                  Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)));
        } catch (android.content.ActivityNotFoundException e) {
          startActivity(new Intent(Intent.ACTION_VIEW,
                  Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
        }
        break;
      case R.id.relative_layout_source:
        openURL(URL_REPO, this);
        break;
      case R.id.relative_layout_translate:
        openURL(URL_REPO_TRANSLATE, this);
        break;
      case R.id.relative_layout_donate:
        billing = new Billing(this);
        break;
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "Destroying the manager.");
    if (billing != null) {
      billing.destroyBillingInstance();
    }
  }
}
