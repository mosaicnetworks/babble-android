/*
 * MIT License
 *
 * Copyright (c) 2018- Mosaic Networks
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.mosaicnetworks.getbabblecore;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.GradleException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import groovy.lang.Closure;

public class GetBabbleCoreAction {

    private String version;
    private String remoteRepo;
    private String localRepo;
    private String protocol;
    private String mDownloadArtifact;
    private String mTmpDirectory;

    public String getVersion() {
        return version;
    }

    public void setVersion(Object version) {
        if (version instanceof Closure) {
            //lazily evaluate closure
            Closure<?> closure = (Closure<?>)version;
            version = closure.call();
        }

        this.version = (String) version;
    }

    public String getRemoteRepo() {
        return remoteRepo;
    }

    public void setRemoteRepo(Object remoteRepo) {
        if (remoteRepo instanceof Closure) {
            //lazily evaluate closure
            Closure<?> closure = (Closure<?>)remoteRepo;
            remoteRepo = closure.call();
        }

        this.remoteRepo = (String) remoteRepo;
    }

    public String getLocalRepo() {
        return localRepo;
    }

    public void setLocalRepo(Object localRepo) {
        if (localRepo instanceof Closure) {
            //lazily evaluate closure
            Closure<?> closure = (Closure<?>)localRepo;
            localRepo = closure.call();
        }

        this.localRepo = (String) localRepo;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(Object protocol) {
        if (protocol instanceof Closure) {
            //lazily evaluate closure
            Closure<?> closure = (Closure<?>)protocol;
            protocol = closure.call();
        }

        this.protocol = (String) protocol;
    }

    public void init() {
        mDownloadArtifact = "babble_" + version + "_android_library.zip";
        mTmpDirectory = localRepo + "/" + version + "/tmp";
    }

    public void execute() {
        init();

        String testFile = localRepo + "/" + version + "/classes.jar";

        if (new File(testFile).isFile()) {
            System.out.println(testFile + " already exists, skipping task getBabbleCore");
            return;
        }

        new File(mTmpDirectory).mkdirs();

        fetch();
        unpack();
        copyDeps();
        cleanup();

    }

    private void fetch() {
        System.out.println("Fetching from remote: " + remoteRepo);

        String dest = mTmpDirectory + "/" + mDownloadArtifact;

        if (protocol.equals("https")) {
            URL url;
            try {
                url = new URL(remoteRepo + "/v" + version + "/" + mDownloadArtifact);
            } catch (MalformedURLException ex) {
                throw new GradleException(ex.getMessage());
            }

            try {
                FileUtils.copyURLToFile(url, new File(dest));
            } catch (IOException ex) {
                throw new GradleException(ex.getMessage());
            }

        } else if (protocol.equals("file")) {

            String source = remoteRepo + "/" + mDownloadArtifact;


            File src = new File(source);
            File target = new File(dest);

            System.out.println("Copying to: " + dest);

            try {
                copyFileUsingStream(src, target);
            } catch (IOException ex) {
                throw new GradleException(ex.getMessage());
            }
        } else {
            throw new GradleException("Unrecognised protocol");
        }
    }

    private void unpack() {

        try {
            unzip(new File(mTmpDirectory + "/" + mDownloadArtifact), mTmpDirectory);
        } catch (IOException ex) {
            throw new GradleException(ex.getMessage());
        }

        new File(mTmpDirectory + "/aar-unzip").mkdirs();

        try {
            unzip(new File(mTmpDirectory + "/" + "babble_" + version + "_mobile.aar" ), mTmpDirectory + "/aar-unzip");
        } catch (IOException ex) {
            throw new GradleException(ex.getMessage());
        }

    }

    private void unzip(File file, String outputDir) throws IOException {
        try (ZipFile zipFile = new ZipFile(file)){
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(outputDir,  entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    InputStream in = zipFile.getInputStream(entry);
                    OutputStream out = new FileOutputStream(entryDestination);
                    IOUtils.copy(in, out);
                    IOUtils.closeQuietly(in);
                    out.close();
                }
            }
        }
    }

    private void copyDeps() {

        //copy classes.jar
        File src = new File(mTmpDirectory + "/aar-unzip/classes.jar");
        File target = new File(localRepo + "/" + version + "/classes.jar");
        try {
            copyFileUsingStream(src, target);
        } catch (IOException ex) {
            throw new GradleException(ex.getMessage());
        }

        //copy jni directory
        File srcJni = new File(mTmpDirectory + "/aar-unzip/jni");
        File targetJni = new File(localRepo + "/" + version + "/jni");
        try {
            FileUtils.copyDirectory(srcJni, targetJni);
        } catch (IOException ex) {
            throw new GradleException(ex.getMessage());
        }

        //copy git.version
        File srcGitVersion = new File(mTmpDirectory + "/git.version");
        File targetGitVersion = new File(localRepo + "/" + version + "/git.version");
        try {
            copyFileUsingStream(srcGitVersion, targetGitVersion);
        } catch (IOException ex) {
            throw new GradleException(ex.getMessage());
        }

    }

    private void cleanup() {
        try {
            FileUtils.deleteDirectory(new File(mTmpDirectory));
        } catch (IOException ex) {
            new GradleException(ex.getMessage());
        }
    }

    private static void copyFileUsingStream(File source, File dest) throws IOException {

        try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(dest)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }
}


