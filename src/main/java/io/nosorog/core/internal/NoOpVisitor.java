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

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.TypeParameter;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EmptyMemberDeclaration;
import com.github.javaparser.ast.body.EmptyTypeDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.MultiTypeParameter;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralMinValueExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralMinValueExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.QualifiedNameExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.TypeDeclarationStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;
import com.github.javaparser.ast.visitor.VoidVisitor;
import io.nosorog.core.Script;

public class NoOpVisitor implements VoidVisitor<Script> {

    @Override
    public void visit(CompilationUnit n, Script arg) {
    }

    @Override
    public void visit(PackageDeclaration n, Script arg) {
    }

    @Override
    public void visit(ImportDeclaration n, Script arg) {
    }

    @Override
    public void visit(TypeParameter n, Script arg) {
    }

    @Override
    public void visit(LineComment n, Script arg) {
    }

    @Override
    public void visit(BlockComment n, Script arg) {
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Script arg) {
    }

    @Override
    public void visit(EnumDeclaration n, Script arg) {
    }

    @Override
    public void visit(EmptyTypeDeclaration n, Script arg) {
    }

    @Override
    public void visit(EnumConstantDeclaration n, Script arg) {
    }

    @Override
    public void visit(AnnotationDeclaration n, Script arg) {
    }

    @Override
    public void visit(AnnotationMemberDeclaration n, Script arg) {
    }

    @Override
    public void visit(FieldDeclaration n, Script arg) {
    }

    @Override
    public void visit(VariableDeclarator n, Script arg) {
    }

    @Override
    public void visit(VariableDeclaratorId n, Script arg) {
    }

    @Override
    public void visit(ConstructorDeclaration n, Script arg) {
    }

    @Override
    public void visit(MethodDeclaration n, Script arg) {
    }

    @Override
    public void visit(Parameter n, Script arg) {
    }

    @Override
    public void visit(MultiTypeParameter n, Script arg) {
    }

    @Override
    public void visit(EmptyMemberDeclaration n, Script arg) {
    }

    @Override
    public void visit(InitializerDeclaration n, Script arg) {
    }

    @Override
    public void visit(JavadocComment n, Script arg) {
    }

    @Override
    public void visit(ClassOrInterfaceType n, Script arg) {
    }

    @Override
    public void visit(PrimitiveType n, Script arg) {
    }

    @Override
    public void visit(ReferenceType n, Script arg) {
    }

    @Override
    public void visit(VoidType n, Script arg) {
    }

    @Override
    public void visit(WildcardType n, Script arg) {
    }

    @Override
    public void visit(UnknownType n, Script arg) {
    }

    @Override
    public void visit(ArrayAccessExpr n, Script arg) {
    }

    @Override
    public void visit(ArrayCreationExpr n, Script arg) {
    }

    @Override
    public void visit(ArrayInitializerExpr n, Script arg) {
    }

    @Override
    public void visit(AssignExpr n, Script arg) {
    }

    @Override
    public void visit(BinaryExpr n, Script arg) {
    }

    @Override
    public void visit(CastExpr n, Script arg) {
    }

    @Override
    public void visit(ClassExpr n, Script arg) {
    }

    @Override
    public void visit(ConditionalExpr n, Script arg) {
    }

    @Override
    public void visit(EnclosedExpr n, Script arg) {
    }

    @Override
    public void visit(FieldAccessExpr n, Script arg) {
    }

    @Override
    public void visit(InstanceOfExpr n, Script arg) {
    }

    @Override
    public void visit(StringLiteralExpr n, Script arg) {
    }

    @Override
    public void visit(IntegerLiteralExpr n, Script arg) {
    }

    @Override
    public void visit(LongLiteralExpr n, Script arg) {
    }

    @Override
    public void visit(IntegerLiteralMinValueExpr n, Script arg) {
    }

    @Override
    public void visit(LongLiteralMinValueExpr n, Script arg) {
    }

    @Override
    public void visit(CharLiteralExpr n, Script arg) {
    }

    @Override
    public void visit(DoubleLiteralExpr n, Script arg) {
    }

    @Override
    public void visit(BooleanLiteralExpr n, Script arg) {
    }

    @Override
    public void visit(NullLiteralExpr n, Script arg) {
    }

    @Override
    public void visit(MethodCallExpr n, Script arg) {
    }

    @Override
    public void visit(NameExpr n, Script arg) {
    }

    @Override
    public void visit(ObjectCreationExpr n, Script arg) {
    }

    @Override
    public void visit(QualifiedNameExpr n, Script arg) {
    }

    @Override
    public void visit(ThisExpr n, Script arg) {
    }

    @Override
    public void visit(SuperExpr n, Script arg) {
    }

    @Override
    public void visit(UnaryExpr n, Script arg) {
    }

    @Override
    public void visit(VariableDeclarationExpr n, Script arg) {
    }

    @Override
    public void visit(MarkerAnnotationExpr n, Script arg) {
    }

    @Override
    public void visit(SingleMemberAnnotationExpr n, Script arg) {
    }

    @Override
    public void visit(NormalAnnotationExpr n, Script arg) {
    }

    @Override
    public void visit(MemberValuePair n, Script arg) {
    }

    @Override
    public void visit(ExplicitConstructorInvocationStmt n, Script arg) {
    }

    @Override
    public void visit(TypeDeclarationStmt n, Script arg) {
    }

    @Override
    public void visit(AssertStmt n, Script arg) {
    }

    @Override
    public void visit(BlockStmt n, Script arg) {
    }

    @Override
    public void visit(LabeledStmt n, Script arg) {
    }

    @Override
    public void visit(EmptyStmt n, Script arg) {
    }

    @Override
    public void visit(ExpressionStmt n, Script arg) {
    }

    @Override
    public void visit(SwitchStmt n, Script arg) {
    }

    @Override
    public void visit(SwitchEntryStmt n, Script arg) {
    }

    @Override
    public void visit(BreakStmt n, Script arg) {
    }

    @Override
    public void visit(ReturnStmt n, Script arg) {
    }

    @Override
    public void visit(IfStmt n, Script arg) {
    }

    @Override
    public void visit(WhileStmt n, Script arg) {
    }

    @Override
    public void visit(ContinueStmt n, Script arg) {
    }

    @Override
    public void visit(DoStmt n, Script arg) {
    }

    @Override
    public void visit(ForeachStmt n, Script arg) {
    }

    @Override
    public void visit(ForStmt n, Script arg) {
    }

    @Override
    public void visit(ThrowStmt n, Script arg) {
    }

    @Override
    public void visit(SynchronizedStmt n, Script arg) {
    }

    @Override
    public void visit(TryStmt n, Script arg) {
    }

    @Override
    public void visit(CatchClause n, Script arg) {
    }

    @Override
    public void visit(LambdaExpr n, Script arg) {
    }

    @Override
    public void visit(MethodReferenceExpr n, Script arg) {
    }

    @Override
    public void visit(TypeExpr n, Script arg) {
    }

    @Override
    public void visit(IntersectionType it, Script a) {
    }

    @Override
    public void visit(UnionType ut, Script a) {
    }

}
