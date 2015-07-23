package com.github.wpic;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;
import java.io.IOException;

/**
 * Convert all coffeescript files to Javascript
 */
@Mojo( name = "coffeescript")
public class CoffeescriptMojo extends AbstractMojo {

    @Parameter( defaultValue = "${project.basedir}/src/main/webapp", property = "inputDirectory", required = false )
    private File inputDirectory;

    @Parameter( defaultValue = "**/*.coffee", property = "include", required = false )
    private String include;

    @Parameter( property = "exclude", required = false )
    private String exclude;

    @Parameter( defaultValue = "${project.build.directory}/${project.artifactId}", property = "outputDirectory", required = false )
    private File outputDirectory;

    @Parameter( property = "outputFile", required = false )
    private File outputFile;

    public void execute() throws MojoExecutionException, MojoFailureException {
        final DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(new String[]{include});
        if (exclude != null) {
            scanner.setExcludes(new String[]{exclude});
        }
        scanner.setBasedir(this.inputDirectory);
        scanner.setCaseSensitive(false);
        scanner.scan();
        final String[] files = scanner.getIncludedFiles();

        Compiler compiler;
        try {
            compiler = new NashornCompiler();
        }
        catch (Exception ex) {
            compiler = new JCoffeescriptCompiler();
        }

        final StringBuilder totalCoffeescripts = new StringBuilder();

        for (final String file:files) {
            final File in = new File(file);
            final String path = in.getPath(); //in.getPath().substring(this.inputDirectory.getPath().length() + 1);

            String coffeescript = null;
            try {
                coffeescript = FileUtils.readFileToString(in);
            }
            catch (IOException ex) {
                getLog().error(ex);
                new MojoExecutionException("Error to read coffeescript file: " + in);
            }

            if (this.outputFile != null) {
                totalCoffeescripts.append(coffeescript).append('\n');
            }
            else {
                String js = null;
                try {
                    js = compiler.compile(coffeescript);
                } catch (Throwable t) {
                    getLog().error(t);
                    new MojoExecutionException("Error in coffeescript file: " + in);
                }

                // create parent folder
                final File out = new File(this.outputDirectory, path.substring(0, path.length() - 6) + "css");
                final File outDir = out.getParentFile();
                if (!outDir.exists() && !outDir.mkdirs()) {
                    throw new MojoFailureException("Can not create output dir: " + outDir);
                }

                try {
                    FileUtils.writeStringToFile(out, js);
                } catch (Throwable t) {
                    getLog().error(t);
                    new MojoExecutionException("Error to save JS file: " + out);
                }
            }
        }

        if (totalCoffeescripts.length() > 0) {
            String js = null;
            try {
                js = compiler.compile(totalCoffeescripts.toString());
            } catch (Throwable t) {
                getLog().error(t);
                new MojoExecutionException("Error in coffeescript files");
            }

            // create parent folder
            final File outDir = this.outputFile.getParentFile();
            if (!outDir.exists() && !outDir.mkdirs()) {
                throw new MojoFailureException("Can not create output dir: " + outDir);
            }

            try {
                FileUtils.writeStringToFile(this.outputFile, js);
            } catch (IOException ex) {
                getLog().error(ex);
                new MojoExecutionException("Error to save JS file: " + this.outputFile);
            }
        }
    }

}