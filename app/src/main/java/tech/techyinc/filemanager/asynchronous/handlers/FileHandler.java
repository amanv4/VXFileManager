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

package tech.techyinc.filemanager.asynchronous.handlers;

import java.io.File;
import java.lang.ref.WeakReference;

import tech.techyinc.filemanager.adapters.RecyclerAdapter;
import tech.techyinc.filemanager.filesystem.CustomFileObserver;
import tech.techyinc.filemanager.filesystem.HybridFile;
import tech.techyinc.filemanager.ui.fragments.MainFragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/** @author Emmanuel on 8/11/2017, at 17:37. */
public class FileHandler extends Handler {
  private WeakReference<MainFragment> mainFragment;
  private RecyclerView listView;
  private boolean useThumbs;

  public FileHandler(MainFragment mainFragment, RecyclerView listView, boolean useThumbs) {
    super(Looper.getMainLooper());
    this.mainFragment = new WeakReference<>(mainFragment);
    this.listView = listView;
    this.useThumbs = useThumbs;
  }

  @Override
  public void handleMessage(Message msg) {
    super.handleMessage(msg);
    final MainFragment main = mainFragment.get();

    if (main == null || main.getActivity() == null || main.getMainFragmentViewModel() == null) {
      return;
    }

    String path = (String) msg.obj;

    switch (msg.what) {
      case CustomFileObserver.GOBACK:
        main.goBack();
        break;
      case CustomFileObserver.NEW_ITEM:
        HybridFile fileCreated =
            new HybridFile(
                main.getMainFragmentViewModel().getOpenMode(), main.getCurrentPath() + "/" + path);
        main.getElementsList().add(fileCreated.generateLayoutElement(main.getContext(), useThumbs));
        break;
      case CustomFileObserver.DELETED_ITEM:
        for (int i = 0; i < main.getElementsList().size(); i++) {
          File currentFile = new File(main.getElementsList().get(i).desc);

          if (currentFile.getName().equals(path)) {
            main.getElementsList().remove(i);
            break;
          }
        }
        break;
      default: // Pass along other messages from the UI
        super.handleMessage(msg);
        return;
    }

    if (listView.getVisibility() == View.VISIBLE) {
      if (main.getElementsList().size() == 0) {
        // no item left in list, recreate views
        main.reloadListElements(
            true,
            main.getMainFragmentViewModel().getResults(),
            !main.getMainFragmentViewModel().isList());
      } else {
        // we already have some elements in list view, invalidate the adapter
        ((RecyclerAdapter) listView.getAdapter()).setItems(listView, main.getElementsList());
      }
    } else {
      // there was no list view, means the directory was empty
      main.loadlist(main.getCurrentPath(), true, main.getMainFragmentViewModel().getOpenMode());
    }

    main.computeScroll();
  }
}
