package cn.com.glsx.test;

public class TestMain {
    public static void main(String[] args) {
        String col = "tvd.created_date";
        System.out.println(col.substring(col.indexOf(".") + 1));
    }
}
