package com.nix.lox;

import java.util.List;

abstract class Stmt {
  interface Visitor<R> {
    R visitBlockStmt(Block stmt);
    R visitClassStmt(Class stmt);
    R visitExpressionStmt(Expression stmt);
    R visitFunctionStmt(Function stmt);
    R visitIfStmt(If stmt);
    R visitWhenStmt(When stmt);
    R visitReturnStmt(Return stmt);
    R visitVarStmt(Var stmt);
    R visitWhileStmt(While stmt);
  }
  static class Block extends Stmt {
    Block(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }

    final List<Stmt> statements;
  }
  static class Class extends Stmt {
    Class(Token name, Expr.Variable superclass, List<Stmt.Function> methods, List<Stmt.Var> variables) {
      this.name = name;
      this.superclass = superclass;
      this.methods = methods;
      this.variables = variables;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitClassStmt(this);
    }

    final Token name;
    final Expr.Variable superclass;
    final List<Stmt.Function> methods;
    final List<Stmt.Var> variables;
  }
  static class Expression extends Stmt {
    Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }

    final Expr expression;
  }
  static class Function extends Stmt {
    Function(Token name, List<Token> params, List<Stmt> body, boolean isStatic, boolean isConstant) {
      this.name = name;
      this.params = params;
      this.body = body;
      this.isStatic = isStatic;
      this.isConstant = isConstant;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionStmt(this);
    }

    final Token name;
    final List<Token> params;
    final List<Stmt> body;
    final boolean isStatic;
    final boolean isConstant;
  }
  static class If extends Stmt {
    If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfStmt(this);
    }

    final Expr condition;
    final Stmt thenBranch;
    final Stmt elseBranch;
  }
  static class When extends Stmt {
    When(Expr condition, Stmt thenBranch, Stmt finallyBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.finallyBranch = finallyBranch;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhenStmt(this);
    }

    final Expr condition;
    final Stmt thenBranch;
    final Stmt finallyBranch;
  }
  static class Return extends Stmt {
    Return(Token keyword, Expr value) {
      this.keyword = keyword;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitReturnStmt(this);
    }

    final Token keyword;
    final Expr value;
  }
  static class Var extends Stmt {
    Var(Token name, Expr initializer, boolean isConstant) {
      this.name = name;
      this.initializer = initializer;
      this.isConstant = isConstant;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }

    final Token name;
    final Expr initializer;
    final boolean isConstant;
  }
  static class While extends Stmt {
    While(Expr condition, Stmt body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStmt(this);
    }

    final Expr condition;
    final Stmt body;
  }

   abstract <R> R accept(Visitor<R> visitor);
}
