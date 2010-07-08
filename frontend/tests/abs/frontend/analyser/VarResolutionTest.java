package abs.frontend.analyser;

import org.junit.Test;

import static org.junit.Assert.*;

import abs.frontend.FrontendTest;
import abs.frontend.ast.ClassDecl;
import abs.frontend.ast.Exp;
import abs.frontend.ast.FieldDecl;
import abs.frontend.ast.FieldUse;
import abs.frontend.ast.LetExp;
import abs.frontend.ast.Model;
import abs.frontend.ast.NegExp;
import abs.frontend.ast.ParamDecl;
import abs.frontend.ast.PatternVarDecl;
import abs.frontend.ast.ReturnStmt;
import abs.frontend.ast.VarDecl;
import abs.frontend.ast.VarOrFieldDecl;
import abs.frontend.ast.VarUse;

import static abs.common.StandardLib.STDLIB_STRING;


public class VarResolutionTest extends FrontendTest {
    @Test
    public void testLocalVar() {
        Exp e = getFirstExp("interface I { } { I i; i = i; }");
        VarUse u = (VarUse) e;
        VarDecl decl = (VarDecl) u.getDecl();
        assertEquals("i",decl.getName());
    }
    
    @Test
    public void testPatternVar() {
        Model m = assertParseOk(STDLIB_STRING + " def Bool f(Bool b) = case b { True => False; x => ~x; }");
        NegExp ne = (NegExp) getFirstCaseExpr(m);
        VarUse v = (VarUse) ne.getOperand();
        PatternVarDecl decl = (PatternVarDecl) v.getDecl();
        assertEquals("x",decl.getName());
    }

    @Test
    public void testFunctionParam() {
        Model m = assertParseOk(STDLIB_STRING + " def Bool f(Bool b) = b");
        VarUse u = (VarUse) getFirstFunctionExpr(m);
        ParamDecl d = (ParamDecl) u.getDecl();
        assertEquals("b", d.getName());

    }
    
    @Test
    public void testLetExp() {
        Model m = assertParseOk(STDLIB_STRING + " def Bool f(Bool b) = let (Bool x) = b in x");
        LetExp e = (LetExp) getFirstFunctionExpr(m);
        VarOrFieldDecl decl = e.getVar();
        VarUse u = (VarUse) e.getExp();
        assertEquals(decl, u.getDecl());

    }

    @Test
    public void testNestedLetExp() {
        Model m = assertParseOk(STDLIB_STRING + " def Bool f(Bool b) = let (Bool x) = let (Bool y) = b in y in x");
        LetExp e = (LetExp) getFirstFunctionExpr(m);
        VarOrFieldDecl decl = e.getVar();
        VarUse u = (VarUse) e.getExp();
        assertEquals(decl, u.getDecl());

    }

    @Test
    public void testNestedLetExp2() {
        Model m = assertParseOk(STDLIB_STRING + " def Bool f(Bool b) = let (Bool x) = let (Bool x) = b in x in x");
        LetExp e = (LetExp) getFirstFunctionExpr(m);
        VarOrFieldDecl decl = e.getVar();
        VarUse u = (VarUse) e.getExp();
        assertEquals(decl, u.getDecl());

    }

    @Test
    public void testNestedLetExp3() {
        Model m = assertParseOk(STDLIB_STRING + " def Bool f(Bool b) = let (Bool x) = b in let (Bool y) = b in x");
        LetExp e = (LetExp) getFirstFunctionExpr(m);
        LetExp e2 = (LetExp) e.getExp();
        VarOrFieldDecl decl = e.getVar();
        VarUse u = (VarUse) e2.getExp();
        assertEquals(decl, u.getDecl());
    }
    
    @Test
    public void testNestedLetExp4() {
        Model m = assertParseOk(STDLIB_STRING + " def Bool f(Bool b) = let (Bool x) = b in let (Bool x) = b in x");
        LetExp e = (LetExp) getFirstFunctionExpr(m);
        LetExp e2 = (LetExp) e.getExp();
        VarOrFieldDecl decl = e2.getVar();
        VarUse u = (VarUse) e2.getExp();
        assertEquals(decl, u.getDecl());
    }
    
    @Test
    public void testNestedLetExp5() {
        Model m = assertParseOk(STDLIB_STRING + "def Bool f(Bool b) = let (Bool x) = b in let (Bool x) = x in x");
        LetExp e = (LetExp) getFirstFunctionExpr(m);
        LetExp e2 = (LetExp) e.getExp();
        VarOrFieldDecl decl = e.getVar();
        VarUse u = (VarUse) e2.getVal();
        assertEquals(decl, u.getDecl());
    }

    @Test
    public void testFieldUse() {
        Model m = assertParseOk(STDLIB_STRING + " class C { Bool f; Bool m() { return this.f; } }");
        ClassDecl d = (ClassDecl) m.localLookup("C");
        FieldDecl f = d.getField(0);
        ReturnStmt s = (ReturnStmt) d.getMethod(0).getBlock().getStmt(0);
        assertEquals(f, ((FieldUse)s.getRetExp()).getDecl());
    }
    
}
