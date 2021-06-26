/*
 * Copyright (C) 2014-2021 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>,
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

package tech.techyinc.filemanager.adapters.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tech.techyinc.filemanager.R
import tech.techyinc.filemanager.ui.views.ThemedTextView

class CompressedItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    // each data item is just a string in this case
    @JvmField
    val pictureIcon: ImageView = view.findViewById(R.id.picture_icon)

    @JvmField
    val genericIcon: ImageView = view.findViewById(R.id.generic_icon)

    @JvmField
    val apkIcon: ImageView = view.findViewById(R.id.apk_icon)

    @JvmField
    val txtTitle: ThemedTextView = view.findViewById(R.id.firstline)

    @JvmField
    val txtDesc: TextView = view.findViewById(R.id.secondLine)

    @JvmField
    val date: TextView = view.findViewById(R.id.date)

    val perm: TextView = view.findViewById(R.id.permis)

    @JvmField
    val rl: View = view.findViewById(R.id.second)

    @JvmField
    val checkImageView: ImageView = view.findViewById(R.id.check_icon)
}
