package com.github.wpic;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jcoffeescript.JCoffeeScriptCompiler;

import javax.script.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * Convert all coffeescript files to Javascript
 */
@Mojo( name = "coffeescript")
public class CoffeescriptMojo extends AbstractMojo {

    @Parameter( defaultValue = "${project.basedir}/src/main/webapp", property = "inputDir", required = false )
    private File inputDirectory;

    @Parameter( defaultValue = "${project.build.directory}/${project.artifactId}", property = "outputDir", required = false )
    private File outputDirectory;

    @Parameter( property = "outputFile", required = false )
    private File outputFile;

    public void execute() throws MojoExecutionException {
        final Iterator<File> files = FileUtils.iterateFiles(this.inputDirectory, new String[]{"coffee"}, true);

        Compiler compiler;
        try {
            compiler = new NashornCompiler();
        }
        catch (Exception ex) {
            compiler = new JcoffeescriptCompiler();
        }

        final StringBuilder totalCoffeescripts = new StringBuilder();

        while (files.hasNext()) {
            final File in = files.next();
            final String path = in.getPath().substring(this.inputDirectory.getPath().length() + 1);

            String coffeescript = null;
            try {
                coffeescript = FileUtils.readFileToString(in);
            }
            catch (IOException ex) {
                new MojoExecutionException("Error to read coffeescript file: " + in);
            }

            if (this.outputFile != null) {
                totalCoffeescripts.append(coffeescript).append('\n');
            }
            else {
                String js = null;
                try {
                    js = compiler.compile(coffeescript);
                } catch (Exception ex) {
                    new MojoExecutionException("Error in coffeescript file: " + in, ex);
                }

                // create parent folder
                final File out = new File(this.outputDirectory, path.substring(0, path.length() - 6) + "css");
                final File outDir = out.getParentFile();
                if (!outDir.exists() && !outDir.mkdirs()) {
                    throw new MojoExecutionException("Can not create output dir: " + outDir);
                }

                try {
                    FileUtils.writeStringToFile(out, js);
                } catch (IOException ex) {
                    new MojoExecutionException("Error to save JS file: " + out);
                }
            }
        }

        if (totalCoffeescripts.length() > 0) {
            String js = null;
            try {
                js = compiler.compile(totalCoffeescripts.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
                new MojoExecutionException("Error in coffeescript files", ex);
            }

            // create parent folder
            final File outDir = this.outputFile.getParentFile();
            if (!outDir.exists() && !outDir.mkdirs()) {
                throw new MojoExecutionException("Can not create output dir: " + outDir);
            }

            try {
                FileUtils.writeStringToFile(this.outputFile, js);
            } catch (IOException ex) {
                new MojoExecutionException("Error to save JS file: " + this.outputFile);
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
            } catch (ScriptException e) {
                new IllegalStateException("Error to load coffeesctip js");
            }

        }

        public String compile(final String coffee) throws IOException {
            try {
                return invocable.invokeFunction("compile", coffee).toString();
            } catch (Exception ex) {
                throw new IOException(ex);
            }
        }
    }

}