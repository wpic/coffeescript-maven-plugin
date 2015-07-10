package com.github.wpic;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;
import org.jcoffeescript.JCoffeeScriptCompiler;

import javax.script.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

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

    public void execute() throws MojoFailureException {
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
            compiler = new JcoffeescriptCompiler();
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
                new MojoFailureException("Error to read coffeescript file: " + in);
            }

            if (this.outputFile != null) {
                totalCoffeescripts.append(coffeescript).append('\n');
            }
            else {
                String js = null;
                try {
                    js = compiler.compile(coffeescript);
                } catch (Exception ex) {
                    getLog().error(ex);
                    new MojoFailureException("Error in coffeescript file: " + in);
                }

                // create parent folder
                final File out = new File(this.outputDirectory, path.substring(0, path.length() - 6) + "css");
                final File outDir = out.getParentFile();
                if (!outDir.exists() && !outDir.mkdirs()) {
                    throw new MojoFailureException("Can not create output dir: " + outDir);
                }

                try {
                    FileUtils.writeStringToFile(out, js);
                } catch (IOException ex) {
                    getLog().error(ex);
                    new MojoFailureException("Error to save JS file: " + out);
                }
            }
        }

        if (totalCoffeescripts.length() > 0) {
            String js = null;
            try {
                js = compiler.compile(totalCoffeescripts.toString());
            } catch (Exception ex) {
                getLog().error(ex);
                new MojoFailureException("Error in coffeescript files");
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
                new MojoFailureException("Error to save JS file: " + this.outputFile);
            }
        }
    }

    private interface Compiler {
        String compile(String js) throws IOException;
    }

    private class JcoffeescriptCompiler implements Compiler {

        private final JCoffeeScriptCompiler compiler = new JCoffeeScriptCompiler();

        public String compile(String js) throws IOException {
            try {
                return this.compiler.compile(js);
            } catch (Exception ex) {
                throw new IOException(ex);
            }
        }
    }

    private class NashornCompiler implements Compiler {

        private final Invocable invocable;

        NashornCompiler() {
            final ScriptEngineManager engineManager = new ScriptEngineManager();
            final ScriptEngine engine = engineManager.getEngineByName("nashorn");
            final ScriptEngineFactory factory = engine.getFactory();

            invocable = (Invocable) engine;

            try {
                engine.eval(new InputStreamReader(getClass().getResourceAsStream("/com/github/wpic/coffee-script.js")));
            } catch (ScriptException ex) {
                getLog().error(ex);
                new IllegalStateException("Error to load coffeesctip js");
            }

        }

        public String compile(final String coffee) throws IOException {
            try {
                return invocable.invokeFunction("compile", coffee).toString();
            } catch (Exception ex) {
                getLog().error(ex);
                throw new IOException(ex);
            }
        }
    }

}