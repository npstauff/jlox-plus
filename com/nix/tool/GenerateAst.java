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
        "Call     : Expr callee, Token paren, List<Expr> arguments, boolean nullCheck",
        "Get      : Expr object, Token name",
        "GetStatic: Expr object, Token name",
        "Coalesce      : Expr object, Token name",
        "Grouping : Expr expression",
        "Literal  : Object value",
        "Logical  : Expr left, Token operator, Expr right",
        "Set      : Expr object, Token name, Expr value",
        "Super    : Token keyword, Token method",
        "This     : Token keyword",
        "Unary    : Token operator, Expr right",
        "Variable : Token name",
        "New      : Token keyword, Expr callee, Token paren, List<Expr> arguments",
        "Typeof: Expr value"
      ));

      defineAst(ouputDir, "Stmt", Arrays.asList(
        "Block      : List<Stmt> statements",
        "Class      : Token name, Expr.Variable superclass," +
                  " List<Stmt.Function> methods, List<Stmt.Var> variables, List<Token> templates, List<Token> interfase",
        "Expression : Expr expression",
        "Function   : Token name, Token extClass, List<Parameter> params," +
                  " List<Stmt> body, boolean isStatic, boolean isConstant, Boolean hasBody, Boolean isoperator, LoxType returnType",
        "If         : Expr condition, Stmt thenBranch," +
                    " Stmt elseBranch",
        "When         : Expr condition, Stmt thenBranch, Stmt finallyBranch",
        "Return     : Token keyword, Expr value",
        "Expect     : Token keyword, Expr value",
        "Var        : Token name, Expr initializer, boolean isConstant, boolean isStatic, boolean pointer, LoxType type",
        "While      : Expr condition, Stmt body",
        "Test      : Expr name, Stmt body",
        "GetFile     : Token name, Expr path",
        "Module    : Token keyword",
        "Interface : Token name, List<Stmt.Function> methods, List<Stmt.Var> variables",
        "Enum : Token name, List<LoxEnum.Element> elements",
        "Switch : Expr value, List<Stmt.Case> cases, Stmt.Case defaultCase",
        "Case: Expr value, Stmt body",
        "Break: Token keyword",
        "Continue: Token keyword"
        
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
