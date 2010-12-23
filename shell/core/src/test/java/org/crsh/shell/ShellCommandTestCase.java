/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.crsh.shell;

import com.beust.jcommander.ParameterException;
import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import junit.framework.TestCase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.crsh.cmdline.processor.CmdSyntaxException;
import org.crsh.command.ClassCommand;
import org.crsh.command.ShellCommand;
import org.crsh.command.ScriptException;
import org.crsh.shell.impl.GroovyScriptCommand;
import org.kohsuke.args4j.CmdLineException;

import java.util.Arrays;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ShellCommandTestCase extends TestCase {


  /** . */
  private GroovyClassLoader loader;

  /** . */
  private GroovyShell shell;

  @Override
  protected void setUp() throws Exception {
    CompilerConfiguration config = new CompilerConfiguration();
    config.setScriptBaseClass(GroovyScriptCommand.class.getName());

    //
    loader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
    shell = new GroovyShell(loader);
  }

  public void testOptionInjectionInCommandClassArgs4j() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.ClassCommand { " +
      "@org.kohsuke.args4j.Option(name=\"-str\", required = true) def String str = 'default value';" +
      "public Object execute() {" +
      "return str;" +
      "}" +
      "}");

    //
    ClassCommand cmd = (ClassCommand)clazz.newInstance();
    assertEquals("abc", new TestCommandContext().execute(cmd, "-str", "abc"));
    try {
      new TestCommandContext().execute(cmd);
    }
    catch (ScriptException e) {
      assertTrue(e.getCause() instanceof CmdLineException);
    }
  }

  public void testOptionInjectionInCommandClassJCommander() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.ClassCommand { " +
      "@com.beust.jcommander.Parameter(names=\"-str\", required = true) def String str = 'default value';" +
      "public Object execute() {" +
      "return str;" +
      "}" +
      "}");

    //
    ClassCommand cmd = (ClassCommand)clazz.newInstance();
    assertEquals("abc", new TestCommandContext().execute(cmd, "-str", "abc"));
    try {
      new TestCommandContext().execute(cmd);
      fail();
    }
    catch (ScriptException e) {
      assert(e.getCause() instanceof ParameterException);
    }
  }

  public void testOptionInjectionInCommandClassCmdLine() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.CRaSHCommand { " +
      "@org.crsh.cmdline.Option(names=\"s\", required=true) def String str = 'default value';" +
      "@org.crsh.cmdline.Command\n" +
      "public Object main() {" +
      "return str;" +
      "}" +
      "}");

    //
    ShellCommand command = (ShellCommand)clazz.newInstance();
    assertEquals("abc", new TestCommandContext().execute(command, "-s", "abc"));
    try {
      new TestCommandContext().execute(command);
      fail();
    }
    catch (ScriptException e) {
      assert(e.getCause() instanceof CmdSyntaxException);
    }
  }

  public void testContextAccessFromCommandClassCmdLine() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.CRaSHCommand { " +
      "@org.crsh.cmdline.Command\n" +
      "public Object main() {" +
      "return juu;" +
      "}" +
      "}");

    //
    ShellCommand cmd = (ShellCommand)clazz.newInstance();
    TestCommandContext<Void, Void> ctx = new TestCommandContext();
    ctx.getAttributes().put("juu", "daa");
    assertEquals("daa", ctx.execute(cmd));
  }

  public void testArgumentInjectionInCommandClassArgs4j() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.ClassCommand { " +
      "@org.kohsuke.args4j.Argument def String str;" +
      "public Object execute() {" +
      "return str;" +
      "}" +
      "}");

    //
    ClassCommand cmd = (ClassCommand)clazz.newInstance();
    assertEquals("b", new TestCommandContext().execute(cmd, "b"));
  }

  public void testArgumentInjectionInCommandClassJCommander() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.ClassCommand { " +
      "@com.beust.jcommander.Parameter def List<String> str;" +
      "public Object execute() {" +
      "return str;" +
      "}" +
      "}");

    //
    ClassCommand cmd = (ClassCommand)clazz.newInstance();
