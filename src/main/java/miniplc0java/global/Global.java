package miniplc0java.global;

public class Global {
    boolean isConst;
    int count;
    String items;

    public boolean getIsConst() {
        return isConst;
    }

    public void setIsConst(boolean isConst) {
        this.isConst = isConst;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public Global(boolean isConst, int count, String items) {
        this.isConst = isConst;
        this.count = count;
        this.items = items;
    }

    public Global(boolean isConst) {
        this.isConst = isConst;
        this.count = 0;
        this.items = null;
    }
}
