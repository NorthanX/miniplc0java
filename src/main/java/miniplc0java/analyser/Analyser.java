package miniplc0java.analyser;

import miniplc0java.function.Function;
import miniplc0java.function.MyFunctions;
import miniplc0java.global.Global;
import miniplc0java.instruction.Instruction;
import miniplc0java.operate.Operator;
import miniplc0java.symbol.Symbol;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public final class Analyser {

    Tokenizer tokenizer;

    /**指令表*/
    ArrayList<Instruction> instructions;

    /**偷看的token*/
    Token peekedToken = null;

    /** 下一个变量的栈偏移 */
    int nextOffset = 0;

    /**当前层数*/
    int floor = 0;

    /**全局变量个数*/
    int globalVCount = 0;

    /**局部变量个数*/
    int localVCount = 0;

    /**函数总个数*/
    int functionCount = 0;

    /**符号表创建*/
    List<Symbol> symbolTable = new ArrayList<>();

    /**函数表创建*/
    List<Function> functionTable = new ArrayList<>();

    /**全局符号表*/
    List<Global> globalTable = new ArrayList<>();

    /***/
    List<Instruction> allInstructions;

    /** 操作符号栈 */
    Stack<TokenType> op = new Stack<>();

    /**当前分析的函数*/
    Symbol analFunction;

    /**返回函数*/
    Symbol retFunction;

    /**循环层数*/
    int circulateLayer = 0;

    /** 开始函数 */
    Function _start;

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws Exception
     */
    private boolean check(TokenType tt) throws Exception {
        Token token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 查看下一个 Token
     *
     * @return
     * @throws Exception
     */
    private Token peek() throws Exception {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    private Token next() throws Exception {
        if (peekedToken != null) {
            Token token = peekedToken;
            peekedToken = null;
            System.out.print(token.getValue() + " ");
            return token;
        } else {
            return tokenizer.nextToken();
        }
    }


    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     *
     * @param tt 类型
     * @return 这个 token
     * @throws Exception 如果类型不匹配
     */
    private Token expect(TokenType tt) throws Exception {
        Token token = peek();
        if (token.getTokenType() == tt) {
            return next();
        }
        else {
            throw new Exception();
        }
    }

    /**
     * 获取下一个变量的栈偏移
     *
     * @return
     */
    private int getNextVariableOffset() {
        return this.nextOffset++;
    }

    private Symbol addSymbol (Symbol symbol) throws Exception {
        Token token = new Token(TokenType.None, symbol.getName(), null, null);
        //判断在符号表里有没有与当前符号相同的名字
        int symbol1 = searchSymbolNum(String.valueOf(token.getValue()));
        //如果没有一样的名字
        if(symbol1 == -1){
            this.symbolTable.add(symbol);
        }
        //如果有相同名字
        else{
            //获取进行比较
            symbol = symbolTable.get(symbol1);
            //如果他们层数一样，则冲突
            if(symbol.getFloor() == floor)
                throw new Exception();
            //如果层数不一样则加入符号表
            this.symbolTable.add(symbol);
        }
        return symbolTable.get(symbolTable.size()-1);
    }

    private int searchSymbolNumByName(String name){
        //在符号表里查找
        for(int i=0; i<symbolTable.size(); i++){
            //如果遇到名字相同的则返回位置
            if(symbolTable.get(i).getName().equals(name))
                return i;
        }
        //符号表里没有名字相同的，返回-1
        return -1;
    }

    /**
     * 最开始的语法分析
     * @throws Exception
     */
    public void startAnalyse() throws Exception{


        instructions = new ArrayList<>();

        //program -> decl_stmt* function*
        //decl_stmt -> let_decl_stmt | const_decl_stmt
        analyseDecl_Stmt();

        allInstructions = instructions;
        //function -> 'fn' IDENT '(' function_param_list? ')' '->' ty block_stmt
        analyseFunction();

        //判断符号表里有没有主程序入口
        //如果没有则报错
        int mainLoca = searchSymbolNumByName("main");
        if(mainLoca == -1){
            throw new Exception();
        }

        //在全局符号表里填入入口程序_start
        globalTable.add(new Global(true, 6, "_start"));
        //找到main函数对应的符号
        Symbol main = symbolTable.get(mainLoca);
        if (!main.getReturnType().equals("void")) {
            //加载地址
            allInstructions.add(new Instruction("stackalloc", 1));
            allInstructions.add(new Instruction("call", functionCount-1));
            allInstructions.add(new Instruction("popn", 1));
        }
        else {
            //加载地址
            allInstructions.add(new Instruction("stackalloc", 0));
            allInstructions.add(new Instruction("call", functionCount-1));
        }
        _start = new Function("_start", floor, globalVCount, 0, 0, 0, allInstructions);
        globalVCount++;
    }

    /**
     * 语法分析函数声明
     * function -> 'fn' IDENT '(' function_param_list? ')' '->' ty block_stmt
     * @throws Exception
     */
    private void analyseFunction() throws Exception{
        while(check(TokenType.FN_KW)){


            allInstructions.addAll(instructions);
            //指令集更新
            instructions = new ArrayList<>();
            localVCount = 0;
            List<Symbol> params = new ArrayList<>();

            expect(TokenType.FN_KW);
            Token ident = expect(TokenType.IDENT);
            String identName = String.valueOf(ident.getValue());
            expect(TokenType.L_PAREN);
            Symbol symbol = new Symbol(identName, true, "function", "", true,
                    getNextVariableOffset(), floor, -1,params, null, -1, globalVCount);
            symbol = addSymbol(symbol);
            
            //参数符号分析
            if(!check(TokenType.R_PAREN))
                analyseFunction_Param_List(params, symbol);
            expect(TokenType.R_PAREN);

            expect(TokenType.ARROW);
            String returnType =analyseTy();
            symbol.setParams(params);symbol.setReturnType(returnType);
            analFunction = symbol;

            //判断返回类型
            int retType;
            if (returnType.equals("int"))   retType = 1;
            else retType = 0;
            Function function = new Function(identName, floor, globalVCount, retType,
                    params.size(), localVCount, instructions);
            functionTable.add(function);

            analyseBlock_Stmt();


            globalVCount++;
            functionCount++;

        }

    }

    /**
     * block_stmt -> '{' stmt* '}'
     * @throws Exception
     */
    private void analyseBlock_Stmt() throws Exception{
        floor++;
        expect(TokenType.L_BRACE);
        analysStmt();
        expect(TokenType.R_BRACE);

        int i = symbolTable.size()-1;
        while (true){
            int floorX = symbolTable.get(i).getFloor();
            if (floorX!=floor){
                break;
            }
            symbolTable.remove(i);
        }

        floor--;
    }

    /**
     * stmt ->
     *       expr_stmt
     *     | decl_stmt
     *     | if_stmt
     *     | while_stmt
     *     | return_stmt
     *     | block_stmt
     *     | empty_stmt
     * @throws Exception
     */
    private void analysStmt() throws Exception {
        while (!check(TokenType.R_BRACE)){
            if (check(TokenType.LET_KW) || check(TokenType.CONST_KW)){
                analyseDecl_Stmt();
            }
            else if (check(TokenType.IF_KW)){
                analyseIf_Stmt();
            }
            else if (check(TokenType.WHILE_KW)){
                analyseWhile_Stmt();
            }
            else if (check(TokenType.RETURN_KW)){
                analyseReturnStmt();
            }
            else if (check(TokenType.L_BRACE)){
                analyseBlock_Stmt();
            }
            else if (check(TokenType.SEMICOLON)){
                analyseEmpty_Stmt();
            }
            else {
                analyseExpr_Stmt();
            }
        }

    }

    /**
     * function_param_list -> function_param (',' function_param)*
     * @param params
     * @param symbol
     */
    private void analyseFunction_Param_List(List<Symbol> params, Symbol symbol) throws Exception {
        int i = 0;
        Symbol param_Symbol = ananalyseFunction_Param(i, symbol);
        params.add(param_Symbol);
        while (check(TokenType.COMMA)){
            i++;
            expect(TokenType.COMMA);
            param_Symbol = ananalyseFunction_Param(i, symbol);
            params.add(param_Symbol);
        }

    }

    /**
     * function_param -> 'const'? IDENT ':' ty
     * @return
     */
    private Symbol ananalyseFunction_Param(int i, Symbol symbol) throws Exception {
        boolean isConst;
        String type;
        Token ident;

        if (check(TokenType.CONST_KW)){
            expect(TokenType.CONST_KW);
            isConst = true;
        }
        else isConst = false;

        ident = expect(TokenType.IDENT);
        expect(TokenType.COLON);
        type = analyseTy();

        Symbol param_Symbol = new Symbol(String.valueOf(ident.getValue()), isConst, type, "", false,
                getNextVariableOffset(), floor+1, i, null, symbol, -1, -1);
        param_Symbol = addSymbol(param_Symbol);
        return param_Symbol;
    }

    /**
     * if_stmt -> 'if' expr block_stmt ('else' (block_stmt | if_stmt))?
     * @throws Exception
     */
    private void analyseIf_Stmt() throws Exception{
        expect(TokenType.IF_KW);
        String funcType =analyseExpr();

        //判断返回类型
        if(funcType.equals("void"))
            throw new Exception();

        //弹栈
        while (!op.empty())
            MyFunctions.operatorInstructions(op.pop(), instructions, funcType);

        //如果前面的计算值非0则跳转
        instructions.add(new Instruction("br.true", 1));
        //无条件跳转
        Instruction jump = new Instruction("br", 0);
        instructions.add(jump);
        //当前指令位置
        int index = instructions.size();

        analyseBlock_Stmt();

        //有return
        if (instructions.get(instructions.size() -1).getOpt().equals("ret")) {
            jump.setX(instructions.size() - index);

            if(check(TokenType.ELSE_KW)){
                expect(TokenType.ELSE_KW);
                if(check(TokenType.L_BRACE)){
                    analyseBlock_Stmt();
                    instructions.add(new Instruction("br", 0));
                }
                else if(check(TokenType.IF_KW))
                    analyseIf_Stmt();
            }
        }
        else {
            Instruction jump1 = new Instruction("br", null);
            instructions.add(jump1);
            int j = instructions.size();

            int distance = j - index;
            jump.setX(distance);

            if(check(TokenType.ELSE_KW)){
                expect(TokenType.ELSE_KW);
                if(check(TokenType.L_BRACE)){
                    analyseBlock_Stmt();
                    instructions.add(new Instruction("br", 0));
                }
                else if(check(TokenType.IF_KW))
                    analyseIf_Stmt();
            }
            distance = instructions.size() - j;
            jump1.setX(distance);
        }


    }

    /**
     * while_stmt -> 'while' expr block_stmt
     * @throws Exception
     */
    private void analyseWhile_Stmt() throws Exception{
        expect(TokenType.WHILE_KW);

        instructions.add(new Instruction("br", 0));
        int whileStart = instructions.size();

        String type = analyseExpr();
        //返回类型必须是int
        if(type.equals("void"))
            throw new Exception();

        //弹栈
        while (!op.empty())
            MyFunctions.operatorInstructions(op.pop(), instructions, type);

        instructions.add(new Instruction("br.true", 1));
        Instruction jump = new Instruction("br", 0);
        instructions.add(jump);
        int index = instructions.size();

        circulateLayer++;
        analyseBlock_Stmt();

        //跳回while 判断语句
        Instruction instruction = new Instruction("br", 0);
        instructions.add(instruction);

        int whileEnd = instructions.size();
        jump.setX(whileEnd - index);
        instruction.setX(whileStart - whileEnd);
        if(circulateLayer > 0)
            circulateLayer--;
    }

    /**
     * return_stmt -> 'return' expr? ';'
     * @throws Exception
     */
    private void analyseReturnStmt() throws Exception{
        String type;
        expect(TokenType.RETURN_KW);

        //如果返回类型是int
        if(analFunction.getReturnType().equals("int")){
            //加载返回地址
            instructions.add(new Instruction("arga", 0));

            type = analyseExpr();
            while (!op.empty())
                MyFunctions.operatorInstructions(op.pop(), instructions, type);

            instructions.add(new Instruction("store.64", null));
        }
        else
            type = "void";

        if(!check(TokenType.SEMICOLON))
            type = analyseExpr();

        //判断返回类型和函数的应有返回类型是否一致
        //一致则返回
        //不一致则报错
        if(!type.equals(analFunction.getReturnType()))
            throw new Exception();

        expect(TokenType.SEMICOLON);
        retFunction = analFunction;

        while (!op.empty())
            MyFunctions.operatorInstructions(op.pop(), instructions, type);
        //ret
        instructions.add(new Instruction("ret", null));
    }

    private void analyseEmpty_Stmt() throws Exception{
        expect(TokenType.SEMICOLON);
    }

    /**
     * expr_stmt -> expr ';'
     * @throws Exception
     */
    private void analyseExpr_Stmt() throws Exception{
        String exprType = analyseExpr();
        //弹栈
        while (!op.empty())
            MyFunctions.operatorInstructions(op.pop(), instructions, exprType);
        expect(TokenType.SEMICOLON);
    }

    /**
     * expr ->
     *       operator_expr
     *     | negate_expr
     *     | assign_expr
     *     | as_expr
     *     | call_expr
     *     | literal_expr
     *     | ident_expr
     *     | group_expr
     * @throws Exception
     */
    private String analyseExpr() throws Exception{
        //当前表达式的类型
        String exprType = "";
        boolean isLibrary;

        //取反表达式处理
        //negate_expr -> '-' expr
        if(check(TokenType.MINUS))
            exprType = analyseNegate_Expr();

            //首位是标识符,可能是assign_expr，call_expr，ident_expr
        else if(check(TokenType.IDENT)){

            Token ident = expect(TokenType.IDENT);
            Symbol symbol = searchSymbol(ident);

            isLibrary = false;
            //如果符号表里没有该ident
            if(symbol == null){
                if(analyseLibrary(String.valueOf(ident.getValue())) == null)
                    throw new Exception();
                isLibrary = true;
            }

            //assign_expr -> l_expr '=' expr
            //l_expr -> IDENT
            if(check(TokenType.ASSIGN))
                //查看赋值表达式左边的标识符是否在符号表里
                exprType = analyseAssign_Expr(symbol);

            //call_expr -> IDENT '(' call_param_list? ')'
            else if(check(TokenType.L_PAREN))
                exprType = analyseCall_Expr(symbol, isLibrary);

            //ident_expr -> IDENT
            else{
                exprType = analyseIdent_Expr(symbol);
            }
        }

        //literal_expr -> UINT_LITERAL | DOUBLE_LITERAL | STRING_LITERAL
        else if(check(TokenType.UINT_LITERAL) || check(TokenType.STRING_LITERAL)){
            exprType = analyseLiteralExpr();
        }

        //group_expr -> '(' expr ')'
        else if(check(TokenType.L_PAREN))
            exprType = analyseGroupExpr();

        //类型转换表达式，运算符表达式
        //如果依旧有expr
        while(check(TokenType.AS_KW) ||
                check(TokenType.PLUS)||
                check(TokenType.MINUS)||
                check(TokenType.MUL)||
                check(TokenType.DIV)||
                check(TokenType.EQ)||
                check(TokenType.NEQ)||
                check(TokenType.LT)||
                check(TokenType.GT)||
                check(TokenType.LE)||
                check(TokenType.GE)){
            //类型转换表达式
            //as_expr -> expr 'as' ty
            //暂时不管吧
            if(check(TokenType.AS_KW))
                exprType = analyseAs_Expr(exprType);

                //运算符表达式
                //operator_expr -> expr binary_operator expr
            else
                exprType = analyseOperator_Expr(exprType);
        }
        //如果成功给表达式赋了类型，则说明上面至少有表达式成立
        if(exprType.equals(""))
            throw new Exception();
            //根本不符合表达式语句
        return exprType;

    }

    /**
     * as_expr -> expr 'as' ty
     * @param exprType
     * @return
     * @throws Exception
     */
    private String analyseAs_Expr(String exprType) throws Exception {
        expect(TokenType.AS_KW);
        String rightType =  analyseTy();
        if(exprType.equals(rightType)){
            return exprType;
        }
        else
            throw new Exception();
    }

    private Symbol analyseLibrary(String name) throws Exception{
        List<Symbol> params = new ArrayList<>();
        Symbol param = new Symbol();
        String returnType;

        if(name.equals("getint")){
            returnType = "int";
            return new Symbol(name, false, "function", returnType, true, 0, floor, -1, params, null,  -1, -1);
        }
        else if(name.equals("getdouble")){
            returnType = "double";
            return new Symbol(name, false, "function", returnType, true, 0, floor, -1, params, null,  -1, -1);
        }
        else if(name.equals("getchar")){
            returnType = "int";
            return new Symbol(name, false, "function", returnType, true, 0, floor, -1, params, null,  -1, -1);
        }
        else if(name.equals("putint")){
            returnType = "void";
            param.setType("int");
            params.add(param);
            return new Symbol(name, false, "function", returnType, true, 0, floor, -1, params, null,  -1, -1);
        }
        else if(name.equals("putdouble")){
            returnType = "void";
            param.setType("double");
            params.add(param);
            return new Symbol(name, false, "function", returnType, true, 0, floor, -1, params, null,  -1, -1);
        }
        else if(name.equals("putchar")){
            returnType = "void";
            param.setType("int");
            params.add(param);
            return new Symbol(name, false, "function", returnType, true, 0, floor, -1, params, null,  -1, -1);
        }
        else if(name.equals("putstr")){
            returnType = "void";
            param.setType("string");
            params.add(param);
            return new Symbol(name, false, "function", returnType, true, 0, floor, -1, params, null,  -1, -1);
        }
        else if(name.equals("putln")){
            returnType = "void";
            return new Symbol(name, false, "function", returnType, true, 0, floor, -1, params, null,  -1, -1);
        }
        else
            return null;
    }

    private String analyseAssign_Expr(Symbol l_Symbol) throws Exception{
        //常量肯定是不行的
        if (l_Symbol.isConst())
            throw new Exception();

        //如果lident是函数参数
        if (l_Symbol.getParamPos() != -1) {
            //获取该参数的函数
            Symbol func = l_Symbol.getFunction();

            //参数存在ret_slots后面
            if (func.getReturnType().equals("void"))
                throw new Exception();
            instructions.add(new Instruction("arga", 1+l_Symbol.getParamPos()));
        }
        //如果该ident是局部变量
        else if(l_Symbol.getFloor() != 0) {
            instructions.add(new Instruction("loca", l_Symbol.getLocalID()));
        }
        //如果该ident是全局变量
        else {
            instructions.add(new Instruction("globa", l_Symbol.getGlobalID()));
        }

        expect(TokenType.ASSIGN);
        String exprType = analyseExpr();
        //弹栈
        while (!op.empty())
            MyFunctions.operatorInstructions(op.pop(), instructions, exprType);

        //设置该符号为已赋值
        if (!l_Symbol.getType().equals(exprType)){
            throw new Exception();
        }
        assignSymbol(l_Symbol.getName());
        instructions.add(new Instruction("store.64", null));
        return "void";
    }

    private String analyseCall_Expr(Symbol symbol, boolean isLibrary) throws Exception{
        Instruction instruction;

        if(isLibrary){
            String libName = symbol.getName();
            globalTable.add(new Global(true, libName.length(), libName));
            instruction = new Instruction("callname", globalVCount);
            globalVCount++;
        }
        else{
            if(!symbol.getType().equals("function"))
                throw new Exception();
            //如果是函数
            int id = MyFunctions.getFunctionId(symbol.getName(), functionTable);
            instruction = new Instruction("call", id + 1);
        }

        String name = symbol.getName();
        expect(TokenType.L_PAREN);
        //将左括号入运算符栈
        op.push(TokenType.L_PAREN);

        if (MyFunctions.functionHasReturn(name, functionTable))
            instructions.add(new Instruction("stackalloc", 1));
        else
            instructions.add(new Instruction("stackalloc", 0));

        if(!check(TokenType.R_PAREN)){
            analyseCall_Param_List(symbol);
        }
        expect(TokenType.R_PAREN);

        //弹出左括号
        op.pop();

        //此时再将call语句压入
        instructions.add(instruction);
        //返回函数的返回类型
        return symbol.getReturnType();
    }

    /**
     * call_param_list -> expr (',' expr)*
     * @param symbol
     */
    private void analyseCall_Param_List(Symbol symbol) throws Exception{
        int i;
        List<Symbol> params = symbol.getParams();
        int paramNum = params.size();

        //如果对应位置的参数类型不匹配，则报错
        String type = analyseExpr();
        while (!op.empty() && op.peek() != TokenType.L_PAREN)
            MyFunctions.operatorInstructions(op.pop(), instructions, type);

        for (i = 0; i<paramNum; i++){
            if(!params.get(i).getType().equals(type))
                throw new Exception();
        }

        i = 0;

        while(check(TokenType.COMMA)){
            expect(TokenType.COMMA);
            //如果对应位置的参数类型不匹配，则报错
            type = analyseExpr();
            while (!op.empty() && op.peek() != TokenType.L_PAREN)
                MyFunctions.operatorInstructions(op.pop(), instructions, type);
            i++;
        }
        //如果参数个数不匹配，则报错
        if(i != paramNum)
            throw new Exception();
    }

    private String analyseLiteralExpr() throws Exception{
        if(check(TokenType.UINT_LITERAL)){
            Token token = next();
            instructions.add(new Instruction("push", (Integer) token.getValue()));
            return "int";
        }
        else if(check(TokenType.STRING_LITERAL)){
            Token token = next();
            String name = (String) token.getValue();
            //加入全局符号表
            globalTable.add(new Global(true, name.length(), name));

            instructions.add(new Instruction("push", globalVCount));
            globalVCount++;
            return "string";
        }
        else
            throw new Exception();
    }

    private String analyseGroupExpr() throws Exception{
        expect(TokenType.L_PAREN);
        op.push(TokenType.L_PAREN);
        String exprType = analyseExpr();
        expect(TokenType.R_PAREN);

        //弹栈
        while (op.peek() != TokenType.L_PAREN)
            MyFunctions.operatorInstructions(op.pop(), instructions, exprType);

        //弹出左括号
        op.pop();
        return exprType;

    }

    private Symbol searchSymbol(Token ident) {
        String name = String.valueOf(ident.getValue());
        //查找
        for(int i=symbolTable.size()-1; i>=0; i--){
            Symbol symbol = symbolTable.get(i);
            //如果遇到名字相同的则返回位置
            if(symbol.getName().equals(name))
                return symbol;
        }
        return null;
    }

    /**
     * negate_expr -> '-' expr
     * @return
     * @throws Exception
     */
    private String analyseNegate_Expr() throws Exception{
        expect(TokenType.MINUS);
        String type = analyseExpr();

        instructions.add(new Instruction("neg.i", null));
        return type;
    }

    /**
     * ident_expr -> IDENT
     * @param symbol
     * @return
     * @throws Exception
     */
    private String analyseIdent_Expr(Symbol symbol) throws Exception{
        if(symbol.getType().equals("void"))
            throw new Exception();

        if (symbol.getParamPos() != -1) {
            //获取该参数的函数
            Symbol func = symbol.getFunction();
            //参数存在ret_slots后面
            if (func.getReturnType().equals("void"))
                instructions.add(new Instruction("arga", symbol.getParamPos()));
            else
                instructions.add(new Instruction("arga", symbol.getParamPos()+1));
        }
        else if(symbol.getFloor() != 1) {
            instructions.add(new Instruction("loca", symbol.getLocalID()));
        }
        //如果该ident是全局变量
        else {
            instructions.add(new Instruction("globa", symbol.getGlobalID()));
        }
        instructions.add(new Instruction("load.64", null));
        return symbol.getType();
    }

    /**
     * binary_operator -> '+' | '-' | '*' | '/' | '==' | '!=' | '<' | '>' | '<=' | '>='
     * operator_expr -> expr binary_operator expr
     * @param exprType
     * @return
     * @throws Exception
     */
    private String analyseOperator_Expr(String exprType) throws Exception{
        Token token;
        if(check(TokenType.AS_KW) ||
                check(TokenType.PLUS)||
                check(TokenType.MINUS)||
                check(TokenType.MUL)||
                check(TokenType.DIV)||
                check(TokenType.EQ)||
                check(TokenType.NEQ)||
                check(TokenType.LT)||
                check(TokenType.GT)||
                check(TokenType.LE)||
                check(TokenType.GE)){
            token = next();
        }
        //不是以上类型
        else
            throw new Exception();

        //比较终结符优先级，判断要不要计算
        //如果栈内终结符优先级高，则弹出该终结符，并计算
        if (!op.empty()) {
            int in = Operator.getOrder(op.peek());
            int out = Operator.getOrder(token.getTokenType());
            if (Operator.priority[in][out] > 0)
                MyFunctions.operatorInstructions(op.pop(), instructions, exprType);
        }
        op.push(token.getTokenType());

        String type =  analyseExpr();
        //如果运算符左右两侧类型一致，且为double或int
        if(exprType.equals(type) && exprType.equals("int"))
            return type;
        else
            throw new Exception();
    }

    /**
     * 声明语句
     * decl_stmt -> let_decl_stmt | const_decl_stmt
     * @throws Exception
     */

    private void analyseDecl_Stmt() throws Exception{
        while (check(TokenType.LET_KW) || check(TokenType.CONST_KW)){
            if (check(TokenType.LET_KW)){
                analyseLet_Decl_Stmt();
            }
            else if (check(TokenType.CONST_KW)){
                analyseConst_Decl_Stmt();
            }

            if (floor == 0){
                globalVCount++;
            }
            else localVCount++;
        }
    }

    /**
     * let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'
     * @throws Exception
     */
    private void analyseLet_Decl_Stmt() throws Exception {
        String identName;
        String type;
        boolean isInitialized = false;
        Token ident;
        Instruction instruction;

        expect(TokenType.LET_KW);
        ident = expect(TokenType.IDENT);
        identName = String.valueOf(ident.getValue());
        expect(TokenType.COLON);

        //获取类型并报错
        if((type = analyseTy()).equals("void"))
            throw new Exception();


        //如果遇到等号，则该声明给赋了值
        if(check(TokenType.ASSIGN)){
            isInitialized = true;

            //判断是全局还是局部
            if (floor == 0) {
                instruction = new Instruction("globa", globalVCount);
            }
            else {
                instruction = new Instruction("loca", localVCount);
            }
            instructions.add(instruction);
            expect(TokenType.ASSIGN);

            String exprType = analyseExpr();

            //将运算符弹栈并计算
            while (!op.empty())
                MyFunctions.operatorInstructions(op.pop(), instructions, exprType);

            //将值存入
            instruction = new Instruction("store.64", null);
            instructions.add(instruction);
        }

        expect(TokenType.SEMICOLON);
        Symbol symbol;
        //加入符号表
        //如果是全局变量
        if(floor == 1){
            symbol = new Symbol(identName, false, type, "", isInitialized, getNextVariableOffset(),
                    floor, -1, null, null, -1, globalVCount);
            Global global = new Global(false);
            globalTable.add(global);
        }

            //如果是局部变量
        else{
            symbol = new Symbol(identName, false, type, "", isInitialized, getNextVariableOffset(),
                    floor, -1, null, null, localVCount, -1);
        }
        addSymbol(symbol);

    }



    /**
     * const_decl_stmt -> 'const' IDENT ':' ty '=' expr ';'
     * @throws Exception
     */
    private void analyseConst_Decl_Stmt() throws Exception{
        String identName;
        String type;
        String exprType;
        Token ident;
        Instruction instruction;

        expect(TokenType.CONST_KW);
        ident = expect(TokenType.IDENT);
        identName = String.valueOf(ident.getValue());
        expect(TokenType.COLON);

        if((type = analyseTy()).equals("void"))
            throw new Exception();

        //加入取地址操作
        if (floor == 0) {
            instruction = new Instruction("globa", globalVCount);
        }
        else {
            instruction = new Instruction("loca", localVCount);
        }
        instructions.add(instruction);
        expect(TokenType.ASSIGN);

        exprType = analyseExpr();
        //将运算符弹栈并计算
        while (!op.empty())
            MyFunctions.operatorInstructions(op.pop(), instructions, exprType);

        //将值存入
        instruction = new Instruction("store.64", null);
        instructions.add(instruction);

        expect(TokenType.SEMICOLON);
        Symbol symbol;
        //全局变量
        if(floor == 1){
            symbol = new Symbol(identName, true, type, "", true, getNextVariableOffset(),
                    floor, -1, null, null, -1, globalVCount);
            Global global = new Global(true);
            globalTable.add(global);
        }
        //局部变量
        else{
            symbol = new Symbol(identName, true, type, "", true, getNextVariableOffset(),
                    floor, -1, null, null, -1, globalVCount);
        }
        addSymbol(symbol);

    }

    /**
     * ty -> IDENT
     * @return
     * @throws Exception
     */
    private String analyseTy() throws Exception {
        Token ty = expect(TokenType.IDENT);
        if (!ty.getValue().equals("int") || !ty.getValue().equals("void")){
            throw new Exception();
        }
        return String.valueOf(ty.getValue());
    }

    private int searchSymbolNum(String name) throws Exception{
        for(int i=0; i<symbolTable.size(); i++){
            if(symbolTable.get(i).getName().equals(name)) return i;
        }
        return -1;
    }

    private void assignSymbol(String name) throws Exception{
        if (searchSymbolNumByName(name) == -1)
            throw new Exception();
        symbolTable.get(searchSymbolNumByName(name)).setInitialized(true);
    }

    public Function get_start() {
        return _start;
    }

    public Tokenizer getTokenizer() {
        return tokenizer;
    }

    public void setTokenizer(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public ArrayList<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(ArrayList<Instruction> instructions) {
        this.instructions = instructions;
    }

    public Token getPeekedToken() {
        return peekedToken;
    }

    public void setPeekedToken(Token peekedToken) {
        this.peekedToken = peekedToken;
    }

    public int getNextOffset() {
        return nextOffset;
    }

    public void setNextOffset(int nextOffset) {
        this.nextOffset = nextOffset;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getGlobalVCount() {
        return globalVCount;
    }

    public void setGlobalVCount(int globalVCount) {
        this.globalVCount = globalVCount;
    }

    public int getLocalVCount() {
        return localVCount;
    }

    public void setLocalVCount(int localVCount) {
        this.localVCount = localVCount;
    }

    public int getFunctionCount() {
        return functionCount;
    }

    public void setFunctionCount(int functionCount) {
        this.functionCount = functionCount;
    }

    public List<Symbol> getSymbolTable() {
        return symbolTable;
    }

    public void setSymbolTable(List<Symbol> symbolTable) {
        this.symbolTable = symbolTable;
    }

    public List<Function> getFunctionTable() {
        return functionTable;
    }

    public void setFunctionTable(List<Function> functionTable) {
        this.functionTable = functionTable;
    }

    public List<Global> getGlobalTable() {
        return globalTable;
    }

    public void setGlobalTable(List<Global> globalTable) {
        this.globalTable = globalTable;
    }

    public Stack<TokenType> getOp() {
        return op;
    }

    public void setOp(Stack<TokenType> op) {
        this.op = op;
    }

    public Symbol getAnalFunction() {
        return analFunction;
    }

    public void setAnalFunction(Symbol analFunction) {
        this.analFunction = analFunction;
    }

    public Symbol getRetFunction() {
        return retFunction;
    }

    public void setRetFunction(Symbol retFunction) {
        this.retFunction = retFunction;
    }

    public int getCirculateLayer() {
        return circulateLayer;
    }

    public void setCirculateLayer(int circulateLayer) {
        this.circulateLayer = circulateLayer;
    }

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }
}
