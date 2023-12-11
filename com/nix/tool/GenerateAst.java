package com.nix.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
  public static void main(String[] args) {
    System.out.println(args);
    String ouputDir = args[0];
    try {
      defineAst(ouputDir, "Expr", Arrays.asList(
        "Assign   : Token name, Expr value, AssignType type",
        "Binary   : Expr left, Token operator, Expr right",
        "Call     : Expr callee, Token paren, List<Expr> arguments, boolean nullCheck, List<Token> templates",
        "Get      : Expr object, Token name",
        "GetIndex : Expr object, Expr index, Token name",
        "GetStatic: Expr object, Token name",
        "SetAssign: Expr object, Token name, Expr value",
        "Coalesce      : Expr object, Token name",
        "Grouping : Expr expression",
        "Literal  : Object value",
        "Logical  : Expr left, Token operator, Expr right",
        "Set      : Expr object, Token name, Expr value",
        "SetIndex : Expr object, Token name, Expr value, Expr index",
        "Super    : Token keyword, Token method",
        "This     : Token keyword",
        "Value     : Token keyword",
        "Unary    : Token operator, Expr right",
        "Variable : Token name",
        "New      : Token keyword, Expr callee, Token paren, List<Expr> arguments",
        "Typeof: Expr value",
        "Length: Expr value, Token name",
        "AnonymousFunction: List<Parameter> params, List<Stmt> body, LoxType returnType",
        "Cast: Token operator, Expr value, Expr castType",
        "Array: LoxType type, List<Expr> values, Expr size",
        "Ternary: Token operator, Expr condition, Expr thenBranch, Expr elseBranch",
        "Type: LoxType type"
      ));

      defineAst(ouputDir, "Stmt", Arrays.asList(
        "Block      : List<Stmt> statements",
        "Class      : Token name, Expr.Variable superclass," +
                  " List<Stmt.Function> methods, List<Stmt.Var> variables, List<Stmt.Property> props, List<Token> templates, List<Token> interfase",
        "Expression : Expr expression",
        "Function   : Token name, Token extClass, List<Parameter> params," +
                  " List<Stmt> body, Modifiers modifiers, Boolean hasBody, LoxType returnType",
        "If         : Expr condition, Stmt thenBranch," +
                    " Stmt elseBranch",
        "When         : Expr condition, Stmt thenBranch, Stmt finallyBranch",
        "Return     : Token keyword, Expr value",
        "Expect     : Token keyword, Expr value",
        "Var        : Token name, Expr initializer, Modifiers modifiers, LoxType type",
        "While      : Expr condition, Stmt body",
        "Test      : Expr name, Stmt body",
        "GetFile     : Token name, Expr path",
        "Module    : Token keyword",
        "Interface : Token name, List<Stmt.Function> methods, List<Stmt.Var> variables",
        "Enum : Token name, List<LoxEnum.Element> elements",
        "Switch : Expr value, List<Stmt.Case> cases, Stmt.Case defaultCase",
        "Case: Expr value, Stmt body",
        "Break: Token keyword",
        "Continue: Token keyword",
        "Property: Token name, Modifiers modifiers, LoxType type, Stmt.Function get, Stmt.Function set",
        "Try: Stmt tryBranch, List<Stmt> catchBranch, Token exName"
      ));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void defineAst(String ouputDir, String baseName, List<String> types) throws IOException{
    String path = ouputDir + "/" + baseName + ".java";
    PrintWriter writer = new PrintWriter(path, "UTF-8");

    writer.println("package com.nix.lox;");
    writer.println();
    writer.println("import java.util.List;");
    writer.println();
    writer.println("abstract class " + baseName + " {");

    defineVisitor(writer, baseName, types);

    for(String type : types){
      String className = type.split(":")[0].trim();
      String fields = type.split(":")[1].trim();
      defineType(writer, baseName, className, fields);
    }

    writer.println();
    writer.println("   abstract <R> R accept(Visitor<R> visitor);");

    writer.println("}");
    writer.close();
  }

  private static void defineType(PrintWriter writer, String baseName, String className, String fieldList){
    writer.println("  static class " + className + " extends " +
        baseName + " {");

    // Constructor.
    writer.println("    " + className + "(" + fieldList + ") {");

    // Store parameters in fields.
    String[] fields = fieldList.split(", ");
    for (String field : fields) {
      String name = field.split(" ")[1];
      writer.println("      this." + name + " = " + name + ";");
    }

    writer.println("    }");

    writer.println();
    writer.println("    @Override");
    writer.println("    <R> R accept(Visitor<R> visitor) {");
    writer.println("      return visitor.visit" +
        className + baseName + "(this);");
    writer.println("    }");

    // Fields.
    writer.println();
    for (String field : fields) {
      writer.println("    final " + field + ";");
    }

    writer.println("  }");
  }

  private static void defineVisitor(
      PrintWriter writer, String baseName, List<String> types) {
    writer.println("  interface Visitor<R> {");

    for (String type : types) {
      String typeName = type.split(":")[0].trim();
      writer.println("    R visit" + typeName + baseName + "(" +
          typeName + " " + baseName.toLowerCase() + ");");
    }

    writer.println("  }");
  }
}
