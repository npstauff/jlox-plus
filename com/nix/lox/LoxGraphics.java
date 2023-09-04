package com.nix.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

class Rect{
  Rectangle rect;
  boolean filled;
  Color color;
  Rect(Rectangle rect, Color color, boolean filled){
    this.color = color;
    this.filled = filled;
    this.rect = rect;
  }
}

public class LoxGraphics extends LoxNative implements ActionListener{

  JFrame frame = null;
  JPanel panel = new JPanel();
  Graphics2D graphics = null;
  Timer t = null;
  boolean startMethodCalled = false;

  ArrayList<Rect> rects = new ArrayList<Rect>();

  LoxGraphics(Environment environment, Interpreter interpreter, LoxClass type) {
    super(null, null, null, null, type);
    setDetails("Graphics", new LoxObject(environment, interpreter, "Graphics", this.type), defineFunctions(environment), interpreter);
  }

  public Map<String, LoxFunction> defineFunctions(Environment environment){
    Map<String, LoxFunction> methods = new HashMap<>();
    methods.put("init", init(environment));
    methods.put("rect", rect(environment));
    methods.put("refresh", refresh(environment));
    methods.put("draw", draw(environment));
    return methods;
  }

  private LoxFunction refresh(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        rects.clear();
        panel.repaint();
        return null;
      }
      
    }, environment, false);
  }

  private LoxFunction draw(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        graphics = (Graphics2D)panel.getGraphics();
        for (Rect rect : rects) {
          graphics.setColor(rect.color);
          if(rect.filled) graphics.fill(rect.rect); else graphics.draw(rect.rect);
        }
        updateDimensions();
        return null;
      }
      
    }, environment, false);
  }

  private LoxFunction rect(Environment environment) {
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 6;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        int x = (int)Math.round((double)arguments.get(0));
        int y = (int)Math.round((double)arguments.get(1));
        int width = (int)Math.round((double)arguments.get(2));
        int height = (int)Math.round((double)arguments.get(3));
        boolean filled = (boolean)arguments.get(4);
        LoxInstance colorInstance = (LoxInstance)arguments.get(5);
        Color c = colorInstance != null ? getColor(colorInstance) : Color.black;
        rects.add(new Rect(new Rectangle(x, y, width, height), c, filled));
        return null;
      }
      
    }, environment, false);
  }

  Color getColor(LoxInstance instance){
    double r = (double)instance.klass.findField("r", false);
    double g = (double)instance.klass.findField("g", false);
    double b = (double)instance.klass.findField("b", false);
    double a = (double)instance.klass.findField("a", false);
    return new Color((int)r, (int)g, (int)b, (int)a);
  }

  public LoxFunction init(Environment environment){
    t = new Timer(10, this);
    t.start();
    return new LoxFunction(new LoxCallable() {

      @Override
      public int arity() {
        return 3;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        String windowName = arguments.get(0).toString();
        int width = (int)((double)arguments.get(1));
        int height = (int)((double)arguments.get(2));
        frame = new JFrame(windowName);
        frame.setPreferredSize(new Dimension(width, height));
        frame.setVisible(true);
        frame.add(panel);
        panel.setPreferredSize(new Dimension(width, height));
        frame.pack();
        frame.setLocationRelativeTo(null);

        updateDimensions();
        return null;
      }
      
    },environment, true);
  }

  @Override
  public void defineFields() {
    updateDimensions();
  }

  public void updateDimensions(){
    if(frame != null){
      put("width", (double)frame.getWidth(), false, false, false);
      put("height", (double)frame.getHeight(), false, false, false);
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if(interpreter.environment.values.get("update") != null){
      Object update = interpreter.environment.values.get("update").value;
      if(update instanceof LoxCallable){
        ((LoxCallable)update).call(interpreter, new ArrayList<>());
      }
    }
  }
  
}
