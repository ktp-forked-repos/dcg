package org.lolczak.dcg.parser.language.expr

import groovy.lang.{Binding, GroovyShell, Script}
import org.lolczak.dcg.model.FConst
import org.lolczak.dcg.parser.language.variable.VariableAssignment

import scala.collection.JavaConversions._
import scala.util.Try
import scalaz.\/

case class GroovyExprEval(imports: List[String] = List.empty) extends ExprEval {

  def this(importSingle: String) = this(List(importSingle))

  private val importedCode = imports.mkString("\n")

  override def evalExpr(exprCode: String, assignment: VariableAssignment): EvalFailure \/ String = {
    val sharedData = new Binding()
    assignment.forEach { case (varName, value) => sharedData.setVariable(varName, value.toString) }
    for {
      script <- compile(exprCode)
      result <- run(script, sharedData)
      value  <- \/.fromTryCatchNonFatal(result.toString) leftMap { case th => CastFailure(th.getMessage) }
    } yield value
  }

  override def evalGuard(guardCode: String, unifiedAssignment: VariableAssignment): EvalFailure \/ EvalResult = {
    val sharedData = new Binding()
    unifiedAssignment.forEach { case (varName, value) => sharedData.setVariable(varName, value.toString) }
    for {
      script <- compile(guardCode)
      result <- run(script, sharedData)
      variables = extractVariables(sharedData, unifiedAssignment)
      success = Try(result.toString.toBoolean).getOrElse(true)
    } yield EvalResult(variables, success)
  }

  private def compile(guardCode: String): EvalFailure \/ Script = {
    val code = importedCode + "\n" + guardCode
    val shell = new GroovyShell()
    \/.fromTryCatchNonFatal(shell.parse(code)) leftMap { case th => CompilationFailure(th.getMessage) }
  }

  private def run(script: Script, sharedData: Binding): EvalFailure \/ Any = {
    script.setBinding(sharedData)
    \/.fromTryCatchNonFatal(script.run()) leftMap { case th => ExecutionFailure(th.getMessage) }
  }

  private def extractVariables(sharedData: Binding, unifiedAssignment: VariableAssignment): VariableAssignment = {
    val allNames: Set[String] = sharedData.getVariables.keySet().toSet[Any].map(_.toString)
    val names: Set[String] = allNames -- unifiedAssignment.variables
    names.foldLeft(unifiedAssignment) {
      case (acc, varName) => acc.put(varName, FConst(sharedData.getVariable(varName).toString))
    }
  }

}
