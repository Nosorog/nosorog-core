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

package io.nosorog.core.internal;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.QualifiedNameExpr;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javassist.Modifier;

public class Importer {

    private static final Logger LOG = Logger.getLogger(Importer.class.getName());

    private final StringBuilder prelude = new StringBuilder();
    private final ClassPath classpath;
    private final Collection<ImportDeclaration> nodes;

    public Importer(Collection<ImportDeclaration> nodes, ClassLoader classLoader) throws IOException {
        classpath = ClassPath.from(classLoader);
        this.nodes = nodes;
    }

    public String getPrelude() {

        for (ImportDeclaration node : nodes) {

            try {
                if (node.isStatic()) {
                    if (node.isAsterisk()) {
                        importStatic(node.getName().toString());
                    } else {
                        QualifiedNameExpr fqn = (QualifiedNameExpr) node.getChildrenNodes().get(0);
                        importMethod(fqn.getQualifier().toString(), fqn.getName());
                    }
                } else if (node.isAsterisk()) {
                    importPackage(node.getName().toString());
                } else {
                    importClass(node.getName().toString());
                }
            } catch (ClassNotFoundException | NoSuchMethodException ex) {
                LOG.log(Level.WARNING, null, ex);
            }

        }

        return prelude.toString();

    }

    private void importClass(String name) throws ClassNotFoundException {

        Class<?> clazz = Class.forName(name);
        prelude.append(String.format("var %s = Java.type('%s');%n", clazz.getSimpleName(), name));

    }

    private void importPackage(String name) throws ClassNotFoundException {

        ImmutableSet<ClassPath.ClassInfo> classes = classpath.getTopLevelClasses(name);

        for (ClassPath.ClassInfo clazz : classes) {
            importClass(clazz.getName());
        }

    }

    private void importStatic(String name) throws ClassNotFoundException {

        Class<?> clazz = Class.forName(name);
        Set<String> methods = new HashSet<>();

        for (Method method : clazz.getMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                methods.add(method.getName());
            }
        }

        for (String method : methods) {
            prelude.append(String.format("var %s = Java.type('%s').%s;%n", method, name, method));
        }

    }

    private void importMethod(String className, String methodName) throws ClassNotFoundException, NoSuchMethodException {

        Class<?> clazz = Class.forName(className);

        boolean found = false;

        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName) && Modifier.isStatic(method.getModifiers())) {
                found = true;
                break;
            }
        }

        if (found) {
            prelude.append(String.format("var %s = Java.type('%s').%s;%n", methodName, className, methodName));
        } else {
            throw new NoSuchMethodException(className + "." + methodName);
        }

    }

}
