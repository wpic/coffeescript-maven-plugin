package com.github.wpic;

import javax.script.*;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 */
public class NashornCompiler implements Compiler {

    private final Invocable invocable;

    NashornCompiler() {
        final ScriptEngineManager engineManager = new ScriptEngineManager();
        final ScriptEngine engine = engineManager.getEngineByName("nashorn");
        final ScriptEngineFactory factory = engine.getFactory();

        invocable = (Invocable) engine;

        try {
            engine.eval(new InputStreamReader(getClass().getResourceAsStream("/com/github/wpic/coffee-script.js")));
        } catch (ScriptException ex) {
            new IllegalStateException("Error to load coffeesctip js");
        }

    }

    public String compile(final String coffee) throws IOException {
        try {
            return invocable.invokeFunction("compile", coffee).toString();
        } catch (Throwable t) {
            throw new IOException(t);
        }
    }
}