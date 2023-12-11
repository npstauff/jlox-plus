package com.nix.lox;

import java.util.List;

public interface LoxCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments, List<LoxClass> generics);
}
