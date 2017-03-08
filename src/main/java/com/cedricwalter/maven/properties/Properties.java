package com.cedricwalter.maven.properties;


import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

@Mojo(name = "properties", threadSafe = true)
public class Properties extends AbstractMojo {

    @Parameter(defaultValue = "${basedir}/src/main/resources")
    private String folder;

    @Parameter(defaultValue = "reference.properties")
    private String reference;

    @Parameter(defaultValue = "[a-zA-Z]*.properties")
    private String pattern;

    private final Log log = getLog();

    private String getFolder() {
        return folder;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {

        log.info(String.format("Looking for all file(s) in folder '%s'.", getFolder()));
        Collection<File> files = getFiles();

        log.info(String.format("Found '%s' files in folder '%s' to compare.", files.size(), getFolder()));
        if (files.size() == 1) {
            log.warn(String.format("Only one file found in '%s' can not compare '%s' to any other properties.", getFolder(), getFileReferences()));
            return;
        }
        final File referenceFile = getFileReferences();
        final Set<Object> referenceFileKeys = getKeys(referenceFile);

        for (File file : files) {
            log.info(String.format("Comparing file '%s' to reference '%s'.", file, referenceFile));
            final Set<Object> keys1 = getKeys(file);
            final boolean equals = equals(referenceFileKeys, keys1);
            if (!equals) {
                String msg;
                if (referenceFileKeys.size() >= keys1.size()) {
                    referenceFileKeys.removeAll(keys1);
                    msg = String.format("Missing keys '%s' in '%s' compare to reference '%s'", referenceFileKeys, file, referenceFile);
                } else {
                    keys1.removeAll(referenceFileKeys);
                    msg = String.format("Too much keys '%s' in '%s' compare to reference '%s'", keys1, file, referenceFile);
                }
                log.error(msg);
                throw new MojoFailureException(msg);
            }
        }
    }

    private File getFileReferences() {
        return new File(getFolder() + "/" + reference);
    }

    private Set<Object> getKeys(File file) throws MojoFailureException {
        java.util.Properties props = new java.util.Properties();
        try {
            props.load(new FileReader(file));

            return props.keySet();
        } catch (IOException e) {
            log.error(e);
            throw new MojoFailureException(e.getMessage());
        }
    }

    private Collection<File> getFiles() throws MojoFailureException {
        Collection<File> files = FileUtils.listFiles(
                new File(getFolder()),
                new RegexFileFilter("[a-zA-Z]*.properties"),
                DirectoryFileFilter.DIRECTORY
        );
        if (files.size() == 0) {
            final String msg = String.format("No file found in '%s'.", getFolder());
            log.info(msg);
            throw new MojoFailureException(msg);
        }
        files.remove(getFileReferences());

        return files;
    }

    private static <E> boolean equals(Set<? extends E> set1, Set<? extends E> set2) {
        return Sets.symmetricDifference(set1, set2).isEmpty();
    }

}