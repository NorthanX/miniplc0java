package miniplc0java.function;

import miniplc0java.instruction.Instruction;

import java.util.List;

public class Function {

    String name;
    int floor;
    //符号表的序号
    int id;
    //返回类型，0为void，1为int
    int retType;
    //参数个数
    int paramNum;
    //局部变量个数
    int localVNum;
    //指令集
    List<Instruction> instructions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRetType() {
        return retType;
    }

    public void setRetType(int retType) {
        this.retType = retType;
    }

    public int getParamNum() {
        return paramNum;
    }

    public void setParamNum(int paramNum) {
        this.paramNum = paramNum;
    }

    public int getLocalVNum() {
        return localVNum;
    }

    public void setLocalVNum(int localVNum) {
        this.localVNum = localVNum;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public Function(String name, int floor, int id, int retType, int paramNum, int localVNum, List<Instruction> instructions) {
        this.name = name;
        this.floor = floor;
        this.id = id;
        this.retType = retType;
        this.paramNum = paramNum;
        this.localVNum = localVNum;
        this.instructions = instructions;
    }
}
