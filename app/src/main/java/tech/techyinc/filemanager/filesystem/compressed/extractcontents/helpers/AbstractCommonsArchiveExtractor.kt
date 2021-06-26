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

package tech.techyinc.filemanager.filesystem.compressed.extractcontents.helpers

import android.content.Context
import tech.techyinc.filemanager.R
import tech.techyinc.filemanager.application.AppConfig
import tech.techyinc.filemanager.file_operations.utils.UpdatePosition
import tech.techyinc.filemanager.filesystem.FileUtil
import tech.techyinc.filemanager.filesystem.MakeDirectoryOperation
import tech.techyinc.filemanager.filesystem.compressed.extractcontents.Extractor
import tech.techyinc.filemanager.filesystem.files.GenericCopyUtil
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import java.io.*
import java.util.*

abstract class AbstractCommonsArchiveExtractor(
    context: Context,
    filePath: String,
    outputPath: String,
    listener: OnUpdate,
    updatePosition: UpdatePosition
) : Extractor(context, filePath, outputPath, listener, updatePosition) {

    /**
     * Subclasses implement this method to create [ArchiveInputStream] instances with given archive
     * as [InputStream].
     *
     * @param inputStream archive as [InputStream]
     */
    abstract fun createFrom(inputStream: InputStream): ArchiveInputStream

    @Throws(IOException::class)
    @Suppress("EmptyWhileBlock")
    override fun extractWithFilter(filter: Filter) {
        var totalBytes: Long = 0
        val archiveEntries = ArrayList<ArchiveEntry>()
        var inputStream = createFrom(FileInputStream(filePath))
        var archiveEntry: ArchiveEntry?
        while (inputStream.nextEntry.also { archiveEntry = it } != null) {
            archiveEntry?.run {
                if (filter.shouldExtract(name, isDirectory)) {
                    archiveEntries.add(this)
                    totalBytes += size
                }
            }
        }
        listener.onStart(totalBytes, archiveEntries[0].name)
        inputStream.close()
        inputStream = createFrom(FileInputStream(filePath))
        archiveEntries.forEach { entry ->
            if (!listener.isCancelled) {
                listener.onUpdate(entry.name)
                // TAR is sequential, you need to walk all the way to the file you want
                while (entry.hashCode() != inputStream.nextEntry.hashCode()) {}
                extractEntry(context, inputStream, entry, outputPath)
            }
        }
        inputStream.close()
        listener.onFinish()
    }

    @Throws(IOException::class)
    protected fun extractEntry(
        context: Context,
        inputStream: ArchiveInputStream,
        entry: ArchiveEntry,
        outputDir: String
    ) {
        if (entry.isDirectory) {
            MakeDirectoryOperation.mkdir(File(outputDir, entry.name), context)
            return
        }
        val outputFile = File(outputDir, entry.name)
        if (!outputFile.parentFile.exists()) {
            MakeDirectoryOperation.mkdir(outputFile.parentFile, context)
        }
        FileUtil.getOutputStream(outputFile, context)?.let { fileOutputStream ->
            BufferedOutputStream(fileOutputStream).run {
                var len: Int
                val buf = ByteArray(GenericCopyUtil.DEFAULT_BUFFER_SIZE)
                while (inputStream.read(buf).also { len = it } != -1) {
                    write(buf, 0, len)
                    updatePosition.updatePosition(len.toLong())
                }
                close()
                outputFile.setLastModified(entry.lastModifiedDate.time)
            }
        } ?: AppConfig.toast(
            context,
            context.getString(
                R.string.error_archive_cannot_extract,
                entry.name,
                outputDir
            )
        )
    }
}
