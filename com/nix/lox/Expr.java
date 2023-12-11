package com.nix.lox;

import java.util.List;

abstract class Expr {
  interface Visitor<R> {
    R visitAssignExpr(Assign expr);
    R visitBinaryExpr(Binary expr);
    R visitCallExpr(Call expr);
    R visitGetExpr(Get expr);
    R visitGetIndexExpr(GetIndex expr);
    R visitGetStaticExpr(GetStatic expr);
    R visitSetAssignExpr(SetAssign expr);
    R visitCoalesceExpr(Coalesce expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitLogicalExpr(Logical expr);
    R visitSetExpr(Set expr);
    R visitSetIndexExpr(SetIndex expr);
    R visitSuperExpr(Super expr);
    R visitThisExpr(This expr);
    R visitValueExpr(Value expr);
    R visitUnaryExpr(Unary expr);
    R visitVariableExpr(Variable expr);
    R visitNewExpr(New expr);
    R visitTypeofExpr(Typeof expr);
    R visitLengthExpr(Length expr);
    R visitAnonymousFunctionExpr(AnonymousFunction expr);
    R visitCastExpr(Cast expr);
    R visitArrayExpr(Array expr);
    R visitTernaryExpr(Ternary expr);
    R visitTypeExpr(Type expr);
  }
  static class Assign extends Expr {
    Assign(Token name, Expr value, AssignType type) {
      this.name = name;
      this.value = value;
      this.type = type;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignExpr(this);
    }

    final Token name;
    final Expr value;
    final AssignType type;
  }
  static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }
  static class Call extends Expr {
    Call(Expr callee, Token paren, List<Expr> arguments, boolean nullCheck, List<Token> templates) {
      this.callee = callee;
      this.paren = paren;
      this.arguments = arguments;
      this.nullCheck = nullCheck;
      this.templates = templates;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCallExpr(this);
    }

    final Expr callee;
    final Token paren;
    final List<Expr> arguments;
    final boolean nullCheck;
    final List<Token> templates;
  }
  static class Get extends Expr {
    Get(Expr object, Token name) {
      this.object = object;
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGetExpr(this);
    }

    final Expr object;
    final Token name;
  }
  static class GetIndex extends Expr {
    GetIndex(Expr object, Expr index, Token name) {
      this.object = object;
      this.index = index;
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGetIndexExpr(this);
    }

    final Expr object;
    final Expr index;
    final Token name;
  }
  static class GetStatic extends Expr {
    GetStatic(Expr object, Token name) {
      this.object = object;
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGetStaticExpr(this);
    }

    final Expr object;
    final Token name;
  }
  static class SetAssign extends Expr {
    SetAssign(Expr object, Token name, Expr value) {
      this.object = object;
      this.name = name;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSetAssignExpr(this);
    }

    final Expr object;
    final Token name;
    final Expr value;
  }
  static class Coalesce extends Expr {
    Coalesce(Expr object, Token name) {
      this.object = object;
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCoalesceExpr(this);
    }

    final Expr object;
    final Token name;
  }
  static class Grouping extends Expr {
    Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }

    final Expr expression;
  }
  static class Literal extends Expr {
    Literal(Object value) {
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }

    final Object value;
  }
  static class Logical extends Expr {
    Logical(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLogicalExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }
  static class Set extends Expr {
    Set(Expr object, Token name, Expr value) {
      this.object = object;
      this.name = name;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSetExpr(this);
    }

    final Expr object;
    final Token name;
    final Expr value;
  }
  static class SetIndex extends Expr {
    SetIndex(Expr object, Token name, Expr value, Expr index) {
      this.object = object;
      this.name = name;
      this.value = value;
      this.index = index;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSetIndexExpr(this);
    }

    final Expr object;
    final Token name;
    final Expr value;
    final Expr index;
  }
  static class Super extends Expr {
    Super(Token keyword, Token method) {
      this.keyword = keyword;
      this.method = method;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSuperExpr(this);
    }

    final Token keyword;
    final Token method;
  }
  static class This extends Expr {
    This(Token keyword) {
      this.keyword = keyword;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitThisExpr(this);
    }

    final Token keyword;
  }
  static class Value extends Expr {
    Value(Token keyword) {
      this.keyword = keyword;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitValueExpr(this);
    }

    final Token keyword;
  }
  static class Unary extends Expr {
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }

    final Token operator;
    final Expr right;
  }
  static class Variable extends Expr {
    Variable(Token name) {
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }

    final Token name;
  }
  static class New extends Expr {
    New(Token keyword, Expr callee, Token paren, List<Expr> arguments) {
      this.keyword = keyword;
      this.callee = callee;
      this.paren = paren;
      this.arguments = arguments;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitNewExpr(this);
    }

    final Token keyword;
    final Expr callee;
    final Token paren;
    final List<Expr> arguments;
  }
  static class Typeof extends Expr {
    Typeof(Expr value) {
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitTypeofExpr(this);
    }

    final Expr value;
  }
  static class Length extends Expr {
    Length(Expr value, Token name) {
      this.value = value;
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLengthExpr(this);
    }

    final Expr value;
    final Token name;
  }
  static class AnonymousFunction extends Expr {
    AnonymousFunction(List<Parameter> params, List<Stmt> body, LoxType returnType) {
      this.params = params;
      this.body = body;
      this.returnType = returnType;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAnonymousFunctionExpr(this);
    }

    final List<Parameter> params;
    final List<Stmt> body;
    final LoxType returnType;
  }
  static class Cast extends Expr {
    Cast(Token operator, Expr value, Expr castType) {
      this.operator = operator;
      this.value = value;
      this.castType = castType;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCastExpr(this);
    }

    final Token operator;
    final Expr value;
    final Expr castType;
  }
  static class Array extends Expr {
    Array(LoxType type, List<Expr> values, Expr size) {
      this.type = type;
      this.values = values;
      this.size = size;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitArrayExpr(this);
    }

    final LoxType type;
    final List<Expr> values;
    final Expr size;
  }
  static class Ternary extends Expr {
    Ternary(Token operator, Expr condition, Expr thenBranch, Expr elseBranch) {
      this.operator = operator;
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitTernaryExpr(this);
    }

    final Token operator;
    final Expr condition;
    final Expr thenBranch;
    final Expr elseBranch;
  }
  static class Type extends Expr {
    Type(LoxType type) {
      this.type = type;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitTypeExpr(this);
    }

    final LoxType type;
  }

   abstract <R> R accept(Visitor<R> visitor);
}
