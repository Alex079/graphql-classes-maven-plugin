package integration.test;

class Data {

    private String f1 = null;

    private Object f2 = null;

    public String getF1() {
        return f1;
    }

    public Object getF2() {
        return f2;
    }

    public void setF1(String f1) {
        this.f1 = f1;
    }

    public void setF2(Object f2) {
        this.f2 = f2;
    }

    @Override
    public String toString() {
        return "f1='" + f1 + "', f2=" + f2;
    }

}
