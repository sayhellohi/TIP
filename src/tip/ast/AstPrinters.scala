package tip.ast

object AstPrinters {

  def withRelevantLocations(): PartialFunction[AstNode, String] = {
    case id: AIdentifierDeclaration =>
      s"${id.print(PartialFunction.empty)}:${id.loc}"
    case id: AAlloc =>
      s"${id.print(PartialFunction.empty)}:${id.loc}"
  }

  def withAllLocations(): PartialFunction[AstNode, String] = {
    case n: AstNode =>
      s"${n.print(PartialFunction.empty)}:${n.loc}"
  }

  /**
    * Implicit class that makes a 'print' method available on 'AstNode' objects.
    *
    * (For information about implicit classes, see [[tip.ast.AstNodeData.AstNodeWithDeclaration]].)
    */
  implicit class DefaultRecursivePrinter(n: AstNode) {

    def print(printer: PartialFunction[AstNode, String]): String =
      printer.applyOrElse(n, {
        n: AstNode =>
          n match {
            case ACallFuncExpr(targetFun, args, _) =>
              s"${targetFun.print(printer)}(${args.map(_.print(printer)).mkString(",")})"
            case AIdentifier(value, _) =>
              value
            case ABinaryOp(operator, left, right, _) =>
              s"(${left.print(printer)} $operator ${right.print(printer)})"
            case AUnaryOp(operator, target, _) =>
              s"($operator${target.print(printer)})"
            case ANumber(value, _) =>
              value.toString
            case AInput(_) =>
              "input"
            case AAlloc(e, _) =>
              s"alloc ${e.print(printer)}"
            case ARecord(fields, _) =>
              fields.map { f =>
                s"${f.field}:${f.exp.print(printer)}"
              }.mkString("{", ",", "}")
            case AAccess(record, field, _) =>
              s"${record.print(printer)}.$field"
            case ANull(_) =>
              "null"
            case AIdentifierDeclaration(value, _) =>
              value
            case AFunDeclaration(name, args, stmts, _) =>
              s"$name(${args.map(_.print(printer)).mkString(",")})\n${stmts.print(printer)}"
            case AAssignStmt(left, right, _) =>
              s"${left.print(printer)} = ${right.print(printer)};"
            case AIfStmt(guard, ifBranch, elseBranch, _) =>
              val elseb = elseBranch.map(x => "else " + x.print(printer)).getOrElse("")
              s"if (${guard.print(printer)}) ${ifBranch.print(printer)} $elseb"
            case AOutputStmt(value, _) =>
              s"output ${value.print(printer)};"
            case AErrorStmt(value, _) =>
              s"error ${value.print(printer)};"
            case AWhileStmt(guard, innerBlock, _) =>
              s"while (${guard.print(printer)}) ${innerBlock.print(printer)}"
            case block: ABlock =>
              s"{\n${block.body.map(_.print(printer)).mkString("\n")}\n}"
            case AReturnStmt(value, _) =>
              s"return ${value.print(printer)};"
            case AVarStmt(declIds, _) =>
              s"var ${declIds.map(_.print(printer)).mkString(",")};"
            case AProgram(funs, _) =>
              s"${funs.map(_.print(printer)).mkString("\n\n")}"
          }
      })
  }
}
