/**
 * The MIT License
 *
 * Copyright (c) 2013, Sitespeed.io organization, Peter Hedenskog 
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.sitespeed.jenkins.util;

import hudson.model.AbstractBuild;
import io.sitespeed.jenkins.SitespeedConstants;


import java.io.File;

import java.util.Arrays;
import java.util.Comparator;


public final class FileUtil {

	private static final FileUtil INSTANCE = new FileUtil();

    /**
     * Create a new utils.
     */
    private FileUtil() {
    }
    
    /**
     * Get the instance.
     * 
     * @return the singleton instance.
     */
    public static FileUtil getInstance() {
            return INSTANCE;
    }

	public File getLastModifiedFileInDir(File dir) {

		if (!dir.isDirectory())
			throw new IllegalArgumentException(dir.getAbsolutePath()
					+ " is not a directory");

		File[] files = dir.listFiles();

		Arrays.sort(files, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return Long.valueOf(f1.lastModified()).compareTo(
						f2.lastModified());
			}
		});

		return files[files.length - 1];

	}
	
	public String getLastModifiedFileNameInDir(File dir) {

		if (!dir.isDirectory())
			throw new IllegalArgumentException(dir.getAbsolutePath()
					+ " is not a directory");

		File[] files = dir.listFiles();

		Arrays.sort(files, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return Long.valueOf(f1.lastModified()).compareTo(
						f2.lastModified());
			}
		});

		return files[files.length - 1].getName();

	}
	
	public File getSitespeedOutputDir(AbstractBuild<?, ?> build) {

		// Get the output result dir, right now we use the workspace
		// TODO make configurable and check for the -r parameter in the input

		return new File(build.getWorkspace().toString() + "/" + build.getNumber() + "/"
				+ SitespeedConstants.DEFAULT_OUTPUT_DIR);
	}

	
}
