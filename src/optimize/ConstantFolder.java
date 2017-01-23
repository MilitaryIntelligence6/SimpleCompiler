package optimize;

import ast.Ast;

/**
 * Created by Mengxu on 2017/1/23.
 */
public class ConstantFolder implements ast.Visitor
{
    private Ast.Exp.T lastExp;

    private boolean isConstant()
    {
        return lastExp != null && this.isConstant(this.lastExp);
    }

    private boolean isConstant(Ast.Exp.T exp)
    {
        return exp instanceof Ast.Exp.Num
                || exp instanceof Ast.Exp.True
                || exp instanceof Ast.Exp.False;
    }

    @Override
    public void visit(Ast.Type.Boolean t) {}

    @Override
    public void visit(Ast.Type.ClassType t) {}

    @Override
    public void visit(Ast.Type.Int t) {}

    @Override
    public void visit(Ast.Dec.DecSingle d) {}

    @Override
    public void visit(Ast.Exp.Add e)
    {
        this.visit(e.left);
        if (isConstant())
        {
            Ast.Exp.Num temLeft = (Ast.Exp.Num) this.lastExp;
            this.visit(e.right);
            if (isConstant())
                this.lastExp = new Ast.Exp.Num(
                        temLeft.num + ((Ast.Exp.Num) this.lastExp).num,
                        this.lastExp.lineNum);
            else this.lastExp = new Ast.Exp.Add(temLeft, this.lastExp, this.lastExp.lineNum);
        } else this.lastExp = e;
    }

    @Override
    public void visit(Ast.Exp.And e)
    {
        this.visit(e.left);
        if (isConstant())
        {
            boolean temLeft = this.lastExp instanceof Ast.Exp.True;
            this.visit(e.right);
            if (isConstant())
                this.lastExp = temLeft && this.lastExp instanceof Ast.Exp.True
                        ? new Ast.Exp.True(this.lastExp.lineNum)
                        : new Ast.Exp.False(this.lastExp.lineNum);
            else this.lastExp = new Ast.Exp.And(temLeft
                    ? new Ast.Exp.True(this.lastExp.lineNum)
                    : new Ast.Exp.False(this.lastExp.lineNum),
                    this.lastExp, this.lastExp.lineNum);
        } else this.lastExp = e;
    }

    @Override
    public void visit(Ast.Exp.Call e)
    {
        java.util.LinkedList<Ast.Exp.T> _args = new java.util.LinkedList<>();
        e.args.forEach(arg ->
        {
            this.visit(arg);
            _args.add(this.lastExp);
        });
        e.args = _args;
        this.lastExp = e;
    }

    @Override
    public void visit(Ast.Exp.False e)
    {
        this.lastExp = e;
    }

    @Override
    public void visit(Ast.Exp.Id e)
    {
        this.lastExp = e;
    }

    @Override
    public void visit(Ast.Exp.LT e)
    {
        this.visit(e.left);
        if (isConstant())
        {
            Ast.Exp.Num temLeft = (Ast.Exp.Num) this.lastExp;
            this.visit(e.right);
            if (isConstant())
                this.lastExp = temLeft.num < ((Ast.Exp.Num) this.lastExp).num
                        ? new Ast.Exp.True(this.lastExp.lineNum)
                        : new Ast.Exp.False(this.lastExp.lineNum);
            else this.lastExp = new Ast.Exp.LT(temLeft, this.lastExp, this.lastExp.lineNum);
        } else this.lastExp = e;
    }

    @Override
    public void visit(Ast.Exp.NewObject e)
    {
        this.lastExp = e;
    }

    @Override
    public void visit(Ast.Exp.Not e)
    {
        this.visit(e.exp);
        if (isConstant())
            this.lastExp = this.lastExp instanceof Ast.Exp.True
                    ? new Ast.Exp.False(this.lastExp.lineNum)
                    : new Ast.Exp.True(this.lastExp.lineNum);
    }

    @Override
    public void visit(Ast.Exp.Num e)
    {
        this.lastExp = e;
    }

    @Override
    public void visit(Ast.Exp.Sub e)
    {
        this.visit(e.left);
        if (isConstant())
        {
            Ast.Exp.Num temLeft = (Ast.Exp.Num) this.lastExp;
            this.visit(e.right);
            if (isConstant())
                this.lastExp = new Ast.Exp.Num(
                        temLeft.num - ((Ast.Exp.Num) this.lastExp).num,
                        this.lastExp.lineNum);
            else this.lastExp = new Ast.Exp.Sub(temLeft, this.lastExp, this.lastExp.lineNum);
        } else this.lastExp = e;
    }

    @Override
    public void visit(Ast.Exp.This e)
    {
        this.lastExp = e;
    }

    @Override
    public void visit(Ast.Exp.Times e)
    {
        this.visit(e.left);
        if (isConstant())
        {
            Ast.Exp.Num temLeft = (Ast.Exp.Num) this.lastExp;
            this.visit(e.right);
            if (isConstant())
                this.lastExp = new Ast.Exp.Num(
                        temLeft.num * ((Ast.Exp.Num) this.lastExp).num,
                        this.lastExp.lineNum);
            else this.lastExp = new Ast.Exp.Times(temLeft, this.lastExp, this.lastExp.lineNum);
        } else this.lastExp = e;
    }

    @Override
    public void visit(Ast.Exp.True e)
    {
        this.lastExp = e;
    }

    @Override
    public void visit(Ast.Stm.Assign s)
    {
        this.visit(s.exp);
        s.exp = this.lastExp;
    }

    @Override
    public void visit(Ast.Stm.Block s)
    {
        s.stms.forEach(this::visit);
    }

    @Override
    public void visit(Ast.Stm.If s)
    {
        this.visit(s.condition);
        s.condition = this.lastExp;
        this.visit(s.then_stm);
        this.visit(s.else_stm);
    }

    @Override
    public void visit(Ast.Stm.Print s)
    {
        this.visit(s.exp);
        s.exp = this.lastExp;
    }

    @Override
    public void visit(Ast.Stm.While s)
    {
        this.visit(s.condition);
        s.condition = this.lastExp;
        this.visit(s.body);
    }

    @Override
    public void visit(Ast.Method.MethodSingle m)
    {
        m.stms.forEach(this::visit);
        this.visit(m.retExp);
        m.retExp = this.lastExp;
    }

    @Override
    public void visit(Ast.Class.ClassSingle c)
    {
        c.methods.forEach(this::visit);
    }

    @Override
    public void visit(Ast.MainClass.MainClassSingle c)
    {
        this.visit(c.stm);
    }

    @Override
    public void visit(Ast.Program.ProgramSingle p)
    {
        this.visit(p.mainClass);
        p.classes.forEach(this::visit);
    }
}
