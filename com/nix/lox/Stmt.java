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
    R visitExpectStmt(Expect stmt);
    R visitVarStmt(Var stmt);
    R visitWhileStmt(While stmt);
    R visitTestStmt(Test stmt);
    R visitGetFileStmt(GetFile stmt);
    R visitModuleStmt(Module stmt);
    R visitInterfaceStmt(Interface stmt);
    R visitEnumStmt(Enum stmt);
    R visitSwitchStmt(Switch stmt);
    R visitCaseStmt(Case stmt);
    R visitBreakStmt(Break stmt);
    R visitContinueStmt(Continue stmt);
    R visitPropertyStmt(Property stmt);
    R visitTryStmt(Try stmt);
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
    Class(Token name, Expr.Variable superclass, List<Stmt.Function> methods, List<Stmt.Var> variables, List<Stmt.Property> props, List<Token> templates, List<Token> interfase) {
      this.name = name;
      this.superclass = superclass;
      this.methods = methods;
      this.variables = variables;
      this.props = props;
      this.templates = templates;
      this.interfase = interfase;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitClassStmt(this);
    }

    final Token name;
    final Expr.Variable superclass;
    final List<Stmt.Function> methods;
    final List<Stmt.Var> variables;
    final List<Stmt.Property> props;
    final List<Token> templates;
    final List<Token> interfase;
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
    Function(Token name, Token extClass, List<Parameter> params, List<Stmt> body, Modifiers modifiers, Boolean hasBody, LoxType returnType) {
      this.name = name;
      this.extClass = extClass;
      this.params = params;
      this.body = body;
      this.modifiers = modifiers;
      this.hasBody = hasBody;
      this.returnType = returnType;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionStmt(this);
    }

    final Token name;
    final Token extClass;
    final List<Parameter> params;
    final List<Stmt> body;
    final Modifiers modifiers;
    final Boolean hasBody;
    final LoxType returnType;
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
  static class Expect extends Stmt {
    Expect(Token keyword, Expr value) {
      this.keyword = keyword;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpectStmt(this);
    }

    final Token keyword;
    final Expr value;
  }
  static class Var extends Stmt {
    Var(Token name, Expr initializer, Modifiers modifiers, LoxType type) {
      this.name = name;
      this.initializer = initializer;
      this.modifiers = modifiers;
      this.type = type;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }

    final Token name;
    final Expr initializer;
    final Modifiers modifiers;
    final LoxType type;
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
  static class Test extends Stmt {
    Test(Expr name, Stmt body) {
      this.name = name;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitTestStmt(this);
    }

    final Expr name;
    final Stmt body;
  }
  static class GetFile extends Stmt {
    GetFile(Token name, Expr path) {
      this.name = name;
      this.path = path;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGetFileStmt(this);
    }

    final Token name;
    final Expr path;
  }
  static class Module extends Stmt {
    Module(Token keyword) {
      this.keyword = keyword;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitModuleStmt(this);
    }

    final Token keyword;
  }
  static class Interface extends Stmt {
    Interface(Token name, List<Stmt.Function> methods, List<Stmt.Var> variables) {
      this.name = name;
      this.methods = methods;
      this.variables = variables;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitInterfaceStmt(this);
    }

    final Token name;
    final List<Stmt.Function> methods;
    final List<Stmt.Var> variables;
  }
  static class Enum extends Stmt {
    Enum(Token name, List<LoxEnum.Element> elements) {
      this.name = name;
      this.elements = elements;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitEnumStmt(this);
    }

    final Token name;
    final List<LoxEnum.Element> elements;
  }
  static class Switch extends Stmt {
    Switch(Expr value, List<Stmt.Case> cases, Stmt.Case defaultCase) {
      this.value = value;
      this.cases = cases;
      this.defaultCase = defaultCase;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSwitchStmt(this);
    }

    final Expr value;
    final List<Stmt.Case> cases;
    final Stmt.Case defaultCase;
  }
  static class Case extends Stmt {
    Case(Expr value, Stmt body) {
      this.value = value;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCaseStmt(this);
    }

    final Expr value;
    final Stmt body;
  }
  static class Break extends Stmt {
    Break(Token keyword) {
      this.keyword = keyword;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBreakStmt(this);
    }

    final Token keyword;
  }
  static class Continue extends Stmt {
    Continue(Token keyword) {
      this.keyword = keyword;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitContinueStmt(this);
    }

    final Token keyword;
  }
  static class Property extends Stmt {
    Property(Token name, Modifiers modifiers, LoxType type, Stmt.Function get, Stmt.Function set) {
      this.name = name;
      this.modifiers = modifiers;
      this.type = type;
      this.get = get;
      this.set = set;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPropertyStmt(this);
    }

    final Token name;
    final Modifiers modifiers;
    final LoxType type;
    final Stmt.Function get;
    final Stmt.Function set;
  }
  static class Try extends Stmt {
    Try(Stmt tryBranch, List<Stmt> catchBranch, Token exName) {
      this.tryBranch = tryBranch;
      this.catchBranch = catchBranch;
      this.exName = exName;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitTryStmt(this);
    }

    final Stmt tryBranch;
    final List<Stmt> catchBranch;
    final Token exName;
  }

   abstract <R> R accept(Visitor<R> visitor);
}
