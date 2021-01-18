package miniplc0java.symbol;

import java.util.List;

public class Symbol {
    //标识符的名字
    String name;
    //是否是常量
    boolean isConst;
    //标识符的类别，int，function
    String type;
    //函数返回类型(如果有的话)
    String returnType;
    //是否初始化
    boolean isInitialized;
    //偏移
    int stackOffset;
    //标识符所在层数
    int floor;

    //是否是函数参数,如果不是则为-1，如果是则为在函数参数列表的位置
    int paramPos;

    //如果是函数，则是参数列表
    List<Symbol> params;
    //如果是参数，则为其函数符号；如果不是则为null
    Symbol function;

    //如果是局部变量，他的id;如果不是，-1
    int localID;
    //如果是全局变量，他的id;如果不是，-1
    int globalID;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isConst() {
        return isConst;
    }

    public void setConst(boolean aConst) {
        isConst = aConst;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }

    public int getStackOffset() {
        return stackOffset;
    }

    public void setStackOffset(int stackOffset) {
        this.stackOffset = stackOffset;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getParamPos() {
        return paramPos;
    }

    public void setParamPos(int paramPos) {
        this.paramPos = paramPos;
    }

    public List<Symbol> getParams() {
        return params;
    }

    public void setParams(List<Symbol> params) {
        this.params = params;
    }

    public Symbol getFunction() {
        return function;
    }

    public void setFunction(Symbol function) {
        this.function = function;
    }

    public int getLocalID() {
        return localID;
    }

    public void setLocalID(int localID) {
        this.localID = localID;
    }

    public int getGlobalID() {
        return globalID;
    }

    public void setGlobalID(int globalID) {
        this.globalID = globalID;
    }

    public Symbol(String name, boolean isConst, String type, String returnType, boolean isInitialized, int stackOffset,
                  int floor, int paramPos, List<Symbol> params, Symbol function, int localID, int globalID) {
        this.name = name;
        this.isConst = isConst;
        this.type = type;
        this.returnType = returnType;
        this.isInitialized = isInitialized;
        this.stackOffset = stackOffset;
        this.floor = floor;
        this.paramPos = paramPos;
        this.params = params;
        this.function = function;
        this.localID = localID;
        this.globalID = globalID;
    }

    public Symbol(){

    }
}
