package parser;

import ast.Ast;
import lexer.Lexer;
import lexer.Token;
import lexer.Token.Kind;

import java.io.InputStream;
import java.util.LinkedList;

/**
 * Created by Mengxu on 2017/1/11.
 */
public class Parser
{
    Lexer lexer;
    Token current;

    public Parser(InputStream fstream)
    {
        lexer = new Lexer(fstream);
        current = lexer.nextToken();
    }

    // utility methods
    private void advance()
    {
        current = lexer.nextToken();
    }

    private void eatToken(Kind kind)
    {
        if (kind == current.kind)
            advance();
        else
        {
            System.out.println("Expects: " + kind.toString());
            System.out.println("But got: " + current.kind.toString() + " at line " + current.lineNum);
            System.exit(1);
        }
    }

    private void error()
    {
        System.out.println("Syntax error: compilation aborting...\n");
        System.exit(1);
    }

    // parse methods

    // ExpList -> Exp ExpRest*
    //         ->
    // ExpRest -> , Exp
    private LinkedList<Ast.Exp.T> parseExpList()
    {
        LinkedList<Ast.Exp.T> explist = new LinkedList<>();
        if (current.kind == Kind.Rparen)
            return explist;
        Ast.Exp.T tem = parseExp();
        tem.lineNum = current.lineNum;
        explist.addLast(tem);
        while (current.kind == Kind.Commer)
        {
            advance();
            tem = parseExp();
            tem.lineNum = current.lineNum;
            explist.add(tem);
        }
        return explist;
    }

    // AtomExp -> (exp)
    //  -> Integer Literal
    //  -> true
    //  -> false
    //  -> this
    //  -> id
    //  -> new id()
    private Ast.Exp.T parseAtomExp()
    {
        Ast.Exp.T exp;
        switch (current.kind)
        {
            case Lparen:
                advance();
                exp = parseExp();
                exp.lineNum = current.lineNum;
                //advance();
                eatToken(Kind.Rparen);
                return exp;
            case NUM:
                exp = new Ast.Exp.Num(Integer.parseInt(current.lexeme),
                        current.lineNum);
                advance();
                return exp;
            case True:
                exp = new Ast.Exp.True(current.lineNum);
                advance();
                return exp;
            case False:
                exp = new Ast.Exp.False(current.lineNum);
                advance();
                return exp;
            case This:
                exp = new Ast.Exp.This(current.lineNum);
                advance();
                return exp;
            case ID:
                exp = new Ast.Exp.Id(current.lexeme, current.lineNum);
                advance();
                return exp;
            case New:
                advance();
                exp = new Ast.Exp.NewObject(current.lexeme, current.lineNum);
                advance();
                eatToken(Kind.Lparen);
                eatToken(Kind.Rparen);
                return exp;
            default:
                error();
                return null;
        }
    }

    // NotExp -> AtomExp
    //  -> AtomExp.id(expList)
    private Ast.Exp.T parseNotExp()
    {
        Ast.Exp.T exp = parseAtomExp();
        while (current.kind == Kind.Dot)
        {
            advance();
            Token id = current;
            eatToken(Kind.ID);
            eatToken(Kind.Lparen);
            exp = new Ast.Exp.Call(exp, id.lexeme, parseExpList(), id.lineNum);
            eatToken(Kind.Rparen);
        }
        return exp;
    }

    // TimesExp -> ! TimesExp
    //  -> NotExp
    private Ast.Exp.T parseTimesExp()
    {
        int i = 0;
        while (current.kind == Kind.Not)
        {
            advance();
            i++;
        }
        Ast.Exp.T exp = parseNotExp();
        Ast.Exp.T tem = new Ast.Exp.Not(exp, exp.lineNum);
        return i%2 == 0 ? exp : tem;
    }

    // AddSubExp -> TimesExp * TimesExp
    //  -> TimesExp
    private Ast.Exp.T parseAddSubExp()
    {
        Ast.Exp.T tem = parseTimesExp();
        Ast.Exp.T exp = tem;
        while (current.kind == Kind.Times)
        {
            advance();
            tem = parseTimesExp();
            exp = new Ast.Exp.Times(exp, tem, tem.lineNum);
        }
        return exp;
    }

    // LtExp -> AddSubExp + AddSubExp
    //  -> AddSubExp - AddSubExp
    //  -> AddSubExp
    private Ast.Exp.T parseLTExp()
    {
        Ast.Exp.T exp = parseAddSubExp();
        while (current.kind == Kind.Add || current.kind == Kind.Sub)
        {
            boolean isAdd = current.kind == Kind.Add;
            advance();
            Ast.Exp.T tem = parseAddSubExp();
            exp = isAdd ? new Ast.Exp.Add(exp, tem, exp.lineNum)
                    : new Ast.Exp.Sub(exp, tem, exp.lineNum);
        }
        return exp;
    }

    // AndExp -> LtExp < LtExp
    // -> LtExp
    private Ast.Exp.T parseAndExp()
    {
        Ast.Exp.T exp = parseLTExp();
        while (current.kind == Kind.LT)
        {
            advance();
            Ast.Exp.T tem = parseLTExp();
            exp = new Ast.Exp.LT(exp, tem, exp.lineNum);
        }
        return exp;
    }

    // Exp -> AndExp && AndExp
    //  -> AndExp
    private Ast.Exp.T parseExp()
    {
        Ast.Exp.T exp = parseAndExp();
        while (current.kind == Kind.And)
        {
            advance();
            Ast.Exp.T tem = parseAndExp();
            exp = new Ast.Exp.And(exp, tem, exp.lineNum);
        }
        return exp;
    }
}