//    assertEquals(Arrays.asList("b"), new TestCommandContext().execute(cmd, "b"));
  }

  public void testArgumentInjectionInCommandCmdLine() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.CRaSHCommand { " +
      "@org.crsh.cmdline.Command\n" +
      "public Object main(@org.crsh.cmdline.Argument String str) {" +
      "return str;" +
      "}" +
      "}");

    //
    ShellCommand cmd = (ShellCommand)clazz.newInstance();
    assertEquals("b", new TestCommandContext().execute(cmd, "b"));
  }

  public void testMainInCommandCmdLine() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.CRaSHCommand { " +
      "@org.crsh.cmdline.Command\n" +
      "public Object main() {" +
      "return 'foo';" +
      "}" +
      "}");

    //
    ShellCommand cmd = (ShellCommand)clazz.newInstance();
    assertEquals("foo", new TestCommandContext().execute(cmd));
  }

  public void testContextAccessInCommandClass() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.ClassCommand { " +
      "public Object execute() {" +
      "return bar;" +
      "}" +
      "}");

    //
    TestCommandContext ctx = new TestCommandContext();
    ctx.getAttributes().put("bar", "bar_value");

    // Execute directly
    ClassCommand cmd = (ClassCommand)clazz.newInstance();
    assertEquals("bar_value", ctx.execute(cmd));
  }

  public void testClosureInvocationInClass() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.ClassCommand { " +
      "public Object execute() {" +
      "return bar();" +
      "}" +
      "}");

    //
    TestCommandContext ctx = new TestCommandContext();
    Closure closure = (Closure)shell.evaluate("{ -> return 'from_closure'; }");
    ctx.getAttributes().put("bar", closure);

    // Execute directly
    ClassCommand cmd = (ClassCommand)clazz.newInstance();
    assertEquals("from_closure", ctx.execute(cmd));
  }

  public void testArgumentQuoteInClass() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.ClassCommand {\n" +
      "@org.kohsuke.args4j.Argument\n" +
      "def List<String> arguments;\n" +
      "public Object execute() {\n" +
      "return arguments;\n" +
      "}\n" +
      "}\n");

    // Execute directly
    ClassCommand cmd = (ClassCommand)clazz.newInstance();
    assertEquals("" + Arrays.asList("foo"), new TestCommandContext().execute(cmd, "'foo'"));
  }

  public void testArgumentQuoteInClass2() throws Exception {
    Class clazz = loader.parseClass("class foo extends org.crsh.command.ClassCommand {\n" +
      "@org.kohsuke.args4j.Argument\n" +
      "def List<String> arguments;\n" +
      "{ unquoteArguments = false; }\n" +
      "public Object execute() {\n" +
      "return arguments;\n" +
      "}\n" +
      "}\n");

    // Execute directly
    ClassCommand cmd = (ClassCommand)clazz.newInstance();
    assertEquals("" + Arrays.asList("'foo'"), new TestCommandContext().execute(cmd, "'foo'"));
  }



  public void testContextAccessInScript() throws Exception {
    Class clazz = loader.parseClass("System.out.println('bar:' + bar) ; return bar;");
    ShellCommand script = (ShellCommand)clazz.newInstance();
    TestCommandContext ctx = new TestCommandContext();
    ctx.getAttributes().put("bar", "bar_value");
    assertEquals("bar_value", ctx.execute(script));
  }

  public void testArgumentAccessInScript() throws Exception {
    Class clazz = loader.parseClass("return args[0];");
    ShellCommand script = (ShellCommand)clazz.newInstance();
    assertEquals("arg_value", new TestCommandContext().execute(script, "arg_value"));
  }

  public void testArgumentAccessInClosure() throws Exception {
    Class clazz = loader.parseClass("{ arg -> return arg };");
    ShellCommand script = (ShellCommand)clazz.newInstance();
    assertEquals("arg_value", new TestCommandContext().execute(script, "arg_value"));
  }
}
