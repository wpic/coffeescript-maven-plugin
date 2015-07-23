package com.github.wpic;

import org.jcoffeescript.JCoffeeScriptCompiler;

import java.io.IOException;

/**
 *
 */
public class JCoffeescriptCompiler implements Compiler {

    private final JCoffeeScriptCompiler compiler = new JCoffeeScriptCompiler();

    public String compile(String js) throws IOException {
        try {
            return this.compiler.compile(js);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }
}
