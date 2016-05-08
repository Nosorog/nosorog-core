/*
 * Copyright 2016 Dmitry Telegin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosorog.core;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.Node;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import org.apache.commons.lang3.StringUtils;

/**
 * ScriptLoader loads a script from an {@link InputStream}, extracts metadata and prepares script for execution.
 */
public class ScriptLoader {

    private static final Logger LOG = Logger.getLogger(ScriptLoader.class.getName());

    private final ClassLoader classLoader;

    /**
     * Construct a ScriptLoader with default {@link ClassLoader}.
     */
    public ScriptLoader() {
        this(null);
    }

    /**
     * Construct a ScriptLoader with custom {@link ClassLoader}.
     * @param classLoader custom {@link ClassLoader}
     */
    public ScriptLoader(ClassLoader classLoader) {
        this.classLoader = classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader;
    }

    /**
     * Load a {@link Script} from {@link InputStream}.
     * @param is stream to read from
     * @return script object
     * @throws IOException if IOException has occurred while reading from the stream
     * @throws ScriptException if an error occurred while processing imports or injections
     */
    public Script load(InputStream is) throws IOException, ScriptException {

        StringWriter body = new StringWriter();
        PrintWriter pw = new PrintWriter(body);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {

            String line;
            boolean flag = false;
            Collection<Node> nodes = new ArrayList<>();

            while ((line = reader.readLine()) != null) {

                pw.println(line);

                if (StringUtils.strip(line).equals("/**")) {
                    flag = true;
                    continue;
                }

                if (StringUtils.strip(line).equals("*/")) {
                    flag = false;
                    continue;
                }

                if (flag) {
                    try {
                        Node node = parseHeader(line);
                        if (node != null) {
                            nodes.add(node);
                        }
                    } catch (ParseException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                }

            }

            return Script.builder(nodes, body.getBuffer().toString(), classLoader).build();

        }

    }

    private Node parseHeader(String line) throws ParseException {

        String l = StringUtils.stripStart(line, " *");

        if (l.startsWith("import")) {
            return JavaParser.parseImport(l + ";");
        } else if (StringUtils.startsWithAny(l, "@Name", "@Description", "@Startup", "@Schedule")) {
            return JavaParser.parseAnnotation(l);
        } else if (l.startsWith("@")) {
            return JavaParser.parseBodyDeclaration(l + ";");
        }

        return null;

    }

}
