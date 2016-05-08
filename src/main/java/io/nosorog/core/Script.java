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

import io.nosorog.core.internal.NoOpVisitor;
import io.nosorog.core.internal.Importer;
import io.nosorog.core.internal.Binder;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.compiler.CompileError;
import javassist.compiler.MemberResolver;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * The Script class represents a script together with its metadata.
 *
 * <br><br>Metadata format depends on the script language.
 * For JavaScript, that would be JSDoc comments and/or <a href="http://ecma-international.org/ecma-262/5.1/#sec-14.1">directive prologues</a>.
 *
 */
public class Script {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    private SingleMemberAnnotationExpr nameNode;
    private SingleMemberAnnotationExpr descriptionNode;

    private final Collection<ImportDeclaration> importNodes = new ArrayList<>();

    private NormalAnnotationExpr startupNode;
    private SingleMemberAnnotationExpr scheduleNode;

    private final Collection<FieldDeclaration> observesNodes = new ArrayList<>();

    private final Collection<FieldDeclaration> injectNodes = new ArrayList<>();
    private final Collection<FieldDeclaration> resourceNodes = new ArrayList<>();

    private final Visitor visitor = new Visitor();
    private final String body;
    private String prelude;
    private Bindings bindings;

    private Script(String body) {
        this.body = body;
    }

    /**
     * Returns script name.
     * @return script name
     */
    public String getName() {
        return ((StringLiteralExpr)nameNode.getMemberValue()).getValue();
    }

    /**
     * Returns script prelude.
     *
     * Prelude is a code fragment that needs to be executed before script, so
     * that all the script dependencies are satisfied.
     *
     * @return script prelude
     */
    public String getPrelude() {
        return prelude;
    }

    /**
     * Returns script body.
     * @return script body
     */
    public String getBody() {
        return body;
    }

    /**
     * Returns script bindings.
     *
     * Bindings produced by this method need to be supplied to {@link javax.script.ScriptEngine}
     * before script execution, so that all the script dependencies are satisfied.
     *
     * @return script bindings
     */
    public Bindings getBindings() {

        return bindings;

    }

    /**
     * Convenience method to execute a script with the given {@link ScriptEngine}.
     *
     * It feeds generated bindings and prelude to the engine, and runs the script afterwards.
     *
     * @param engine script engine
     * @return result of script execution
     * @throws ScriptException if an exception occurred during script processing
     */
    public Object runWith(ScriptEngine engine) throws ScriptException {

        engine.setBindings(getBindings(), ScriptContext.ENGINE_SCOPE);
        engine.eval(getPrelude());

        return engine.eval(getBody());

    }

    private class Visitor extends NoOpVisitor {

        @Override
        public void visit(NormalAnnotationExpr node, Script script) {

            String name = node.getName().getName();

            switch (name) {

                case "Startup":
                    startupNode = node;
                    break;

                default:

            }

        }

        @Override
        public void visit(SingleMemberAnnotationExpr node, Script script) {

            String name = node.getName().getName();

            switch (name) {

                case "Name":
                    nameNode = node;
                    break;

                case "Description":
                    descriptionNode = node;
                    break;

                case "Schedule":
                    scheduleNode = node;
                    break;

                default:

            }

        }

        @Override
        public void visit(FieldDeclaration node, Script script) {

            String name = node.getAnnotations().get(0).getName().getName();

            switch (name) {

                case "Observes":
                    observesNodes.add(node);
                    break;

                case "Inject":
                    injectNodes.add(node);
                    break;

                case "Resource":
                case "EJB":
                case "WebServiceRef":
                case "PersistenceUnit":
                case "PersistenceContext":
                    resourceNodes.add(node);
                    break;

                default:

            }

        }

        @Override
        public void visit(ImportDeclaration node, Script script) {
            importNodes.add(node);
        }

    }

    private class StubBuilder {

        private final Logger LOG = Logger.getLogger(StubBuilder.class.getName());
        private static final String STUB = "io.nosorog.core.internal.Stub";

        private final ClassPool pool;
        private final MemberResolver resolver;
        private final Collection<String> fields = new ArrayList<>();

        private StubBuilder(ClassLoader classLoader) {

            pool = new ClassPool(true);

            pool.appendClassPath(new LoaderClassPath(classLoader));

            pool.importPackage("javax.script");
            pool.importPackage("javax.inject");
            pool.importPackage("javax.enterprise.inject");
            pool.importPackage("javax.annotation");
            pool.importPackage("javax.persistence");
            pool.importPackage("javax.ejb.EJB");
            pool.importPackage("javax.xml.ws.WebServiceRef");

            resolver = new MemberResolver(pool);

        }

        private Class build() throws CannotCompileException {

            for (ImportDeclaration node : importNodes) {
                if (!node.isStatic()) {
                    pool.importPackage(node.getName().toString());
                }
            }

            CtClass ctClass = pool.makeClass(
                String.format("%s$%s$%d", STUB, getName(), SEQUENCE.incrementAndGet())
            );

            ClassFile classFile = ctClass.getClassFile();
            ConstPool constPool = classFile.getConstPool();
            classFile.setInterfaces(new String[] { STUB });

            for (FieldDeclaration inject : injectNodes) {

                String name = inject.getVariables().get(0).getId().getName();
                ReferenceType refType = (ReferenceType) inject.getType();
                ClassOrInterfaceType coiType = (ClassOrInterfaceType) refType.getType();

                try {

                    String fqn = resolve(coiType.getName());
                    CtField field = new CtField(pool.get(fqn), name, ctClass);
                    ctClass.addField(field);
                    FieldInfo info = field.getFieldInfo();

                    for (AnnotationExpr node : inject.getAnnotations()) {
                        AnnotationsAttribute attribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                        String afqn = resolve(node.getName().getName());
                        Annotation annotation = new Annotation(afqn, constPool);
                        attribute.setAnnotation(annotation);
                        info.addAttribute(attribute);
                    }

                    fields.add(name);

                } catch (CompileError | CannotCompileException | NotFoundException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }

            }

            StringBuilder src = new StringBuilder();

            src.append("public Bindings getBindings() {");
            src.append(" SimpleBindings bindings = new SimpleBindings();");

            for (String name : fields) {
                src.append(String.format(" bindings.put(\"%s\", %s);", name, name));
            }

            src.append(" return bindings;");
            src.append("}");

            CtMethod method = CtNewMethod.make(src.toString(), ctClass);
            ctClass.addMethod(method);

            return ctClass.toClass();

        }

        private String resolve(String name) throws CompileError {
            return resolver.resolveJvmClassName(MemberResolver.jvmToJavaName(name));
        }

    }

    static Builder builder(Collection<Node> nodes, String body, ClassLoader classLoader) {
        return new Script(body).new Builder(nodes, classLoader);
    }

    class Builder {

        private final Collection<Node> nodes;
        private final ClassLoader classLoader;

        private Builder(Collection<Node> nodes, ClassLoader classLoader) {
            this.nodes = nodes;
            this.classLoader = classLoader;
        }

        Script build() throws ScriptException {

            for (Node node : nodes) {
                node.accept(visitor, null);
            }

            try {

                StubBuilder stubBuilder = new StubBuilder(classLoader);
                Importer importer = new Importer(importNodes, classLoader);

                prelude = importer.getPrelude();
                bindings = Binder.getBindings(stubBuilder.build());

            } catch (IOException | CannotCompileException | InstantiationException | IllegalAccessException e) {
                throw new ScriptException(e);
            }

            return Script.this;

        }

    }

}
