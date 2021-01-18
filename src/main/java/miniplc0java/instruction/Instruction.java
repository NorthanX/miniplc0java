package miniplc0java.instruction;

import java.util.Objects;

public class Instruction {
    //操作
    String opt;
    //参数
    Integer x;


    public Instruction(String opt) {
        this.opt = opt;
        this.x = 0;
    }

    public Instruction(String opt, Integer x) {
        this.opt = opt;
        this.x = x;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Instruction that = (Instruction) o;
        return opt == that.opt && Objects.equals(x, that.x);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opt, x);
    }

    public String getOpt() {
        return opt;
    }

    public void setOpt(String opt) {
        this.opt = opt;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }
}
